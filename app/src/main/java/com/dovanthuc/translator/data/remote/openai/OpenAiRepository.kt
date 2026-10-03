package com.dovanthuc.translator.data.remote.openai

import com.dovanthuc.translator.data.prefs.SettingsDataStore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class MissingApiKeyException : Exception("Chưa cấu hình OpenAI API Key trong Cài đặt")

class OpenAiRepository(
    private val settingsDataStore: SettingsDataStore,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        // Request DTOs (e.g. ChatCompletionRequest.model) rely on Kotlin default parameter
        // values — without this, kotlinx.serialization omits any field still at its default,
        // which made the gateway see a missing/null "model" and reject the request.
        encodeDefaults = true
    }
) {
    private val api: OpenAiApi by lazy { buildApi() }

    suspend fun translateToEnglish(vietnameseText: String): Result<String> {
        val apiKey = settingsDataStore.getOpenAiApiKey()
        if (apiKey.isNullOrBlank()) {
            return Result.failure(MissingApiKeyException())
        }
        return runCatching {
            val request = ChatCompletionRequest(
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = "You are a professional Vietnamese-to-English translator. " +
                            "Translate the user's Vietnamese text into natural, fluent English. " +
                            "Reply with only the translation, no explanation."
                    ),
                    ChatMessage(role = "user", content = vietnameseText)
                )
            )
            val response = api.createChatCompletion(request)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val message = errorBody?.let {
                    runCatching { json.decodeFromString<OpenAiErrorBody>(it).error?.message }.getOrNull()
                }
                error(message ?: "OpenAI request failed (HTTP ${response.code()})")
            }
            response.body()?.choices?.firstOrNull()?.message?.content?.trim()
                ?: error("Không nhận được bản dịch từ AI")
        }
    }

    private fun buildApi(): OpenAiApi {
        val authInterceptor = okhttp3.Interceptor { chain ->
            val apiKey = settingsDataStore.getOpenAiApiKey()
            val requestBuilder = chain.request().newBuilder()
            if (!apiKey.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }
            chain.proceed(requestBuilder.build())
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(OpenAiApi::class.java)
    }

    companion object {
        // OpenAI-compatible gateway (not the direct api.openai.com endpoint) — same
        // request/response shape, just a different base URL + API key.
        private const val BASE_URL = "https://aiportalapi.stu-platform.live/jpe/"
    }
}
