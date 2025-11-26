package com.example.crm_logistico_movil.models.ai

import com.google.gson.annotations.SerializedName

// Modelos para la API de Google Gemini

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content,
    @SerializedName("finishReason")
    val finishReason: String?,
    val index: Int?
)

// Modelos para el chat interno
data class AIChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false
)