package com.dovanthuc.translator.domain.model

data class TranslationCard(
    val id: Long,
    val topic: String,
    val vietnameseSentence: String,
    val sampleAnswer: String,
    val formula: String?,
    val hint: String?,
    val sourceFile: String,
    val needsReview: Boolean
)
