package com.dovanthuc.translator.ui.voice

data class VoiceTranslateUiState(
    val isListening: Boolean = false,
    /** True right after the user taps "stop", until the recognizer's final result/error
     *  actually arrives. Starting a new session before that settles races with the OS speech
     *  service tearing down the previous one and tends to fail with "mất kết nối" (error 11). */
    val isProcessingStop: Boolean = false,
    val recognizedText: String = "",
    val translatedText: String = "",
    val isTranslating: Boolean = false,
    val error: String? = null
)
