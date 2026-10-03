package com.dovanthuc.translator.data.local.db

import kotlinx.serialization.Serializable

@Serializable
data class TranslationCardDto(
    val id: Long,
    val topic: String,
    val vietnameseSentence: String,
    val sampleAnswer: String,
    val formula: String? = null,
    val hint: String? = null,
    val sourceFile: String,
    val needsReview: Boolean = false
)
