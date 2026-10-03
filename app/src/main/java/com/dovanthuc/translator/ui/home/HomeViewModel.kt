package com.dovanthuc.translator.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dovanthuc.translator.data.prefs.DataSyncStatus
import com.dovanthuc.translator.data.prefs.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(settingsDataStore: SettingsDataStore) : ViewModel() {

    val dataSyncStatus: StateFlow<DataSyncStatus> = settingsDataStore.dataSyncStatus.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DataSyncStatus(version = null, generatedAt = null, count = 0, lastSyncedAtMillis = null)
    )
}
