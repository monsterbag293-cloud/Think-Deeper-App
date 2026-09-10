package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ApiMessage(
  val role: String,
  val content: String
)

@JsonClass(generateAdapter = true)
data class ChatApiRequest(
  val messages: List<ApiMessage>,
  val model: String,
  val provider: String? = null,
  val intensity: String = "medium",
  val isDeepThink: Boolean = false,
  val apiKey: String? = null,
  val temperature: Double = 0.4
)

@JsonClass(generateAdapter = true)
data class ChatApiResponse(
  val reply: String? = null,
  val content: String? = null,
  val activities: List<ActivityStepResponse>? = null,
  @Json(name = "think_ms") val thinkMs: Long? = 0L,
  val model: String? = null
)

@JsonClass(generateAdapter = true)
data class ActivityStepResponse(
  val agent: String = "Agent 1",
  val name: String = "Step",
  val status: String = "done",
  val detail: String = ""
)

@JsonClass(generateAdapter = true)
data class ServerConfigResponse(
  val name: String? = "Think Deeper",
  val version: String? = "2.0.0",
  val status: String? = "online",
  val models: List<String>? = listOf("gpt-4o-mini", "claude-3-5-sonnet", "deepseek-r1", "gemini-2.5-flash"),
  val motd: String? = "Connected to Vercel production endpoints"
)

@JsonClass(generateAdapter = true)
data class PushTokenRequest(
  val token: String,
  val platform: String = "android"
)

@JsonClass(generateAdapter = true)
data class PushTokenResponse(
  val success: Boolean = true,
  val message: String = "Device registered"
)

@JsonClass(generateAdapter = true)
data class OpenAiChatRequest(
  val model: String,
  val messages: List<ApiMessage>,
  val temperature: Float = 0.7f,
  val stream: Boolean = false
)

@JsonClass(generateAdapter = true)
data class OpenAiChatResponse(
  val choices: List<OpenAiChoice>? = null,
  val error: OpenAiError? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
  val message: ApiMessage? = null,
  @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiError(
  val message: String? = null,
  val type: String? = null,
  val code: String? = null
)

// Gemini native models
@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(
  val contents: List<GeminiContent>,
  @Json(name = "system_instruction") val systemInstruction: GeminiContent? = null,
  val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
  val role: String? = null,
  val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
  val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
  val temperature: Float = 0.4f,
  val maxOutputTokens: Int = 8192
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
  val candidates: List<GeminiCandidate>? = null,
  val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
  val content: GeminiContent? = null,
  val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiError(
  val code: Int? = null,
  val message: String? = null,
  val status: String? = null
)

interface VercelApiService {
  @GET
  fun getServerConfig(
    @Url url: String,
    @Header("Authorization") authHeader: String? = null
  ): Call<ServerConfigResponse>

  @POST
  fun sendChat(
    @Url url: String,
    @Body request: ChatApiRequest,
    @Header("Authorization") authHeader: String? = null
  ): Call<ChatApiResponse>

  @POST
  fun sendChatDirect(
    @Body request: ChatApiRequest,
    @Header("Authorization") authHeader: String? = null
  ): Call<ChatApiResponse>

  @POST
  fun sendOpenAiChat(
    @Url url: String,
    @Body request: OpenAiChatRequest,
    @Header("Authorization") authHeader: String? = null
  ): Call<OpenAiChatResponse>

  @POST("chat/completions")
  fun chatCompletions(
    @Header("Authorization") authHeader: String,
    @Body request: OpenAiChatRequest
  ): Call<OpenAiChatResponse>

  @POST
  fun sendGeminiChat(
    @Url url: String,
    @Body request: GeminiGenerateRequest,
    @Header("Authorization") authHeader: String? = null
  ): Call<GeminiGenerateResponse>

  @POST("v1beta/models/{model}:generateContent")
  fun generateGemini(
    @Path("model") model: String,
    @Query("key") apiKey: String,
    @Body request: GeminiGenerateRequest
  ): Call<GeminiGenerateResponse>

  @POST
  fun registerPushToken(
    @Url url: String,
    @Body request: PushTokenRequest,
    @Header("Authorization") authHeader: String? = null
  ): Call<PushTokenResponse>
}

object VercelApiClient {
  private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
  }

  val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .build()

  fun createService(baseUrl: String): VercelApiService {
    val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    return Retrofit.Builder()
      .baseUrl(cleanUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
      .create(VercelApiService::class.java)
  }

  fun createGeminiService(baseUrl: String = "https://generativelanguage.googleapis.com/"): VercelApiService {
    val cleanUrl = if (baseUrl.isBlank()) "https://generativelanguage.googleapis.com/" else if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    return Retrofit.Builder()
      .baseUrl(cleanUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
      .create(VercelApiService::class.java)
  }

  fun createDirectService(baseUrl: String): VercelApiService {
    val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    return Retrofit.Builder()
      .baseUrl(cleanUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
      .create(VercelApiService::class.java)
  }
}
