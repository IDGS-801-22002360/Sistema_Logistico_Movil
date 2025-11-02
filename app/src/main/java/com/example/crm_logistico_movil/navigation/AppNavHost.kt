package com.example.crm_logistico_movil.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.crm_logistico_movil.auth.ForgotPasswordScreen
import com.example.crm_logistico_movil.auth.LoginScreen
import com.example.crm_logistico_movil.auth.RegisterScreen
import com.example.crm_logistico_movil.client.ClientDashboardScreen
import com.example.crm_logistico_movil.operative.OperativeDashboardScreen
import com.example.crm_logistico_movil.admin.AdminDashboardScreen
import com.example.crm_logistico_movil.sales.SalesDashboardScreen
import com.example.crm_logistico_movil.screens.*

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        // Autenticación
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        // Dashboards por roles
        composable(Screen.ClientDashboard.route) {
            ClientDashboardScreen(navController = navController)
        }
        composable(Screen.OperativeDashboard.route) {
            OperativeDashboardScreen(navController = navController)
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
        composable(Screen.SalesDashboard.route) {
            SalesDashboardScreen(navController = navController)
        }

        // Operaciones
        composable(Screen.OperationsList.route) {
            OperationsListScreen(navController = navController)
        }
        composable(Screen.OperationDetail.route) { backStackEntry ->
            val operationId = backStackEntry.arguments?.getString("operationId")
            OperationDetailScreen(navController = navController, operationId = operationId)
        }
        composable(Screen.CreateOperation.route) {
            CreateOperationScreen(navController = navController)
        }
        composable(Screen.EditOperation.route) { backStackEntry ->
            val operationId = backStackEntry.arguments?.getString("operationId")
            EditOperationScreen(navController = navController, operationId = operationId)
        }

        // Cotizaciones
        composable(Screen.QuotesList.route) {
            QuotesListScreen(navController = navController)
        }
        composable(Screen.QuoteDetail.route) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getString("quoteId")
            QuoteDetailScreen(navController = navController, quoteId = quoteId)
        }
        composable(Screen.CreateQuoteRequest.route) {
            CreateQuoteRequestScreen(navController = navController)
        }
        composable(Screen.QuoteRequests.route) {
            QuoteRequestsScreen(navController = navController)
        }

        // Facturas
        composable(Screen.InvoicesList.route) {
            InvoicesListScreen(navController = navController)
        }
        composable(Screen.InvoiceDetail.route) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId")
            InvoiceDetailScreen(navController = navController, invoiceId = invoiceId)
        }
        composable(Screen.CreditNotes.route) {
            CreditNotesScreen(navController = navController)
        }

        // Incidencias y Demoras
        composable(Screen.IncidentsList.route) {
            IncidentsListScreen(navController = navController)
        }
        composable(Screen.IncidentDetail.route) { backStackEntry ->
            val incidentId = backStackEntry.arguments?.getString("incidentId")
            IncidentDetailScreen(navController = navController, incidentId = incidentId)
        }
        composable(Screen.CreateIncident.route) { backStackEntry ->
            val operationId = backStackEntry.arguments?.getString("operationId")
            CreateIncidentScreen(navController = navController, operationId = operationId)
        }
        composable(Screen.DelaysList.route) {
            DelaysListScreen(navController = navController)
        }
        composable(Screen.CreateDelay.route) { backStackEntry ->
            val operationId = backStackEntry.arguments?.getString("operationId")
            CreateDelayScreen(navController = navController, operationId = operationId)
        }

        // Tracking
        composable(Screen.TrackingMap.route) { backStackEntry ->
            val operationId = backStackEntry.arguments?.getString("operationId")
            TrackingMapScreen(navController = navController, operationId = operationId)
        }
        composable(Screen.TrackingHistory.route) { backStackEntry ->
            val operationId = backStackEntry.arguments?.getString("operationId")
            TrackingHistoryScreen(navController = navController, operationId = operationId)
        }

        // Comunicación
        composable(Screen.Chat.route) { backStackEntry ->
            val operationId = backStackEntry.arguments?.getString("operationId")
            ChatScreen(navController = navController, operationId = operationId)
        }
        composable(Screen.ChatList.route) {
            ChatListScreen(navController = navController)
        }

        // Notificaciones y Soporte
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
        composable(Screen.Support.route) {
            SupportScreen(navController = navController)
        }
        composable(Screen.Feedback.route) {
            FeedbackScreen(navController = navController)
        }

        // Perfil y Configuración
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // Clientes (para operativos/admin)
        composable(Screen.ClientsList.route) {
            ClientsListScreen(navController = navController)
        }
        composable(Screen.ClientDetail.route) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId")
            ClientDetailScreen(navController = navController, clientId = clientId)
        }

        // Documentos
        composable(Screen.DocumentsList.route) { backStackEntry ->
            val entityId = backStackEntry.arguments?.getString("entityId")
            val entityType = backStackEntry.arguments?.getString("entityType")
            DocumentsListScreen(navController = navController, entityId = entityId, entityType = entityType)
        }
    }
}
