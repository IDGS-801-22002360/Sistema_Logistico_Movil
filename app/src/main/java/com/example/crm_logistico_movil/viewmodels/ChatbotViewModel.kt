package com.example.crm_logistico_movil.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.services.ChatAnalysisService
import com.example.crm_logistico_movil.services.SmartResponseGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatbotViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _chatContext = MutableStateFlow(ChatContext())
    val chatContext: StateFlow<ChatContext> = _chatContext.asStateFlow()

    private val _suggestedActions = MutableStateFlow<List<String>>(emptyList())
    val suggestedActions: StateFlow<List<String>> = _suggestedActions.asStateFlow()

    private val dateFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val analysisService = ChatAnalysisService()
    private val responseGenerator = SmartResponseGenerator()

    init {
        // Cargar contexto del cliente (esto se podría obtener del repositorio)
        loadClientContext()
        // Mensaje de bienvenida personalizado al iniciar el chat
        sendSmartWelcomeMessage()
    }

    private fun loadClientContext() {
        // Simular carga de contexto del cliente
        // En la implementación real, esto vendría del repositorio/API
        viewModelScope.launch {
            val context = ChatContext(
                clientId = "CLIENT_001",
                clientName = "Juan Pérez",
                hasActiveOperations = true,
                pendingInvoices = 2,
                overdueInvoices = 0,
                lastOperationStatus = "En tránsito",
                totalOperationsThisMonth = 5,
                averageDeliveryTime = 3.2,
                preferredLanguage = "es"
            )
            _chatContext.value = context
        }
    }

    private fun sendSmartWelcomeMessage() {
        viewModelScope.launch {
            delay(500) // Pequeña pausa para cargar contexto

            val context = _chatContext.value
            val welcomeMessage = if (context.clientName.isNotEmpty()) {
                "¡Hola ${context.clientName}! 👋 Soy tu asistente virtual inteligente de LogiCorp.\n\n🎯 He revisado tu cuenta y veo que:\n${generateContextSummary(context)}\n\n💬 Estoy aquí para ayudarte con cualquier consulta. ¡Pregúntame lo que necesites!"
            } else {
                "¡Hola! 👋 Soy tu asistente virtual inteligente de LogiCorp.\n\nPuedo ayudarte con:\n• 📦 Consultas detalladas sobre operaciones\n• 🗺 Tracking en tiempo real\n• 💰 Estados de facturas y pagos\n• 📝 Cotizaciones personalizadas\n• 🛠 Soporte técnico especializado\n\n¿En qué puedo ayudarte hoy?"
            }

            val chatMessage = ChatMessage(
                content = welcomeMessage,
                isFromUser = false,
                timestamp = getCurrentTime()
            )

            _messages.value = listOf(chatMessage)

            // Sugerir acciones basadas en el contexto
            val actions = mutableListOf<String>()
            if (context.hasActiveOperations) actions.add("Ver mis operaciones activas")
            if (context.pendingInvoices > 0) actions.add("Revisar facturas pendientes")
            actions.add("Solicitar nueva cotización")
            actions.add("Contactar soporte")

            _suggestedActions.value = actions
        }
    }

    private fun generateContextSummary(context: ChatContext): String {
        val summary = mutableListOf<String>()

        if (context.hasActiveOperations) {
            summary.add("✅ Tienes ${context.totalOperationsThisMonth} operaciones activas")
        }

        if (context.pendingInvoices > 0) {
            summary.add("📄 ${context.pendingInvoices} factura(s) pendiente(s)")
        }

        if (context.overdueInvoices > 0) {
            summary.add("⚠ ${context.overdueInvoices} factura(s) vencida(s)")
        }

        if (context.averageDeliveryTime > 0) {
            summary.add("📊 Tu promedio de entrega es ${context.averageDeliveryTime} días")
        }

        return if (summary.isNotEmpty()) {
            summary.joinToString("\n")
        } else {
            "🆕 ¡Bienvenido a LogiCorp!"
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            content = text,
            isFromUser = true,
            timestamp = getCurrentTime()
        )

        _messages.value = _messages.value + userMessage

        // Actualizar contexto de conversación
        val currentContext = _chatContext.value
        currentContext.conversationFlow.add(text)

        // Simular que el bot está procesando (tiempo variable según complejidad)
        _isTyping.value = true

        viewModelScope.launch {
            // Analizar intención del mensaje
            val intent = analysisService.analyzeMessage(text, currentContext)

            // Tiempo de procesamiento realista
            val processingTime = calculateProcessingTime(intent, text)
            delay(processingTime)

            // Generar respuesta inteligente
            val smartResponse = responseGenerator.generateResponse(intent, currentContext, text)

            // Crear mensaje del bot
            val botMessage = ChatMessage(
                content = smartResponse.message,
                isFromUser = false,
                timestamp = getCurrentTime()
            )

            _isTyping.value = false
            _messages.value = _messages.value + botMessage

            // Actualizar contexto con nueva información
            updateContext(smartResponse, intent)

            // Actualizar acciones sugeridas
            _suggestedActions.value = smartResponse.suggestedActions

            // Si requiere escalación, notificar
            if (intent.requiresHumanEscalation) {
                handleHumanEscalation(intent, text)
            }
        }
    }

    private fun calculateProcessingTime(intent: MessageIntent, text: String): Long {
        val baseTime = 800L
        val complexityMultiplier = when {
            intent.entities.isNotEmpty() -> 1.5
            intent.urgencyLevel >= 4 -> 0.5 // Responder más rápido a urgencias
            text.length > 100 -> 1.3
            else -> 1.0
        }
        val randomVariation = (0.8 + Math.random() * 0.4) // 80%-120% del tiempo base

        return (baseTime * complexityMultiplier * randomVariation).toLong()
    }

    private fun updateContext(response: SmartResponse, intent: MessageIntent) {
        val currentContext = _chatContext.value

        // Actualizar último tema de interacción
        currentContext.lastInteractionTopic = response.topic

        // Agregar operaciones mencionadas
        intent.entities["operation_id"]?.let { opId ->
            if (!currentContext.mentionedOperations.contains(opId)) {
                currentContext.mentionedOperations.add(opId)
            }
        }

        // Registrar issues si es una queja
        if (intent.type == IntentType.COMPLAINT) {
            currentContext.currentIssues.add("Queja registrada: ${getCurrentTime()}")
        }

        // Aplicar actualizaciones de contexto específicas
        response.contextUpdates.forEach { (key, value) ->
            when (key) {
                "escalated" -> { /* Marcar como escalado */ }
                "urgent_case" -> { /* Marcar como caso urgente */ }
                "complaint_registered" -> { /* Registrar queja */ }
            }
        }

        _chatContext.value = currentContext
    }

    private fun handleHumanEscalation(intent: MessageIntent, userMessage: String) {
        viewModelScope.launch {
            delay(2000) // Simular tiempo de escalación

            val escalationMessage = ChatMessage(
                content = "🔔 *Escalación Automática*\n\nHe notificado a nuestro equipo de atención especializada sobre tu consulta.\n\n📞 Te contactarán en los próximos 15 minutos al:\n• Teléfono registrado\n• Email principal\n\n🆔 Número de caso: #ESC-${System.currentTimeMillis().toString().takeLast(6)}\n\n⏰ Mientras tanto, sigo aquí para cualquier otra consulta.",
                isFromUser = false,
                timestamp = getCurrentTime()
            )

            _messages.value = _messages.value + escalationMessage
        }
    }

    fun selectSuggestedAction(action: String) {
        // Simular selección de acción sugerida
        sendMessage("Quiero $action")
    }

    fun updateClientContext(
        hasActiveOperations: Boolean? = null,
        pendingInvoices: Int? = null,
        clientName: String? = null
    ) {
        val currentContext = _chatContext.value

        hasActiveOperations?.let { currentContext.hasActiveOperations = it }
        pendingInvoices?.let { currentContext.pendingInvoices = it }
        clientName?.let { currentContext.clientName = it }

        _chatContext.value = currentContext
    }

    private fun getCurrentTime(): String {
        return dateFormatter.format(Date())
    }
}