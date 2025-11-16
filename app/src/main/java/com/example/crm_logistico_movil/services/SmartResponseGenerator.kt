package com.example.crm_logistico_movil.services

import com.example.crm_logistico_movil.models.*

class SmartResponseGenerator {

    fun generateResponse(
        intent: MessageIntent,
        context: ChatContext,
        userMessage: String
    ): SmartResponse {

        return when (intent.type) {
            IntentType.GREETING -> generateGreeting(intent, context)
            IntentType.QUESTION -> generateQuestionResponse(intent, context, userMessage)
            IntentType.REQUEST -> generateRequestResponse(intent, context, userMessage)
            IntentType.COMPLAINT -> generateComplaintResponse(intent, context, userMessage)
            IntentType.GRATITUDE -> generateGratitudeResponse(intent, context)
            IntentType.FAREWELL -> generateFarewellResponse(intent, context)
            IntentType.FOLLOW_UP -> generateFollowUpResponse(intent, context, userMessage)
            IntentType.URGENT -> generateUrgentResponse(intent, context, userMessage)
            IntentType.CLARIFICATION -> generateClarificationResponse(intent, context, userMessage)
            else -> generateInformationResponse(intent, context, userMessage)
        }
    }

    private fun generateGreeting(intent: MessageIntent, context: ChatContext): SmartResponse {
        val clientName = if (context.clientName.isNotEmpty()) context.clientName else "estimado cliente"

        val personalizedGreeting = when {
            context.hasActiveOperations && context.pendingInvoices > 0 ->
                "¡Hola $clientName! 👋 Veo que tienes ${context.totalOperationsThisMonth} operaciones activas y ${context.pendingInvoices} factura(s) pendiente(s). ¿En qué puedo ayudarte hoy?"

            context.hasActiveOperations ->
                "¡Buen día $clientName! 🚚 Tienes ${context.totalOperationsThisMonth} operaciones en curso. ¿Necesitas información sobre alguna en particular?"

            context.overdueInvoices > 0 ->
                "Hola $clientName, te saludo cordialmente. Noto que tienes ${context.overdueInvoices} factura(s) vencida(s). ¿Puedo ayudarte con información de pagos?"

            else ->
                "¡Hola $clientName! 😊 Es un placer atenderte. ¿En qué puedo asistirte hoy?"
        }

        val actions = mutableListOf<String>()
        if (context.hasActiveOperations) actions.add("Ver estado de operaciones")
        if (context.pendingInvoices > 0) actions.add("Revisar facturas pendientes")
        actions.add("Solicitar nueva cotización")

        return SmartResponse(
            message = personalizedGreeting,
            topic = ChatTopic.GREETING,
            confidence = intent.confidence,
            suggestedActions = actions,
            requiresFollowUp = context.hasActiveOperations || context.pendingInvoices > 0
        )
    }

