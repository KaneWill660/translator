package com.dovanthuc.translator.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "translator_settings")

data class DataSyncStatus(
    val version: String?,
    val generatedAt: String?,
    val count: Int,
    val lastSyncedAtMillis: Long?
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val LOCAL_DATA_VERSION = stringPreferencesKey("local_data_version")
        val LOCAL_DATA_COUNT = intPreferencesKey("local_data_count")
        val LOCAL_DATA_GENERATED_AT = stringPreferencesKey("local_data_generated_at")
        val LAST_SYNCED_AT = longPreferencesKey("last_synced_at")
    }

    val dataSyncStatus: Flow<DataSyncStatus> = context.dataStore.data.map { prefs ->
        DataSyncStatus(
            version = prefs[Keys.LOCAL_DATA_VERSION],
            generatedAt = prefs[Keys.LOCAL_DATA_GENERATED_AT],
            count = prefs[Keys.LOCAL_DATA_COUNT] ?: 0,
            lastSyncedAtMillis = prefs[Keys.LAST_SYNCED_AT]
        )
    }

    suspend fun currentLocalDataVersion(): String? = dataSyncStatus.first().version

    /** Records the dataset now stored in Room (bundled seed or remote sync). */
    suspend fun recordDataset(version: String, generatedAt: String, count: Int, syncedAtMillis: Long? = null) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOCAL_DATA_VERSION] = version
            prefs[Keys.LOCAL_DATA_GENERATED_AT] = generatedAt
            prefs[Keys.LOCAL_DATA_COUNT] = count
            if (syncedAtMillis != null) prefs[Keys.LAST_SYNCED_AT] = syncedAtMillis
        }
    }

    // --- OpenAI API key: stored separately via EncryptedSharedPreferences (credential material) ---

    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "translator_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getOpenAiApiKey(): String? = encryptedPrefs.getString(KEY_OPENAI_API_KEY, null)

    fun setOpenAiApiKey(key: String) {
        encryptedPrefs.edit().putString(KEY_OPENAI_API_KEY, key).apply()
    }

    fun clearOpenAiApiKey() {
        encryptedPrefs.edit().remove(KEY_OPENAI_API_KEY).apply()
    }

    companion object {
        private const val KEY_OPENAI_API_KEY = "openai_api_key"
    }
}
