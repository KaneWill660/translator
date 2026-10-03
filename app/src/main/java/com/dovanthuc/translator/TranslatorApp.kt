package com.dovanthuc.translator

import android.app.Application
import com.dovanthuc.translator.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TranslatorApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Silent background data sync: never blocks app startup or the UI.
        applicationScope.launch {
            container.translationCardRepository.ensureSeeded()
            container.dataSyncManager.syncIfNeeded()
        }
    }
}
