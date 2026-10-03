#!/usr/bin/env python3
"""
Parse the Vietnamese->English translation practice HTML files under data/*.html
into translation_cards.json + version.json, consumed by the Translator Android app
(bundled as default assets, and pushed to the KaneWill660/translator GitHub repo
so the app can silently sync updates).

Usage:
    python tools/import_data/parse_translations.py

Outputs (overwritten each run):
    data/translation_cards.json
    data/version.json
    app/src/main/assets/translation_cards.json   (copy, bundled into the app)
    app/src/main/assets/version.json              (copy, bundled into the app)

Records without a usable sample English answer are skipped entirely (per product
decision — see docs/business_description.md / the implementation plan). Records
where the `translation`-class paragraph turns out to be a grammar note (contains
"=") instead of the real answer are recovered from the last `other`-class
paragraph in that record and flagged with needsReview=true for a one-time manual
check of the generated JSON.
"""
from __future__ import annotations

import hashlib
import json
import re
import sys
from dataclasses import dataclass, asdict
from datetime import datetime, timezone
from pathlib import Path

from bs4 import BeautifulSoup

ROOT = Path(__file__).resolve().parents[2]
DATA_DIR = ROOT / "data"
ASSETS_DIR = ROOT / "app" / "src" / "main" / "assets"

DICH_PREFIX_RE = re.compile(r"^\s*D[ịi]ch\s*:\s*", re.IGNORECASE)

# Vietnamese-specific diacritic letters (lowercase) — presence of any of these is a strong,
# language-only signal that a paragraph is Vietnamese text rather than an English sentence.
_VIETNAMESE_CHARS = set(
    "àáảãạăằắẳẵặâầấẩẫậđèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵ"
)


def contains_vietnamese(text: str) -> bool:
    return any(ch in _VIETNAMESE_CHARS for ch in text.lower())


def looks_like_note(text: str) -> bool:
    """Short vocab/grammar-equivalence lines (e.g. "Lan truyền mạnh = go viral") that should
    never be mistaken for the actual sentence to translate or its answer."""
    stripped = text.strip()
    return "=" in stripped or stripped.endswith(":") or len(stripped.split()) <= 3


_LABEL_PREFIX_RE = re.compile(r"^\s*(?:câu\s*dịch|dịch)\s*[:.]\s*", re.IGNORECASE)
_HINT_PREFIX_RE = re.compile(r"^\s*(?:giải\s*nghĩa|dùng\s*để|cách\s*dùng)\s*[:.]?\s*", re.IGNORECASE)
_LEADING_NUMBER_RE = re.compile(r"^\s*\d+\s*[.)]\s*")


def extract_pairs(topic: str, ordered: list[tuple[str, str]]):
    """Language-based extraction for records without p.primary/p.translation.

    A Vietnamese sentence is paired with the first following non-note paragraph, provided that
    paragraph is English (no Vietnamese letters). This naturally rejects Vietnamese hints
    ("Dùng để…", "GIẢI NGHĨA: …"), which are followed by another Vietnamese paragraph, and
    skips vocab notes ("A = B", short phrases) sitting between a sentence and its answer.
    Returns (formula, hint, [(vietnamese, english), ...]).
    """
    texts = [_LABEL_PREFIX_RE.sub("", t).strip() for _, t in ordered]
    n = len(texts)
    pairs: list[tuple[str, str]] = []
    first_pair_index: int | None = None

    i = 0
    while i < n:
        t = texts[i]
        is_candidate = (
            ordered[i][0] != "formula"
            and t
            and contains_vietnamese(t)
            and not looks_like_note(t)
            and not _HINT_PREFIX_RE.match(t)
        )
        if is_candidate:
            j = i + 1
            while j < n and looks_like_note(texts[j]):
                j += 1
            if j < n and not contains_vietnamese(texts[j]) and not _HINT_PREFIX_RE.match(texts[j]):
                if first_pair_index is None:
                    first_pair_index = i
                pairs.append((normalize_sentence(t), texts[j]))
                i = j + 1
                continue
        i += 1

    # Hint = first Vietnamese descriptive paragraph (formula or other) before the first pair.
    hint: str | None = None
    formula: str | None = None
    for idx in range(first_pair_index if first_pair_index is not None else n):
        cls, raw = ordered[idx]
        text = texts[idx]
        if cls == "formula" and not _HINT_PREFIX_RE.match(text):
            formula = formula or text
            continue
        if hint is None and (_HINT_PREFIX_RE.match(text) or contains_vietnamese(text)) and not looks_like_note(text):
            hint = normalize_sentence(_HINT_PREFIX_RE.sub("", text).strip()) or None
    if formula is None:
        # Hint-only p.formula (e.g. "Giải nghĩa: …") — the real formula is the h2 title.
        formula = _LEADING_NUMBER_RE.sub("", topic).strip() or None
    return formula, hint, pairs


