package com.dovanthuc.translator.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dovanthuc.translator.domain.model.TranslationCard

@Entity(tableName = "translation_cards")
data class TranslationCardEntity(
    @PrimaryKey val id: Long,
    val topic: String,
    val vietnameseSentence: String,
    val sampleAnswer: String,
    val formula: String?,
    val hint: String?,
    val sourceFile: String,
    val needsReview: Boolean
)

fun TranslationCardEntity.toDomain() = TranslationCard(
    id = id,
    topic = topic,
    vietnameseSentence = vietnameseSentence,
    sampleAnswer = sampleAnswer,
    formula = formula,
    hint = hint,
    sourceFile = sourceFile,
    needsReview = needsReview
)

fun TranslationCardDto.toEntity() = TranslationCardEntity(
    id = id,
    topic = topic,
    vietnameseSentence = vietnameseSentence,
    sampleAnswer = sampleAnswer,
    formula = formula,
    hint = hint,
    sourceFile = sourceFile,
    needsReview = needsReview
)
