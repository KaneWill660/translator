package com.dovanthuc.translator.ui.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dovanthuc.translator.data.remote.openai.MissingApiKeyException
import com.dovanthuc.translator.data.remote.openai.OpenAiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VoiceTranslateViewModel(
    private val openAiRepository: OpenAiRepository,
    private val speechRecognizerManager: SpeechRecognizerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceTranslateUiState())
    val uiState: StateFlow<VoiceTranslateUiState> = _uiState.asStateFlow()

    private var listeningTimeoutJob: Job? = null

    /** Set when the user manually taps "stop" — auto-sends whatever text comes back for
     *  translation, but only for that manual stop, not every natural end-of-speech. */
    private var autoTranslateOnNextResult = false

    fun startListening() {
        val state = _uiState.value
        if (state.isListening || state.isProcessingStop) {
            // Already listening, or the previous session is still tearing down after a manual
            // stop — starting a new one right now races with the OS speech service releasing
            // the old session and tends to fail with "mất kết nối" (ERROR_SERVER_DISCONNECTED).
            return
        }
        autoTranslateOnNextResult = false
        _uiState.update { it.copy(error = null, translatedText = "", recognizedText = "") }
        speechRecognizerManager.startListening(
            onListeningChanged = { listening ->
                _uiState.update { it.copy(isListening = listening) }
                if (listening) {
                    armListeningTimeout()
                } else {
                    listeningTimeoutJob?.cancel()
                }
            },
            onPartialResult = { text -> _uiState.update { it.copy(recognizedText = text) } },
            onResult = { text ->
                listeningTimeoutJob?.cancel()
                _uiState.update { it.copy(recognizedText = text, isListening = false, isProcessingStop = false) }
                if (autoTranslateOnNextResult) {
                    autoTranslateOnNextResult = false
                    translate()
                }
            },
            onError = { message ->
                listeningTimeoutJob?.cancel()
                autoTranslateOnNextResult = false
                _uiState.update { it.copy(isListening = false, isProcessingStop = false, error = message) }
            }
        )
    }

    /** Called when the user taps the mic again while it's already listening — finishes the
     *  recognition and, once the (final) text comes back, sends it straight to translate.
     *  Blocks new starts via [isProcessingStop] until that final callback actually arrives. */
    fun finishListening() {
        if (!_uiState.value.isListening) return
        autoTranslateOnNextResult = true
        _uiState.update { it.copy(isProcessingStop = true) }
        speechRecognizerManager.finishListening()
    }

    /** Hard-stops listening right away, no waiting for a final result — used when the user
     *  moves on to another action (e.g. tapping "Dịch sang Anh") while still listening. */
    private fun stopListeningImmediately() {
        listeningTimeoutJob?.cancel()
        speechRecognizerManager.destroy()
        autoTranslateOnNextResult = false
        _uiState.update { it.copy(isListening = false, isProcessingStop = false) }
    }

    private fun armListeningTimeout() {
        listeningTimeoutJob?.cancel()
        listeningTimeoutJob = viewModelScope.launch {
            delay(LISTENING_TIMEOUT_MS)
            speechRecognizerManager.destroy()
            autoTranslateOnNextResult = false
            _uiState.update {
                it.copy(
                    isListening = false,
                    isProcessingStop = false,
                    error = "Không nhận được phản hồi, đã dừng nghe — thử lại nhé"
                )
            }
        }
    }

    fun onRecognizedTextChanged(text: String) {
        _uiState.update { it.copy(recognizedText = text) }
    }

    fun translate() {
        if (_uiState.value.isListening) {
            stopListeningImmediately()
        }
        val text = _uiState.value.recognizedText.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true, error = null) }
            openAiRepository.translateToEnglish(text)
                .onSuccess { translated ->
                    _uiState.update { it.copy(isTranslating = false, translatedText = translated) }
                }
                .onFailure { throwable ->
                    val message = when (throwable) {
                        is MissingApiKeyException -> throwable.message.orEmpty()
                        else -> throwable.message ?: "Không thể dịch lúc này, thử lại sau"
                    }
                    _uiState.update { it.copy(isTranslating = false, error = message) }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listeningTimeoutJob?.cancel()
        speechRecognizerManager.destroy()
    }

    companion object {
        private const val LISTENING_TIMEOUT_MS = 12_000L
    }
}