def normalize_sentence(text: str) -> str:
    """Some source files write the Vietnamese sentence in ALL CAPS for emphasis; convert those
    to normal sentence case for a nicer reading experience. Leaves already-normal-case text
    (the more common convention) untouched."""
    stripped = text.strip()
    if stripped and stripped == stripped.upper():
        return stripped[0].upper() + stripped[1:].lower()
    return stripped


@dataclass
class Card:
    id: int
    topic: str
    vietnameseSentence: str
    sampleAnswer: str
    formula: str | None
    hint: str | None
    sourceFile: str
    needsReview: bool


def clean_text(el) -> str:
    return el.get_text(strip=True) if el is not None else ""


def parse_file(path: Path) -> tuple[list[Card], int, int]:
    """Returns (cards, skipped_no_primary, skipped_no_answer)."""
    soup = BeautifulSoup(path.read_text(encoding="utf-8"), "html.parser")
    source_name = path.stem  # "Translation 74"

    cards: list[Card] = []
    skipped_no_primary = 0
    skipped_no_answer = 0

    for section in soup.find_all("section", class_="record"):
        topic = clean_text(section.find("h2"))
        paragraphs = section.find_all("p")

        formula_text = None
        primary_text = None
        translation_texts: list[str] = []
        hint_texts: list[str] = []  # p.other seen BEFORE p.primary (usage hint for the formula)
        post_texts: list[str] = []  # p.other seen AFTER p.primary (candidate answer / vocab notes)

        for p in paragraphs:
            classes = p.get("class") or []
            text = clean_text(p)
            if not text:
                continue
            if "formula" in classes and formula_text is None:
                formula_text = text
            elif "primary" in classes and primary_text is None:
                primary_text = text
            elif "translation" in classes:
                translation_texts.append(text)
            elif "other" in classes:
                (hint_texts if primary_text is None else post_texts).append(text)

        needs_review = False
        vietnamese_sentence: str | None = None
        sample_answer: str | None = None
        hint_text = hint_texts[0] if hint_texts else None

        if primary_text:
            # Original file format: explicit p.primary (the VN sentence) + p.translation
            # (the sample answer).
            vietnamese_sentence = DICH_PREFIX_RE.sub("", primary_text).strip()

            if translation_texts:
                candidate = translation_texts[0]
                if "=" in candidate:
                    # The `translation`-class paragraph is actually a grammar/vocab note, not
                    # the real answer sentence — fall back to the first *sentence-like* `other`
                    # paragraph appearing after `primary` (skipping further vocab notes, which
                    # also tend to contain "=" and can appear after the real answer).
                    needs_review = True
                    sample_answer = next((t for t in post_texts if "=" not in t), None)
                    if sample_answer is None:
                        print(
                            f"  [skip] {source_name} · '{topic}': translation trông như ghi chú "
                            f"ngữ pháp ('{candidate}') nhưng không có p.other phù hợp để fallback",
                            file=sys.stderr,
                        )
                else:
                    sample_answer = candidate
        else:
            # Newer file formats (Translation 80+): no p.primary / p.translation classes — only
            # p.formula / p.other, and the layout varies between files (hint inside p.formula
            # or p.other, "DỊCH:" / "CÂU DỊCH:" labels, ALL-CAPS VN, several pairs per record…).
            # Classify paragraphs by language instead of position — see extract_pairs().
            ordered = [
                (("formula" if "formula" in (p.get("class") or []) else "other"), clean_text(p))
                for p in paragraphs
                if clean_text(p)
            ]
            if not ordered:
                continue  # section heading with no content (e.g. "1. Nói về ảnh hưởng ...")

            formula_for_card, hint_for_card, pairs = extract_pairs(topic, ordered)
            if not pairs:
                skipped_no_primary += 1
                print(f"  [skip] {source_name} · '{topic}': không tìm được cặp câu Việt → Anh", file=sys.stderr)
                continue
            for vn, en in pairs:
                cards.append(
                    Card(
                        id=0,
                        topic=topic,
                        vietnameseSentence=vn,
                        sampleAnswer=en,
                        formula=formula_for_card,
                        hint=hint_for_card,
                        sourceFile=source_name,
                        needsReview=True,
                    )
                )
            continue

        if not vietnamese_sentence:
            skipped_no_primary += 1
            print(f"  [skip] {source_name} · '{topic}': không có câu tiếng Việt (p.primary)", file=sys.stderr)
            continue

        if not sample_answer:
            skipped_no_answer += 1
            print(f"  [skip] {source_name} · '{topic}': không tìm được đáp án mẫu tiếng Anh", file=sys.stderr)
            continue

        cards.append(
            Card(
                id=0,  # assigned later, after collecting all files, for stable global ordering
                topic=topic,
                vietnameseSentence=vietnamese_sentence,
                sampleAnswer=sample_answer,
                formula=formula_text,
                hint=hint_text,
                sourceFile=source_name,
                needsReview=needs_review,
            )
        )

    return cards, skipped_no_primary, skipped_no_answer


