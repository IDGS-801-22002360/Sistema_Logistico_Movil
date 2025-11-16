# 🤖 Chatbot de Soporte LogiCorp

## 📋 Descripción

Chatbot inteligente integrado en la aplicación móvil para brindar soporte automatizado 24/7 a los usuarios. El bot puede responder consultas sobre operaciones, tracking, facturas, cotizaciones y soporte técnico.

## 🎯 Características Principales

### ✅ _Funcionalidades Implementadas:_

1. _Interfaz de Chat Nativa_

   - Diseño moderno con Material 3
   - Indicador de "escribiendo..."
   - Scrolling automático
   - Avatares para usuario y bot
   - Timestamps en mensajes

2. _IA Conversacional_

   - Reconocimiento de intención por palabras clave
   - Respuestas contextuales inteligentes
   - Manejo de múltiples temas
   - Respuestas de fallback

3. _Temas Soportados:_
   - ✅ Saludos y despedidas
   - ✅ Consultas sobre operaciones
   - ✅ Tracking y seguimiento
   - ✅ Facturas y pagos
   - ✅ Cotizaciones
   - ✅ Soporte técnico
   - ✅ Información de contacto

## 🚀 Cómo Funciona

### _Arquitectura:_

SupportScreen -> ChatbotViewModel -> ChatMessage Models

### _Flujo de Conversación:_

1. Usuario escribe mensaje
2. ViewModel analiza palabras clave
3. Clasifica la intención (ChatTopic)
4. Genera respuesta apropiada
5. Simula tiempo de "escritura"
6. Muestra respuesta del bot

## 🛠 Archivos Implementados

app/src/main/java/com/example/crm_logistico_movil/
├── models/ChatModels.kt # Modelos de datos del chat
├── viewmodels/ChatbotViewModel.kt # Lógica del chatbot
└── screens/CommonScreens.kt # UI del chat actualizada

## 📱 Compatibilidad

✅ _Emulador Android_
✅ _Dispositivos físicos Android_
✅ _Material Design 3_
✅ _Jetpack Compose_

## 🔧 Cómo Expandir el Chatbot

### _Agregar Nuevas Palabras Clave:_

kotlin
// En ChatbotViewModel.kt, función generateBotResponse()
containsAny(lowerMessage, listOf("nueva", "palabra", "clave")) -> {
ChatbotResponse(
message = "Tu respuesta personalizada aquí",
topic = ChatTopic.TU_TEMA
)
}

### _Agregar Nuevos Temas:_

kotlin
// En ChatModels.kt
enum class ChatTopic {
// ...existentes...
NUEVO_TEMA,
OTRO_TEMA
}

### _Integrar con APIs Reales:_

kotlin
// Ejemplo: Consultar operación por ID
if (lowerMessage.contains("operación") && containsNumber(lowerMessage)) {
val operationId = extractNumber(lowerMessage)
val operation = repository.getOperation(operationId)
// Generar respuesta con datos reales
}

## 🎨 Personalización Visual

### _Colores del Chat:_

- _Usuario_: MaterialTheme.colorScheme.primary
- _Bot_: MaterialTheme.colorScheme.surface
- _Estado en línea_: Color(0xFF4CAF50)

### _Iconos:_

- _Bot_: Icons.Default.SmartToy
- _Usuario_: Icons.Default.Person
- _Enviar_: Icons.Default.Send

## 🚀 Próximas Mejoras Sugeridas

1. _Integración con APIs:_

   - Consultar operaciones reales por ID
   - Estado de facturas en tiempo real
   - Tracking GPS actual

2. _IA Mejorada:_

   - Integración con OpenAI/ChatGPT
   - Análisis de sentimientos
   - Historial de conversaciones

3. _Funcionalidades Avanzadas:_

   - Botones de respuesta rápida
   - Envío de imágenes/documentos
   - Notificaciones push del chat

4. _Analytics:_
   - Métricas de satisfacción
   - Temas más consultados
   - Tiempo de resolución

## 📞 Información de Contacto (Personalizable)

kotlin
// Actualizar en ChatbotViewModel.kt
📧 Email: contacto@logicorp.com
📞 Teléfono: +52 33 1234 5678
📱 WhatsApp: +52 33 9876 5432
🏢 Oficina: Av. Chapultepec 123, Guadalajara, Jalisco

## ✨ Ejemplo de Uso

_Usuario:_ "Hola, ¿puedo ver el tracking de mi operación?"

_Bot:_ "¡Hola! Para el seguimiento de tu envío puedes:
• Ver la ubicación actual en el mapa dentro de los detalles de la operación
• Consultar el historial completo de movimientos
• Recibir actualizaciones en tiempo real

¿Necesitas rastrear alguna operación en particular?"

---

💡 _Nota_: Este chatbot está diseñado para funcionar completamente offline con respuestas predefinidas inteligentes. Para funcionalidades más avanzadas, se puede integrar con servicios de IA en la nube.
