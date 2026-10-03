package com.dovanthuc.translator.ui.flashcard

import com.dovanthuc.translator.domain.model.TranslationCard

data class FlashcardEntry(
    val card: TranslationCard,
    val userInput: String = "",
    val isSubmitted: Boolean = false
)

data class FlashcardUiState(
    val history: List<FlashcardEntry> = emptyList(),
    val currentIndex: Int = -1,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
) {
    val current: FlashcardEntry? get() = history.getOrNull(currentIndex)
    val canGoPrev: Boolean get() = currentIndex > 0
}
