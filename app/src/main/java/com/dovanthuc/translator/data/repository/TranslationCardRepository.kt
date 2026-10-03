package com.dovanthuc.translator.data.repository

import com.dovanthuc.translator.data.local.db.AssetJsonSeeder
import com.dovanthuc.translator.data.local.db.TranslationCardDao
import com.dovanthuc.translator.data.local.db.toDomain
import com.dovanthuc.translator.data.local.db.toEntity
import com.dovanthuc.translator.data.prefs.SettingsDataStore
import com.dovanthuc.translator.domain.model.TranslationCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TranslationCardRepository(
    private val dao: TranslationCardDao,
    private val assetJsonSeeder: AssetJsonSeeder,
    private val settingsDataStore: SettingsDataStore
) {
    private val seedMutex = Mutex()
    private var seeded = false

    /**
     * Idempotent. Seeds Room from the bundled asset JSON when the table is empty, or when the
     * bundled dataset is newer than what is stored (e.g. after an app update that ships more
     * cards) — otherwise an old install would keep serving its stale first-run data forever.
     */
    suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return
            val bundled = assetJsonSeeder.readBundledVersion()
            val stored = settingsDataStore.dataSyncStatus.first()
            val bundledIsNewer = bundled != null &&
                (stored.generatedAt == null || bundled.generatedAt > stored.generatedAt)
            if (dao.count() == 0 || bundledIsNewer) {
                val cards = assetJsonSeeder.readBundledCards()
                dao.replaceAll(cards.map { it.toEntity() })
                if (bundled != null) {
                    settingsDataStore.recordDataset(bundled.version, bundled.generatedAt, cards.size)
                }
            }
            seeded = true
        }
    }

    suspend fun getRandomCard(excludeId: Long? = null): TranslationCard? {
        ensureSeeded()
        val entity = if (excludeId != null) {
            dao.getRandomCardExcluding(excludeId)
        } else {
            dao.getRandomCard()
        }
        return entity?.toDomain()
    }
}
