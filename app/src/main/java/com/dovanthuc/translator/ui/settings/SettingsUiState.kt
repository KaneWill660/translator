package com.dovanthuc.translator.ui.settings

data class SettingsUiState(
    val apiKeyInput: String = "",
    val isKeySaved: Boolean = false,
    val isSyncing: Boolean = false
)
