package com.dovanthuc.translator.data.local.db

import android.content.Context
import com.dovanthuc.translator.data.remote.datasync.DataVersionDto
import kotlinx.serialization.json.Json

class AssetJsonSeeder(
    private val context: Context,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun readBundledCards(): List<TranslationCardDto> {
        val text = context.assets.open(CARDS_ASSET).bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }

    fun readBundledVersion(): DataVersionDto? = runCatching {
        val text = context.assets.open(VERSION_ASSET).bufferedReader().use { it.readText() }
        json.decodeFromString<DataVersionDto>(text)
    }.getOrNull()

    companion object {
        private const val CARDS_ASSET = "translation_cards.json"
        private const val VERSION_ASSET = "version.json"
    }
}