    private fun generateQuestionResponse(intent: MessageIntent, context: ChatContext, userMessage: String): SmartResponse {
        val lowerMessage = userMessage.lowercase()

        return when {
            // Preguntas sobre operaciones específicas
            intent.entities.containsKey("operation_id") -> {
                val opId = intent.entities["operation_id"]!!
                SmartResponse(
                    message = "📦 Sobre la operación $opId:\n\n🔍 Ve a 'Operaciones' > Buscar '$opId'\n📊 Ahí encontrarás:\n• Estado actual en tiempo real\n• Documentos completos\n• Tracking con GPS\n• Fechas estimadas vs reales\n• Info del transportista\n\n¿Necesitas algo específico de esta operación?",
                    topic = ChatTopic.OPERATIONS,
                    confidence = 0.9f,
                    suggestedActions = listOf("Ver operación $opId", "Tracking en tiempo real", "Contactar transportista"),
                    requiresFollowUp = true,
                    contextUpdates = mapOf("mentioned_operation" to opId)
                )
            }

            // Preguntas sobre tiempo de entrega
            lowerMessage.contains("cuándo") && (lowerMessage.contains("llega") || lowerMessage.contains("entrega")) -> {
                val message = if (context.hasActiveOperations) {
                    "⏰ Para saber cuándo llegan tus envíos:\n\n📱 Ve a 'Operaciones' en el menú\n🎯 Cada operación muestra:\n• Fecha estimada de entrega\n• Progreso en tiempo real\n• Última actualización GPS\n\n📊 Tu promedio histórico de entrega es ${context.averageDeliveryTime} días.\n\n¿Hay alguna operación específica que te preocupe?"
                } else {
                    "⏰ Los tiempos de entrega varían según:\n\n📍 Origen y destino\n📦 Tipo de carga\n🚛 Modalidad de transporte\n🛂 Trámites aduanales (si aplica)\n\n💡 En promedio manejamos:\n• Nacional: 2-5 días\n• Internacional: 7-15 días\n\n¿Necesitas cotizar un envío específico?"
                }

                SmartResponse(
                    message = message,
                    topic = ChatTopic.TRACKING,
                    confidence = 0.8f,
                    suggestedActions = listOf("Ver operaciones activas", "Solicitar cotización", "Calcular tiempo de entrega")
                )
            }

            // Preguntas sobre costos
            lowerMessage.contains("costo") || lowerMessage.contains("precio") || lowerMessage.contains("cuánto") -> {
                SmartResponse(
                    message = "💰 Sobre costos y precios:\n\n📝 Los precios dependen de:\n• Peso y dimensiones\n• Distancia y destino\n• Tipo de servicio (estándar/express)\n• Valor declarado del producto\n\n💡 Para obtener un precio exacto:\n1️⃣ Ve a 'Solicitar Cotización'\n2️⃣ Completa los datos del envío\n3️⃣ Recibe respuesta en máximo 2 horas\n\n🎯 También puedes revisar tus facturas históricas para referencias de precio.",
                    topic = ChatTopic.QUOTES,
                    confidence = 0.8f,
                    suggestedActions = listOf("Solicitar cotización", "Ver facturas históricas", "Contactar comercial"),
                    requiresFollowUp = true
                )
            }

            // Preguntas sobre documentos
            lowerMessage.contains("documento") || lowerMessage.contains("papel") || lowerMessage.contains("certificado") -> {
                SmartResponse(
                    message = "📋 Sobre documentación:\n\n✅ En cada operación encuentras:\n• Guía de envío\n• Factura comercial\n• Lista de empaque\n• Certificados de origen (si aplica)\n• Seguros de carga\n\n📱 Todos están disponibles en PDF desde la app.\n\n🔒 Tu información está 100% segura con encriptación de grado bancario.\n\n¿Necesitas algún documento específico?",
                    topic = ChatTopic.OPERATIONS,
                    confidence = 0.8f,
                    suggestedActions = listOf("Ver documentos", "Descargar PDFs", "Contactar soporte documental")
                )
            }

            else -> generateGeneralQuestionResponse(lowerMessage, context)
        }
    }

    private fun generateGeneralQuestionResponse(message: String, context: ChatContext): SmartResponse {
        val responses = listOf(
            "🤔 Es una excelente pregunta. Te puedo ayudar de manera más específica si me das más detalles.\n\n💡 ¿Te refieres a algo sobre:\n• Tus operaciones actuales\n• Facturas o pagos\n• Nuevas cotizaciones\n• Uso de la aplicación\n\n¿Podrías ser más específico?",
            "🧠 Entiendo tu consulta. Para darte la mejor respuesta posible:\n\n📋 ¿Es sobre alguna operación en particular?\n💰 ¿Necesitas información financiera?\n🚚 ¿Dudas sobre servicios logísticos?\n📱 ¿Problemas con la app?\n\nCuéntame más detalles por favor.",
            "💭 Tu pregunta es interesante. Me ayudarías mucho si me dices:\n\n🎯 ¿En qué área específica necesitas ayuda?\n📊 ¿Hay algún número de operación o factura?\n⏰ ¿Es algo urgente?\n\n¡Entre más detalles me des, mejor te podré ayudar!"
        )

        return SmartResponse(
            message = responses.random(),
            topic = ChatTopic.UNKNOWN,
            confidence = 0.6f,
            suggestedActions = listOf("Especificar consulta", "Ver menú de opciones", "Contactar agente humano"),
            requiresFollowUp = true
        )
    }

