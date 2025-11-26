package com.example.crm_logistico_movil.navigation

sealed class Screen(val route: String) {
    // Autenticación
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    
    // Dashboard por roles
    object ClientDashboard : Screen("client_dashboard")
    object OperativeDashboard : Screen("operative_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object SalesDashboard : Screen("sales_dashboard")
    
    // Operaciones
    object OperationsList : Screen("operations_list")
    object OperationDetail : Screen("operation_detail/{operationId}") {
        fun createRoute(operationId: String) = "operation_detail/$operationId"
    }
    object CreateOperation : Screen("create_operation")
    object EditOperation : Screen("edit_operation/{operationId}") {
        fun createRoute(operationId: String) = "edit_operation/$operationId"
    }
    
    // Cotizaciones
    object QuotesList : Screen("quotes_list")
    object QuoteDetail : Screen("quote_detail/{quoteId}") {
        fun createRoute(quoteId: String) = "quote_detail/$quoteId"
    }
    object CreateQuoteRequest : Screen("create_quote_request")
    object QuoteRequests : Screen("quote_requests")
    
    // Facturas
    object InvoicesList : Screen("invoices_list")
    object InvoiceDetail : Screen("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: String) = "invoice_detail/$invoiceId"
    }
    object CreditNotes : Screen("credit_notes")
    
    // Incidencias y Demoras
    object IncidentsList : Screen("incidents_list")
    object IncidentDetail : Screen("incident_detail/{incidentId}") {
        fun createRoute(incidentId: String) = "incident_detail/$incidentId"
    }
    object CreateIncident : Screen("create_incident/{operationId}") {
        fun createRoute(operationId: String) = "create_incident/$operationId"
    }
    object DelaysList : Screen("delays_list")
    object CreateDelay : Screen("create_delay/{operationId}") {
        fun createRoute(operationId: String) = "create_delay/$operationId"
    }
    
    // Tracking
    object TrackingMap : Screen("tracking_map/{operationId}") {
        fun createRoute(operationId: String) = "tracking_map/$operationId"
    }
    object TrackingHistory : Screen("tracking_history/{operationId}") {
        fun createRoute(operationId: String) = "tracking_history/$operationId"
    }
    
    // Comunicación
    object Chat : Screen("chat/{operationId}") {
        fun createRoute(operationId: String) = "chat/$operationId"
    }
    object ChatList : Screen("chat_list")
    
    // Notificaciones y Soporte
    object Notifications : Screen("notifications")
    object Support : Screen("support")
    object AISupport : Screen("ai_support")
    object Feedback : Screen("feedback")
    
    // Clientes (para operativos/admin)
    object ClientsList : Screen("clients_list")
    object ClientDetail : Screen("client_detail/{clientId}") {
        fun createRoute(clientId: String) = "client_detail/$clientId"
    }
    
    // Documentos
    object DocumentsList : Screen("documents_list/{entityId}/{entityType}") {
        fun createRoute(entityId: String, entityType: String) = "documents_list/$entityId/$entityType"
    }
    
    // Configuración
    object Settings : Screen("settings")
}