def main() -> None:
    html_files = sorted(DATA_DIR.glob("*.html"))
    if not html_files:
        print(f"Không tìm thấy file .html nào trong {DATA_DIR}", file=sys.stderr)
        sys.exit(1)

    all_cards: list[Card] = []
    total_skipped_no_primary = 0
    total_skipped_no_answer = 0
    review_count = 0

    for path in html_files:
        print(f"Đang đọc {path.name}...")
        cards, skipped_no_primary, skipped_no_answer = parse_file(path)
        total_skipped_no_primary += skipped_no_primary
        total_skipped_no_answer += skipped_no_answer
        all_cards.extend(cards)

    for index, card in enumerate(all_cards, start=1):
        card.id = index
        if card.needsReview:
            review_count += 1

    cards_json = json.dumps([asdict(c) for c in all_cards], ensure_ascii=False, indent=2)
    content_hash = hashlib.sha256(cards_json.encode("utf-8")).hexdigest()[:12]

    version_payload = {
        "version": content_hash,
        "generatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "count": len(all_cards),
    }
    version_json = json.dumps(version_payload, ensure_ascii=False, indent=2)

    for target_dir in (DATA_DIR, ASSETS_DIR):
        target_dir.mkdir(parents=True, exist_ok=True)
        (target_dir / "translation_cards.json").write_text(cards_json + "\n", encoding="utf-8")
        (target_dir / "version.json").write_text(version_json + "\n", encoding="utf-8")

    print()
    print(f"Tổng số câu dùng được: {len(all_cards)}")
    print(f"  - Bỏ qua vì thiếu câu tiếng Việt: {total_skipped_no_primary}")
    print(f"  - Bỏ qua vì thiếu đáp án mẫu: {total_skipped_no_answer}")
    print(f"  - Cần review thủ công (needsReview=true): {review_count}")
    print(f"Version: {content_hash}")
    print()
    print(f"Đã ghi: {DATA_DIR / 'translation_cards.json'}")
    print(f"Đã ghi: {DATA_DIR / 'version.json'}")
    print(f"Đã ghi: {ASSETS_DIR / 'translation_cards.json'}")
    print(f"Đã ghi: {ASSETS_DIR / 'version.json'}")

    if review_count:
        print()
        print(f"⚠ Có {review_count} record needsReview=true — nên mở {DATA_DIR / 'translation_cards.json'} kiểm tra thủ công trước khi commit/push.")


if __name__ == "__main__":
    main()