    private fun generateRequestResponse(intent: MessageIntent, context: ChatContext, userMessage: String): SmartResponse {
        // Implementación para solicitudes específicas
        val lowerMessage = userMessage.lowercase()

        return when {
            lowerMessage.contains("cotización") || lowerMessage.contains("cotizar") -> {
                SmartResponse(
                    message = "📝 ¡Perfecto! Para solicitar tu cotización:\n\n1️⃣ Ve a 'Solicitar Cotización' en el menú\n2️⃣ Completa la información:\n   • Origen y destino\n   • Peso y dimensiones\n   • Tipo de mercancía\n   • Fecha de envío\n\n⚡ Nuestro equipo comercial responde en menos de 2 horas.\n\n💡 Tip: Si tienes fotos de la carga, adjúntalas para una cotización más precisa.",
                    topic = ChatTopic.QUOTES,
                    confidence = 0.9f,
                    suggestedActions = listOf("Ir a solicitar cotización", "Ver cotizaciones anteriores", "Contactar comercial"),
                    requiresFollowUp = true
                )
            }

            lowerMessage.contains("cancelar") -> {
                SmartResponse(
                    message = "⚠ Entiendo que necesitas cancelar algo. Para ayudarte mejor:\n\n🔍 ¿Qué necesitas cancelar?\n• ¿Una operación en curso?\n• ¿Una cotización pendiente?\n• ¿Un servicio programado?\n\n📞 Para cancelaciones, también puedes contactar directamente:\n📱 WhatsApp: +52 33 9876 5432\n📧 operaciones@logicorp.com\n\n⏰ Horario: 24/7 para urgencias",
                    topic = ChatTopic.TECHNICAL_SUPPORT,
                    confidence = 0.8f,
                    suggestedActions = listOf("Contactar operaciones", "Ver políticas de cancelación", "Especificar qué cancelar"),
                    requiresFollowUp = true
                )
            }

            else -> {
                SmartResponse(
                    message = "✅ Entiendo tu solicitud. Para atenderte de la mejor manera:\n\n📋 ¿Podrías ser más específico sobre lo que necesitas?\n💡 Te puedo ayudar con:\n• Información de operaciones\n• Trámites y documentos\n• Cotizaciones y precios\n• Soporte técnico\n\n🚀 ¡Estoy aquí para resolver lo que necesites!",
                    topic = ChatTopic.UNKNOWN,
                    confidence = 0.7f,
                    suggestedActions = listOf("Especificar solicitud", "Ver opciones de servicio", "Contactar agente"),
                    requiresFollowUp = true
                )
            }
        }
    }

    private fun generateComplaintResponse(intent: MessageIntent, context: ChatContext, userMessage: String): SmartResponse {
        return SmartResponse(
            message = "😔 Lamento mucho que tengas esta experiencia. Tu satisfacción es nuestra prioridad.\n\n🔧 Para resolver tu situación:\n\n1️⃣ He escalado tu caso inmediatamente\n2️⃣ Un supervisor te contactará en máximo 30 minutos\n3️⃣ Mientras tanto, cuéntame más detalles\n\n📞 También puedes llamar directamente:\n🆘 Línea de quejas: +52 33 9999 0000\n📧 quejas@logicorp.com\n\n💪 ¡Vamos a solucionar esto juntos!",
            topic = ChatTopic.TECHNICAL_SUPPORT,
            confidence = 0.9f,
            suggestedActions = listOf("Escalación inmediata", "Contacto supervisor", "Documentar queja"),
            requiresFollowUp = true,
            contextUpdates = mapOf("escalated" to true, "complaint_registered" to true)
        )
    }

    private fun generateGratitudeResponse(intent: MessageIntent, context: ChatContext): SmartResponse {
        val responses = listOf(
            "😊 ¡Me alegra muchísimo haberte ayudado! Para eso estamos aquí.\n\n🌟 Si necesitas cualquier cosa más, no dudes en escribirme.\n\n💡 Recuerda que puedes:\n• Consultar tus operaciones 24/7\n• Solicitar cotizaciones\n• Contactar soporte directo\n\n¡Que tengas un excelente día!",
            "🎉 ¡Es un placer poder asistirte! Tu satisfacción es nuestro éxito.\n\n💪 Estamos aquí 24/7 para lo que necesites.\n\n⭐ Si tienes un momento, nos encantaría tu opinión en las tiendas de apps.\n\n¿Hay algo más en lo que pueda ayudarte?",
            "🙏 ¡Gracias por tus palabras! Me motiva mucho saber que te fue útil.\n\n🚀 LogiCorp se esfuerza cada día por brindarte el mejor servicio.\n\n📱 Recuerda que esta app se actualiza constantemente con nuevas funciones.\n\n¿Te gustaría saber sobre alguna función nueva?"
        )

        return SmartResponse(
            message = responses.random(),
            topic = ChatTopic.GREETING,
            confidence = 0.9f,
            suggestedActions = listOf("Calificar app", "Ver funciones nuevas", "Continuar conversación"),
            requiresFollowUp = false
        )
    }

