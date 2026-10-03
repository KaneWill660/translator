package com.dovanthuc.translator.data.remote.datasync

import com.dovanthuc.translator.data.local.db.TranslationCardDao
import com.dovanthuc.translator.data.local.db.TranslationCardDto
import com.dovanthuc.translator.data.local.db.toEntity
import com.dovanthuc.translator.data.prefs.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Silent, best-effort data sync: checks the small [VERSION_URL] first, and only
 * downloads the full [CARDS_URL] payload when the remote version actually differs.
 * Any failure (offline, timeout, malformed response) is swallowed — callers must
 * never block the UI on this, and the app keeps using whatever is already in Room.
 */
class DataSyncManager(
    private val dao: TranslationCardDao,
    private val settingsDataStore: SettingsDataStore,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()
) {
    suspend fun syncIfNeeded() {
        runCatching {
            withTimeoutOrNull(VERSION_CHECK_TIMEOUT_MS) {
                val remoteVersionJson = fetch(VERSION_URL) ?: return@withTimeoutOrNull
                val remote = json.decodeFromString<DataVersionDto>(remoteVersionJson)

                val local = settingsDataStore.dataSyncStatus.first()
                if (remote.version == local.version) return@withTimeoutOrNull
                // Never downgrade: a remote file older than what's already in Room (e.g. the
                // GitHub copy wasn't pushed yet after the app shipped newer bundled data) is ignored.
                if (local.generatedAt != null && remote.generatedAt <= local.generatedAt) {
                    return@withTimeoutOrNull
                }

                val cardsJson = fetch(CARDS_URL) ?: return@withTimeoutOrNull
                val cards = json.decodeFromString<List<TranslationCardDto>>(cardsJson)
                if (cards.isEmpty()) return@withTimeoutOrNull

                dao.replaceAll(cards.map { it.toEntity() })
                settingsDataStore.recordDataset(
                    version = remote.version,
                    generatedAt = remote.generatedAt,
                    count = cards.size,
                    syncedAtMillis = System.currentTimeMillis()
                )
            }
        }
    }

    private fun fetch(url: String): String? = runCatching {
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@use null
            response.body?.string()
        }
    }.getOrNull()

    companion object {
        private const val REPO_RAW_BASE =
            "https://raw.githubusercontent.com/KaneWill660/translator/main/data"
        private const val VERSION_URL = "$REPO_RAW_BASE/version.json"
        private const val CARDS_URL = "$REPO_RAW_BASE/translation_cards.json"
        private const val VERSION_CHECK_TIMEOUT_MS = 20_000L
    }
}
