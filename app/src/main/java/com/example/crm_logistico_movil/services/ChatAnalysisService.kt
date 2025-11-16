package com.example.crm_logistico_movil.services

import com.example.crm_logistico_movil.models.*
import java.util.regex.Pattern

class ChatAnalysisService {

    // Patrones para detectar entidades específicas
    private val operationPattern = Pattern.compile("(op|operación|operacion|envío|envio)\\s*[#:]?\\s*([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE)
    private val invoicePattern = Pattern.compile("(factura|fact|invoice)\\s*[#:]?\\s*([a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE)
    private val amountPattern = Pattern.compile("(\\$|\\$\\$|pesos|dolares|usd)\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE)
    private val datePattern = Pattern.compile("(hoy|ayer|mañana|\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE)
    private val timePattern = Pattern.compile("\\d{1,2}:\\d{2}\\s*(am|pm)?", Pattern.CASE_INSENSITIVE)

    // Palabras clave por urgencia
    private val urgentKeywords = listOf(
        "urgente", "emergencia", "rápido", "rapido", "inmediato", "ya",
        "problema", "error", "perdido", "robado", "dañado", "accidente"
    )

    private val complaintKeywords = listOf(
        "queja", "reclamo", "molesto", "enojado", "disgustado", "insatisfecho",
        "mal servicio", "terrible", "pésimo", "horrible", "nunca más"
    )

    private val gratitudeKeywords = listOf(
        "gracias", "thank you", "excelente", "perfecto", "muy bien",
        "increíble", "fantástico", "buenísimo", "genial"
    )

    fun analyzeMessage(message: String, context: ChatContext): MessageIntent {
        val lowerMessage = message.lowercase()

        // Detectar entidades
        val entities = mutableMapOf<String, String>()

        operationPattern.matcher(message).let { matcher ->
            if (matcher.find()) {
                entities["operation_id"] = matcher.group(2)
            }
        }

        invoicePattern.matcher(message).let { matcher ->
            if (matcher.find()) {
                entities["invoice_id"] = matcher.group(2)
            }
        }

        amountPattern.matcher(message).let { matcher ->
            if (matcher.find()) {
                entities["amount"] = matcher.group(2)
                entities["currency"] = matcher.group(1)
            }
        }

        if (datePattern.matcher(message).find()) {
            entities["date_mentioned"] = "true"
        }

        if (timePattern.matcher(message).find()) {
            entities["time_mentioned"] = "true"
        }

        // Determinar tipo de intención
        val intentType = when {
            isGreeting(lowerMessage) -> IntentType.GREETING
            isFarewell(lowerMessage) -> IntentType.FAREWELL
            isComplaint(lowerMessage) -> IntentType.COMPLAINT
            isGratitude(lowerMessage) -> IntentType.GRATITUDE
            isQuestion(lowerMessage) -> IntentType.QUESTION
            isRequest(lowerMessage) -> IntentType.REQUEST
            isFollowUp(lowerMessage, context) -> IntentType.FOLLOW_UP
            isClarification(lowerMessage) -> IntentType.CLARIFICATION
            else -> IntentType.INFORMATION_SEEKING
        }

        // Calcular nivel de urgencia
        val urgencyLevel = calculateUrgency(lowerMessage, intentType)

        // Determinar confianza
        val confidence = calculateConfidence(lowerMessage, intentType, entities)

        // ¿Requiere escalación humana?
        val requiresEscalation = shouldEscalateToHuman(lowerMessage, intentType, urgencyLevel)

        return MessageIntent(
            type = intentType,
            entities = entities,
            confidence = confidence,
            urgencyLevel = urgencyLevel,
            requiresHumanEscalation = requiresEscalation
        )
    }

    private fun isGreeting(message: String): Boolean {
        val greetingWords = listOf("hola", "buenos", "buenas", "saludos", "hi", "hello", "buenas tardes", "buenas noches")
        return greetingWords.any { message.contains(it) }
    }

    private fun isFarewell(message: String): Boolean {
        val farewellWords = listOf("adiós", "adios", "bye", "hasta luego", "nos vemos", "chao", "me voy")
        return farewellWords.any { message.contains(it) }
    }

    private fun isComplaint(message: String): Boolean {
        return complaintKeywords.any { message.contains(it) }
    }

    private fun isGratitude(message: String): Boolean {
        return gratitudeKeywords.any { message.contains(it) }
    }

    private fun isQuestion(message: String): Boolean {
        val questionWords = listOf("qué", "que", "cómo", "como", "dónde", "donde", "cuándo", "cuando", "por qué", "porque")
        val hasQuestionMark = message.contains("?")
        return hasQuestionMark || questionWords.any { message.startsWith(it) }
    }

    private fun isRequest(message: String): Boolean {
        val requestWords = listOf("necesito", "requiero", "solicito", "puedes", "podrías", "me ayudas", "quiero", "deseo")
        return requestWords.any { message.contains(it) }
    }

    private fun isFollowUp(message: String, context: ChatContext): Boolean {
        val followUpWords = listOf("también", "tambien", "además", "ademas", "y", "otra cosa", "otra pregunta")
        return followUpWords.any { message.contains(it) } && context.conversationFlow.isNotEmpty()
    }

    private fun isClarification(message: String): Boolean {
        val clarificationWords = listOf("no entiendo", "explica", "clarifica", "qué significa", "que significa", "no comprendo")
        return clarificationWords.any { message.contains(it) }
    }

    private fun calculateUrgency(message: String, intentType: IntentType): Int {
        var urgency = when (intentType) {
            IntentType.URGENT -> 5
            IntentType.COMPLAINT -> 4
            IntentType.REQUEST -> 3
            IntentType.QUESTION -> 2
            else -> 1
        }

        // Incrementar por palabras urgentes
        urgentKeywords.forEach { keyword ->
            if (message.contains(keyword)) {
                urgency = minOf(5, urgency + 1)
            }
        }

        return urgency
    }

    private fun calculateConfidence(message: String, intentType: IntentType, entities: Map<String, String>): Float {
        var confidence = 0.5f

        // Mayor confianza si hay entidades específicas
        if (entities.isNotEmpty()) confidence += 0.3f

        // Ajustar por tipo de intención
        when (intentType) {
            IntentType.GREETING, IntentType.FAREWELL, IntentType.GRATITUDE -> confidence += 0.4f
            IntentType.QUESTION -> if (message.contains("?")) confidence += 0.3f
            IntentType.REQUEST -> if (message.contains("necesito") || message.contains("requiero")) confidence += 0.2f
            else -> confidence += 0.1f
        }

        return minOf(1.0f, confidence)
    }

    private fun shouldEscalateToHuman(message: String, intentType: IntentType, urgencyLevel: Int): Boolean {
        return when {
            urgencyLevel >= 4 -> true
            intentType == IntentType.COMPLAINT -> true
            message.contains("hablar con") && message.contains("persona") -> true
            message.contains("gerente") || message.contains("supervisor") -> true
            message.contains("cancelar") && message.contains("servicio") -> true
            else -> false
        }
    }
}