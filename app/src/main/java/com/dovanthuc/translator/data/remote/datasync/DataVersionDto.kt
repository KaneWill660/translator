package com.dovanthuc.translator.data.remote.datasync

import kotlinx.serialization.Serializable

@Serializable
data class DataVersionDto(
    val version: String,
    val generatedAt: String,
    val count: Int
)
