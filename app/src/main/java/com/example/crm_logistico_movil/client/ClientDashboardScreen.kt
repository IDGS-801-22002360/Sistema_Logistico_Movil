package com.example.crm_logistico_movil.client

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.crm_logistico_movil.R
import com.example.crm_logistico_movil.components.TopAppBar
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.navigation.Screen
import com.example.crm_logistico_movil.ui.theme.CRM_Logistico_MovilTheme
import com.example.crm_logistico_movil.utils.getStatusColor
import com.example.crm_logistico_movil.utils.getStatusText
import com.example.crm_logistico_movil.viewmodels.AuthViewModel
import com.example.crm_logistico_movil.viewmodels.ClientDashboardViewModel
import com.example.crm_logistico_movil.viewmodels.NotificationViewModel
import com.example.crm_logistico_movil.services.NotificationService

// ============================================================================
// ASUNCIONES DE CLASES DE MODELO: DEBES TENER ESTAS DEFINICIONES EN models.kt
// (o donde sea que definas tus modelos)
// ============================================================================
// Si estos modelos no están definidos con estos campos, el código fallará.
// Por favor, verifica y ajusta tus modelos si es necesario.

// Use canonical models from com.example.crm_logistico_movil.models (no local duplicates)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDashboardScreen(
    navController: NavController,
    notificationViewModel: NotificationViewModel,
    clientViewModel: ClientDashboardViewModel = viewModel()
) {
    val uiState by clientViewModel.uiState.collectAsState()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val notificationState by notificationViewModel.uiState.collectAsState()

    // Inicializar el servicio de notificaciones
    LaunchedEffect(Unit) {
        NotificationService.initialize(notificationViewModel)
    }

    // Cargar datos cuando se obtiene el usuario actual
    LaunchedEffect(authState.currentUser?.id_usuario) {
        authState.currentUser?.id_usuario?.let { clientId ->
            clientViewModel.loadDashboardData(clientId)

        }
    }

    Scaffold( // Usamos Scaffold para la AppBar y el contenido
        topBar = {
            TopAppBar(
                title = "Bienvenido, ${authState.currentUser?.nombre ?: "Usuario"}",
                showBackButton = false,
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.Notifications.route) }
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones"
                            )
                            // Badge de notificaciones no leídas
                            if (notificationState.unreadCount > 0) {
                                Badge(
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = if (notificationState.unreadCount > 99) "99+" else notificationState.unreadCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.Profile.route) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Botón temporal para probar notificaciones
            FloatingActionButton(
                onClick = {
                    authState.currentUser?.id_usuario?.let { clientId ->
                        Log.d("ClientDashboard", "Testing notification creation for client: $clientId")
                        val timestamp = System.currentTimeMillis()

                        // Probar diferentes tipos de notificaciones
                        NotificationService.notifySolicitudCreated("SOL-TEST-$timestamp", clientId)
                        NotificationService.notifyOperacionAssigned("OP-TEST-$timestamp", clientId)
                        NotificationService.notifyFacturaGenerated("FAC-TEST-$timestamp", clientId, "$1,500.00 USD")
                        NotificationService.notifyFacturaVencimiento("FAC-TEST-$timestamp", clientId, 5)
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Test Notifications")
            }
        }
    ) { paddingValues -> // paddingValues es importante para que el contenido no se solape con la TopAppBar
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // Aplicar padding aquí
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // Aplicar padding aquí
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WelcomeCard(uiState.clientSummary)
                }

                item {
                    QuickActionsRow(navController)
                }

                item {
                    StatsCards(uiState.clientSummary)
                }

                if (uiState.recentOperations.isNotEmpty()) {
                    item {
                        RecentOperationsCard(uiState.recentOperations, navController)
                    }
                }

                // Aquí se usa uiState.quoteRequests, que debería ser List<SolicitudCotizacionExtended>
                if (uiState.quoteRequests.isNotEmpty()) {
                    item {
                        QuoteRequestsCard(uiState.quoteRequests, navController)
                    }
                }

                if (uiState.recentInvoices.isNotEmpty()) {
                    item {
                        RecentInvoicesCard(uiState.recentInvoices, navController)
                    }
                }

                // Mostrar mensaje de error si existe
                uiState.errorMessage?.let { message ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(summary: com.example.crm_logistico_movil.models.ClientSummary?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Bienvenido de vuelta!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (summary != null) {
                Text(
                    text = buildString {
                        if (summary.operacionesActivas > 0) {
                            append("Tienes ${summary.operacionesActivas} operaciones activas")
                        }
                        if (summary.cotizacionesPendientes > 0) {
                            if (isNotEmpty()) append(" y ")
                            append("${summary.cotizacionesPendientes} cotizaciones pendientes")
                        }
                        if (summary.solicitudesPendientes > 0) {
                            if (isNotEmpty()) append(", ")
                            append("${summary.solicitudesPendientes} solicitudes en proceso")
                        }
                        if (isEmpty()) append("Todo al día. No hay operaciones pendientes.")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = "Cargando información...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(navController: NavController) {
    Column {
        Text(
            text = "Acciones Rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                listOf(
                    QuickAction(
                        title = "Ver Operaciones",
                        icon = Icons.Default.LocalShipping,
                        color = Color(0xFF1976D2),
                        onClick = { navController.navigate(Screen.OperationsList.route) }
                    ),
                    QuickAction(
                        title = "Solicitar Cotización",
                        icon = Icons.Default.RequestQuote,
                        color = Color(0xFF388E3C),
                        onClick = { navController.navigate(Screen.CreateQuoteRequest.route) }
                    ),
                    QuickAction(
                        title = "Ver Facturas",
                        icon = Icons.Default.Receipt,
                        color = Color(0xFFF57C00),
                        onClick = { navController.navigate(Screen.InvoicesList.route) }
                    ),
                    QuickAction(
                        title = "ChatBot",
                        icon = Icons.Default.SmartToy,
                        color = Color(0xFF7B1FA2),
                        onClick = { navController.navigate(Screen.Support.route) }
                    ),
                    QuickAction(
                        title = "Soporte IA",
                        icon = Icons.Default.Psychology,
                        color = Color(0xFF2E7D32),
                        onClick = { navController.navigate(Screen.AISupport.route) }
                    )
                )
            ) { action ->
                QuickActionCard(action)
            }
        }
    }
}

@Composable
private fun QuickActionCard(action: QuickAction) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        onClick = action.onClick,
        colors = CardDefaults.cardColors(
            containerColor = action.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = action.color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.labelSmall,
                color = action.color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatsCards(summary: com.example.crm_logistico_movil.models.ClientSummary?) {
    Column {
        Text(
            text = "Resumen",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Operaciones Activas",
                value = summary?.operacionesActivas?.toString() ?: "-",
                icon = Icons.Default.LocalShipping,
                color = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Facturas Pendientes",
                value = summary?.facturasPendientes?.toString() ?: "-",
                icon = Icons.Default.Receipt,
                color = Color(0xFFF57C00),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Cotizaciones Pendientes",
                value = summary?.cotizacionesPendientes?.toString() ?: "-",
                icon = Icons.Default.Assignment,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Solicitudes Pendientes",
                value = summary?.solicitudesPendientes?.toString() ?: "-",
                icon = Icons.Default.PendingActions,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RecentOperationsCard(
    operations: List<com.example.crm_logistico_movil.models.OperationExtended>,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Operaciones Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { navController.navigate(Screen.OperationsList.route) }
                ) {
                    Text("Ver todas")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            operations.take(3).forEach { operation ->
                OperationItem(
                    operation = operation,
                    onClick = {
                        // Asegúrate de que Screen.OperationDetail.createRoute exista y acepte el id
                        navController.navigate(
                            Screen.OperationDetail.createRoute(operation.id_operacion)
                        )
                    }
                )
                if (operation != operations.take(3).last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OperationItem(
    operation: com.example.crm_logistico_movil.models.OperationExtended,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (operation.tipo_servicio?.uppercase()) {
                "MARITIMO" -> Icons.Default.DirectionsBoat
                "AEREO" -> Icons.Default.Flight
                "TERRESTRE" -> Icons.Default.LocalShipping
                else -> Icons.Default.LocalShipping
            },
            contentDescription = null,
            tint = getStatusColor(operation.estatus ?: ""),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = operation.id_operacion,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = getStatusText(operation.estatus ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = getStatusColor(operation.estatus ?: "")
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// Status utils are now in com.example.crm_logistico_movil.utils.StatusUtils

private fun formatCurrency(amount: Double): String {
    return "$${String.format("%,.2f", amount)}"
}

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuoteRequestsCard(
    quoteRequests: List<SolicitudCotizacionExtended>, // Confirmado: usa SolicitudCotizacionExtended
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solicitudes de Cotización",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { navController.navigate("new_quote_request") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (quoteRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay solicitudes pendientes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quoteRequests.take(3)) { quote -> // 'quote' es SolicitudCotizacionExtended
                        QuoteRequestItem(
                            quote = quote,
                            onClick = { navController.navigate("quote_detail/${quote.id_solicitud}") }
                        )
                    }
                }

                if (quoteRequests.size > 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { navController.navigate("quote_requests") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver todas (${quoteRequests.size})")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteRequestItem(
    quote: SolicitudCotizacionExtended, // Confirmado: recibe SolicitudCotizacionExtended
    onClick: () -> Unit
) {
    android.util.Log.d("ClientDashboard", "QuoteRequestItem data: origen_pais=${quote.origen_pais}, destino_pais=${quote.destino_pais}, origen_ciudad=${quote.origen_ciudad}, destino_ciudad=${quote.destino_ciudad}")
    Card(
            modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${quote.origen_pais} → ${quote.destino_pais}", // usar campos del modelo ClientModels.kt
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Solicitada: ${quote.fecha_solicitud}", // usar campos del modelo ClientModels.kt
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = when (quote.estatus) { // usar campo estatus
                    "PENDIENTE" -> Color(0xFFF57C00)
                    "PROCESADA" -> Color(0xFF388E3C)
                    "RECHAZADA" -> Color(0xFFD32F2F)
                    else -> MaterialTheme.colorScheme.primary
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = quote.estatus, // usar campos del modelo ClientModels.kt
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun RecentInvoicesCard(
    invoices: List<FacturaExtended>, // Confirmado: recibe FacturaExtended
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Facturas Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { navController.navigate(Screen.InvoicesList.route) }
                ) {
                    Text("Ver todas")
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (invoices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay facturas recientes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(invoices.take(3)) { invoice -> // 'invoice' es FacturaExtended
                        InvoiceItem(
                            invoice = invoice,
                            onClick = { navController.navigate(Screen.InvoiceDetail.createRoute(invoice.id_factura_cliente)) } // usar id_factura_cliente
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceItem(
    invoice: FacturaExtended, // Confirmado: recibe FacturaExtended
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Factura #${invoice.numero_factura}", // usar campos del modelo ClientModels.kt
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = invoice.fecha_emision, // usar campos del modelo ClientModels.kt
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(invoice.monto_total), // usar campos del modelo ClientModels.kt
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = when (invoice.estatus) { // usar campo estatus
                        "PAGADA" -> Color(0xFF388E3C)
                        "PENDIENTE" -> Color(0xFFF57C00)
                        "VENCIDA" -> Color(0xFFD32F2F)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = invoice.estatus, // usar campos del modelo ClientModels.kt
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewClientDashboardScreen() {
    CRM_Logistico_MovilTheme {
        ClientDashboardScreen(
            navController = rememberNavController(),
            notificationViewModel = viewModel()
        )
    }
}