package com.dovanthuc.translator.ui.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Thin wrapper around Android's built-in [SpeechRecognizer], configured for Vietnamese
 * speech input (the user speaks Vietnamese; translation to English happens afterwards via AI).
 *
 * Must be created and used from the main thread (Android requirement for [SpeechRecognizer]).
 */
class SpeechRecognizerManager(private val context: Context) {

    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(
        onListeningChanged: (Boolean) -> Unit,
        onPartialResult: (String) -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isAvailable()) {
            onError("Thiết bị không hỗ trợ nhận diện giọng nói (thiếu app Google hoặc engine nhận diện)")
            return
        }

        // Reuse the same SpeechRecognizer instance across start/stop cycles instead of
        // destroying and recreating it every time — repeatedly tearing down and rebinding to
        // the OS speech-recognition service is what was causing ERROR_SERVER_DISCONNECTED (11)
        // when starting again shortly after stopping. Only [destroy] (screen leaving) actually
        // releases it.
        val activeRecognizer = recognizer
            ?: SpeechRecognizer.createSpeechRecognizer(context).also { recognizer = it }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        activeRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onListeningChanged(true)
            }

            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                onListeningChanged(false)
            }

            override fun onError(error: Int) {
                onListeningChanged(false)
                onError(mapError(error))
            }

            override fun onResults(results: Bundle?) {
                onListeningChanged(false)
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    onResult(text)
                } else {
                    onError("Không nhận diện được giọng nói, thử lại nhé")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    onPartialResult(text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })

        activeRecognizer.startListening(intent)
    }

    /**
     * User-initiated "I'm done talking" — asks the recognizer to stop capturing audio and
     * process whatever it has so far (still delivers results/error asynchronously via the
     * listener registered in [startListening]). This is what the mic button calls when tapped
     * again while already listening.
     */
    fun finishListening() {
        recognizer?.stopListening()
    }

    /** Hard cleanup: used before starting a fresh session and when the screen goes away. */
    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun mapError(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH -> "Không nhận diện được, thử nói lại nhé"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Không nghe thấy gì, thử lại"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Chưa cấp quyền micro"
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            "Lỗi mạng khi nhận diện giọng nói, kiểm tra kết nối rồi thử lại"
        SpeechRecognizer.ERROR_SERVER, SpeechRecognizer.ERROR_SERVER_DISCONNECTED ->
            "Mất kết nối với dịch vụ nhận diện giọng nói, thử lại nhé"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Bộ nhận diện đang bận, thử lại"
        SpeechRecognizer.ERROR_CLIENT -> "Đã dừng nhận diện"
        else -> "Lỗi nhận diện giọng nói (mã $error), thử lại nhé"
    }
}