    private fun generateFarewellResponse(intent: MessageIntent, context: ChatContext): SmartResponse {
        val clientName = if (context.clientName.isNotEmpty()) context.clientName else ""

        val personalizedFarewell = if (context.hasActiveOperations) {
            "👋 ¡Hasta pronto $clientName! Estaré aquí para cualquier consulta sobre tus operaciones.\n\n📱 Recuerda que puedes:\n• Ver tracking en tiempo real\n• Recibir notificaciones automáticas\n• Contactarme 24/7\n\n🚚 ¡Que todo llegue perfecto! Cuídate."
        } else {
            "👋 ¡Nos vemos $clientName! Ha sido un placer ayudarte.\n\n🌟 LogiCorp está aquí cuando nos necesites.\n\n💡 Para tu próximo envío, recuerda que puedes solicitar cotizaciones directamente desde la app.\n\n¡Excelente día!"
        }

        return SmartResponse(
            message = personalizedFarewell,
            topic = ChatTopic.GREETING,
            confidence = 0.9f,
            suggestedActions = listOf(),
            requiresFollowUp = false
        )
    }

    private fun generateFollowUpResponse(intent: MessageIntent, context: ChatContext, userMessage: String): SmartResponse {
        val lastTopic = context.lastInteractionTopic

        return when (lastTopic) {
            ChatTopic.OPERATIONS -> SmartResponse(
                message = "📦 Perfecto, continúo con tu consulta de operaciones.\n\n¿Necesitas ayuda con:\n• Estado específico de algún envío\n• Documentos adicionales\n• Información del transportista\n• Cambios en la entrega\n\n¿Qué más puedo explicarte?",
                topic = ChatTopic.OPERATIONS,
                confidence = 0.8f,
                suggestedActions = listOf("Estado de envíos", "Documentos", "Info transportista"),
                requiresFollowUp = true
            )

            else -> SmartResponse(
                message = "➕ ¡Por supuesto! Me da gusto que quieras saber más.\n\n🎯 Además de lo que ya conversamos, también puedo ayudarte con:\n• Reportar incidencias\n• Configurar notificaciones\n• Información de servicios\n• Contactos específicos por área\n\n¿Qué te interesa explorar?",
                topic = ChatTopic.UNKNOWN,
                confidence = 0.7f,
                suggestedActions = listOf("Explorar servicios", "Configurar app", "Ver contactos"),
                requiresFollowUp = true
            )
        }
    }

    private fun generateUrgentResponse(intent: MessageIntent, context: ChatContext, userMessage: String): SmartResponse {
        return SmartResponse(
            message = "🚨 Entiendo que es urgente. Voy a escalarlo inmediatamente.\n\n⚡ Acciones inmediatas:\n1️⃣ Caso escalado a supervisor\n2️⃣ Notificación a operaciones\n3️⃣ Seguimiento prioritario\n\n📞 Para atención inmediata:\n🆘 Emergencias 24/7: +52 33 9999 0000\n📱 WhatsApp urgente: +52 33 8888 7777\n\n💪 ¡Vamos a resolver esto ya!",
            topic = ChatTopic.TECHNICAL_SUPPORT,
            confidence = 0.95f,
            suggestedActions = listOf("Llamar emergencias", "WhatsApp urgente", "Escalar caso"),
            requiresFollowUp = true,
            contextUpdates = mapOf("urgent_case" to true, "escalated" to true)
        )
    }

    private fun generateClarificationResponse(intent: MessageIntent, context: ChatContext, userMessage: String): SmartResponse {
        return SmartResponse(
            message = "🤔 ¡Por supuesto! Te explico mejor.\n\n💡 Me puedes preguntar sobre:\n\n📦 *Operaciones: Estados, tracking, documentos\n💰 **Facturas: Pagos, vencimientos, métodos\n📝 **Cotizaciones: Precios, servicios, tiempos\n🛠 **Soporte: Problemas técnicos, dudas\n📞 **Contactos*: Teléfonos, emails, horarios\n\n¿Sobre cuál tema necesitas que sea más específico?",
            topic = ChatTopic.UNKNOWN,
            confidence = 0.8f,
            suggestedActions = listOf("Elegir tema", "Hacer pregunta específica", "Ver menú completo"),
            requiresFollowUp = true
        )
    }

    private fun generateInformationResponse(intent: MessageIntent, context: ChatContext, userMessage: String): SmartResponse {
        return SmartResponse(
            message = "ℹ Te proporciono la información que necesitas.\n\n📚 LogiCorp te ofrece:\n\n🚚 *Servicios logísticos completos\n📱 **App móvil 24/7\n🌎 **Cobertura nacional e internacional\n🔒 **Seguridad garantizada\n📊 **Tracking en tiempo real*\n\n¿Hay algo específico que te interese conocer más a fondo?",
            topic = ChatTopic.UNKNOWN,
            confidence = 0.7f,
            suggestedActions = listOf("Ver servicios", "Conocer cobertura", "Info de seguridad"),
            requiresFollowUp = true
        )
    }
}