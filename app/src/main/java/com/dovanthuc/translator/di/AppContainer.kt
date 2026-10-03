package com.dovanthuc.translator.di

import android.content.Context
import com.dovanthuc.translator.data.local.db.AppDatabase
import com.dovanthuc.translator.data.local.db.AssetJsonSeeder
import com.dovanthuc.translator.data.prefs.SettingsDataStore
import com.dovanthuc.translator.data.remote.datasync.DataSyncManager
import com.dovanthuc.translator.data.remote.openai.OpenAiRepository
import com.dovanthuc.translator.data.repository.TranslationCardRepository

class AppContainer(context: Context) {
    private val database by lazy { AppDatabase.getInstance(context) }
    private val cardDao by lazy { database.translationCardDao() }
    private val assetJsonSeeder by lazy { AssetJsonSeeder(context) }

    val settingsDataStore by lazy { SettingsDataStore(context) }

    val translationCardRepository by lazy {
        TranslationCardRepository(cardDao, assetJsonSeeder, settingsDataStore)
    }

    val dataSyncManager by lazy {
        DataSyncManager(cardDao, settingsDataStore)
    }

    val openAiRepository by lazy {
        OpenAiRepository(settingsDataStore)
    }
}
