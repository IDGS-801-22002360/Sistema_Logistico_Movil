package com.example.crm_logistico_movil.models



data class ChatContext(

    val clientId: String = "",

    var clientName: String = "",

    var hasActiveOperations: Boolean = false,

    var pendingInvoices: Int = 0,

    var overdueInvoices: Int = 0,

    val lastOperationStatus: String = "",

    val totalOperationsThisMonth: Int = 0,

    val averageDeliveryTime: Double = 0.0,

    val preferredLanguage: String = "es",

    var lastInteractionTopic: ChatTopic? = null,

    val conversationFlow: MutableList<String> = mutableListOf(),

    val mentionedOperations: MutableList<String> = mutableListOf(),

    val currentIssues: MutableList<String> = mutableListOf()

)



data class SmartResponse(

    val message: String,

    val topic: ChatTopic,

    val confidence: Float,

    val suggestedActions: List<String> = emptyList(),

    val requiresFollowUp: Boolean = false,

    val contextUpdates: Map<String, Any> = emptyMap()

)



enum class IntentType {

    GREETING,

    QUESTION,

    COMPLAINT,

    REQUEST,

    INFORMATION_SEEKING,

    FOLLOW_UP,

    GRATITUDE,

    FAREWELL,

    URGENT,

    CLARIFICATION

}



data class MessageIntent(

    val type: IntentType,

    val entities: Map<String, String> = emptyMap(),

    val confidence: Float = 0.0f,

    val urgencyLevel: Int = 1, // 1-5 scale

    val requiresHumanEscalation: Boolean = false

)