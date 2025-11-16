package com.example.crm_logistico_movil.models

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: String
)

enum class ChatTopic {
    GREETING,
    OPERATIONS,
    TRACKING,
    INVOICES,
    QUOTES,
    TECHNICAL_SUPPORT,
    CONTACT_INFO,
    UNKNOWN
}

data class ChatbotResponse(
    val message: String,
    val topic: ChatTopic = ChatTopic.UNKNOWN,
    val suggestions: List<String> = emptyList()
)