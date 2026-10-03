package com.dovanthuc.translator.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dovanthuc.translator.data.prefs.DataSyncStatus
import com.dovanthuc.translator.data.prefs.SettingsDataStore
import com.dovanthuc.translator.data.remote.datasync.DataSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val dataSyncManager: DataSyncManager
) : ViewModel() {

    val dataSyncStatus: StateFlow<DataSyncStatus> = settingsDataStore.dataSyncStatus.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DataSyncStatus(version = null, generatedAt = null, count = 0, lastSyncedAtMillis = null)
    )

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            apiKeyInput = settingsDataStore.getOpenAiApiKey().orEmpty(),
            isKeySaved = !settingsDataStore.getOpenAiApiKey().isNullOrBlank()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onApiKeyInputChanged(value: String) {
        _uiState.update { it.copy(apiKeyInput = value, isKeySaved = false) }
    }

    fun saveApiKey() {
        val key = _uiState.value.apiKeyInput.trim()
        if (key.isEmpty()) return
        settingsDataStore.setOpenAiApiKey(key)
        _uiState.update { it.copy(isKeySaved = true) }
    }

    fun checkForUpdatesNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            dataSyncManager.syncIfNeeded()
            _uiState.update { it.copy(isSyncing = false) }
        }
    }
}
