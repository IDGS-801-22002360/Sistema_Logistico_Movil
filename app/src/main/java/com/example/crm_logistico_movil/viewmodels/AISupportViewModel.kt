package com.example.crm_logistico_movil.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crm_logistico_movil.api.GeminiApiClient
import com.example.crm_logistico_movil.config.GeminiConfig
import com.example.crm_logistico_movil.models.ai.AIChatMessage
import com.example.crm_logistico_movil.models.ai.Content
import com.example.crm_logistico_movil.models.ai.GeminiRequest
import com.example.crm_logistico_movil.models.ai.Part
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AISupportViewModel : ViewModel() {
    private val geminiApi = GeminiApiClient.apiService

    private val _messages = MutableStateFlow<List<AIChatMessage>>(emptyList())
    val messages: StateFlow<List<AIChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        // Mensaje de bienvenida
        addMessage(
            AIChatMessage(
                content = "¡Hola! 👋 Soy tu asistente de soporte inteligente de LogiCorp.\n\n" +
                        "Estoy aquí para ayudarte con cualquier duda sobre:\n" +
                        "🚚 Operaciones logísticas\n" +
                        "📋 Documentación\n" +
                        "💰 Facturación\n" +
                        "📦 Tracking de envíos\n" +
                        "🌍 Comercio internacional\n\n" +
                        "¿En qué puedo ayudarte hoy?",
                isFromUser = false
            )
        )
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // Agregar mensaje del usuario
        addMessage(
            AIChatMessage(
                content = userMessage,
                isFromUser = true
            )
        )

        // Agregar mensaje de "escribiendo" de la IA
        val loadingMessage = AIChatMessage(
            content = "",
            isFromUser = false,
            isLoading = true
        )
        addMessage(loadingMessage)

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                println("🤖 Enviando mensaje a Gemini: $userMessage")
                println("🔑 API Key configurada: ${GeminiConfig.API_KEY.take(20)}...")
                println("🌐 URL base: ${GeminiConfig.BASE_URL}")

                val response = geminiApi.generateContent(
                    request = createGeminiRequest(userMessage)
                )

                println("📡 Respuesta HTTP: ${response.code()}")

                if (response.isSuccessful) {
                    val geminiResponse = response.body()
                    println("✅ Respuesta exitosa: ${geminiResponse?.candidates?.size} candidatos")

                    val aiResponse = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                    if (!aiResponse.isNullOrBlank()) {
                        // Remover mensaje de loading y agregar respuesta real
                        removeLoadingMessage()
                        addMessage(
                            AIChatMessage(
                                content = aiResponse,
                                isFromUser = false
                            )
                        )
                    } else {
                        println("⚠ Respuesta vacía de Gemini")
                        handleError("No pude generar una respuesta. ¿Puedes intentar reformular tu pregunta?")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("❌ Error en Gemini API: ${response.code()} - ${response.message()}")
                    println("Error details: $errorBody")

                    val errorMessage = when (response.code()) {
                        400 -> "Solicitud inválida. Verifica tu mensaje."
                        401 -> "API Key inválida. Verifica tu configuración."
                        403 -> "Acceso denegado. Verifica los permisos de tu API Key."
                        429 -> "Demasiadas solicitudes. Espera un momento e intenta de nuevo."
                        500 -> "Error del servidor de Google. Intenta más tarde."
                        else -> "Error al conectar con el servicio de IA (${response.code()}). Inténtalo de nuevo."
                    }

                    handleError(errorMessage)
                }

            } catch (e: java.net.UnknownHostException) {
                println("🌐 Error de conexión: Sin internet")
                handleError("Sin conexión a internet. Verifica tu conexión y vuelve a intentarlo.")
            } catch (e: java.net.SocketTimeoutException) {
                println("⏰ Timeout de conexión")
                handleError("La conexión tardó demasiado. Verifica tu internet e inténtalo de nuevo.")
            } catch (e: Exception) {
                println("💥 Exception en IA: ${e.message}")
                e.printStackTrace()
                handleError("Error inesperado: ${e.message?.take(100) ?: "Error desconocido"}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun createGeminiRequest(userMessage: String): GeminiRequest {
        // Simplificamos el mensaje - solo texto del usuario
        return GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = "Eres un asistente de LogiCorp. Responde en español de forma amigable y profesional. Pregunta del usuario: $userMessage")
                    )
                )
            )
        )
    }

    private fun addMessage(message: AIChatMessage) {
        _messages.value = _messages.value + message
    }

    private fun removeLoadingMessage() {
        _messages.value = _messages.value.filterNot { it.isLoading }
    }

    private fun handleError(errorMessage: String) {
        removeLoadingMessage()
        addMessage(
            AIChatMessage(
                content = "❌ $errorMessage\n\n🔄 Puedes intentar nuevamente o contactar a nuestro equipo de soporte humano si el problema persiste.",
                isFromUser = false,
                hasError = true
            )
        )
        _error.value = errorMessage
    }

    fun clearError() {
        _error.value = null
    }

    fun clearChat() {
        _messages.value = listOf(
            AIChatMessage(
                content = "Chat reiniciado. ¿En qué puedo ayudarte? 🤖",
                isFromUser = false
            )
        )
    }
}