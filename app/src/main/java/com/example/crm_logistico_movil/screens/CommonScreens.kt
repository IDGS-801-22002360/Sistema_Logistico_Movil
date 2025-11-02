package com.example.crm_logistico_movil.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.crm_logistico_movil.components.TopAppBar
import com.example.crm_logistico_movil.components.SearchTextField
import com.example.crm_logistico_movil.dummy.DummyData
import com.example.crm_logistico_movil.models.*

// NOTIFICACIONES SCREEN
@Composable
fun NotificationsScreen(navController: NavController) {
    val notifications = remember {
        listOf(
            NotificationItem("Nueva operación OP004 asignada", "Hace 5 minutos", Icons.Default.LocalShipping, Color(0xFF1976D2)),
            NotificationItem("Factura INV-123 próxima a vencer", "Hace 1 hora", Icons.Default.Warning, Color(0xFFF57C00)),
            NotificationItem("Incidencia reportada en OP001", "Hace 2 horas", Icons.Default.Error, Color(0xFFE91E63)),
            NotificationItem("Cotización COT002 aprobada", "Hace 3 horas", Icons.Default.CheckCircle, Color(0xFF388E3C)),
            NotificationItem("Cliente nuevo registrado", "Hace 5 horas", Icons.Default.PersonAdd, Color(0xFF6A1B9A))
        )
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "Notificaciones",
            onBackClick = { navController.popBackStack() }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(notification)
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = notification.icon,
                contentDescription = null,
                tint = notification.color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = notification.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// OPERATIONS LIST SCREEN
@Composable
fun OperationsListScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    val operations = remember { DummyData.dummyOperationsList }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "Operaciones",
            onBackClick = { navController.popBackStack() }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SearchTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Buscar operaciones..."
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(operations) { operation ->
                    OperationListItem(
                        operation = operation,
                        onClick = { /* Navigate to detail */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun OperationListItem(operation: Operation, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (operation.tipo_servicio) {
                    TipoServicio.MARITIMO -> Icons.Default.DirectionsBoat
                    TipoServicio.AEREO -> Icons.Default.Flight
                    TipoServicio.TERRESTRE -> Icons.Default.LocalShipping
                    else -> Icons.Default.LocalShipping
                },
                contentDescription = null,
                tint = getStatusColor(operation.estatus),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = operation.id_operacion,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${operation.tipo_servicio} - ${operation.tipo_carga}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = getStatusText(operation.estatus),
                    style = MaterialTheme.typography.bodySmall,
                    color = getStatusColor(operation.estatus)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// QUOTES LIST SCREEN
@Composable
fun QuotesListScreen(navController: NavController) {
    val quotes = remember {
        listOf(
            QuoteItem("COT001", "Transportes ABC", "$4,500", "Pendiente"),
            QuoteItem("COT002", "Logística XYZ", "$2,800", "Enviada"),
            QuoteItem("COT003", "Comercial DEF", "$6,200", "Aprobada"),
            QuoteItem("COT004", "Distribuidora 123", "$3,200", "Rechazada")
        )
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "Cotizaciones",
            onBackClick = { navController.popBackStack() }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quotes) { quote ->
                QuoteListCard(quote)
            }
        }
    }
}

@Composable
private fun QuoteListCard(quote: QuoteItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RequestQuote,
                contentDescription = null,
                tint = when (quote.status) {
                    "Pendiente" -> Color(0xFFF57C00)
                    "Enviada" -> Color(0xFF1976D2)
                    "Aprobada" -> Color(0xFF388E3C)
                    "Rechazada" -> Color(0xFFE91E63)
                    else -> Color.Gray
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${quote.id} - ${quote.client}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${quote.status} • ${quote.amount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

// INVOICES LIST SCREEN
@Composable
fun InvoicesListScreen(navController: NavController) {
    val invoices = remember {
        listOf(
            InvoiceItem("INV-001", "$4,500", "Pendiente", "2024-01-15"),
            InvoiceItem("INV-002", "$2,800", "Pagada", "2024-01-10"),
            InvoiceItem("INV-003", "$6,200", "Vencida", "2024-01-05"),
            InvoiceItem("INV-004", "$3,200", "Pendiente", "2024-01-20")
        )
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "Facturas",
            onBackClick = { navController.popBackStack() }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(invoices) { invoice ->
                InvoiceListCard(invoice)
            }
        }
    }
}

@Composable
private fun InvoiceListCard(invoice: InvoiceItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = when (invoice.status) {
                    "Pendiente" -> Color(0xFFF57C00)
                    "Pagada" -> Color(0xFF388E3C)
                    "Vencida" -> Color(0xFFE91E63)
                    else -> Color.Gray
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.id,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${invoice.amount} • ${invoice.dueDate}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = invoice.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (invoice.status) {
                        "Pendiente" -> Color(0xFFF57C00)
                        "Pagada" -> Color(0xFF388E3C)
                        "Vencida" -> Color(0xFFE91E63)
                        else -> Color.Gray
                    }
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

// SUPPORT SCREEN
@Composable
fun SupportScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "Soporte",
            onBackClick = { navController.popBackStack() }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SupportCard(
                    title = "Preguntas Frecuentes",
                    description = "Encuentra respuestas a las preguntas más comunes",
                    icon = Icons.Default.Help,
                    onClick = { }
                )
            }
            item {
                SupportCard(
                    title = "Contactar Soporte",
                    description = "Habla directamente con nuestro equipo de soporte",
                    icon = Icons.Default.Support,
                    onClick = { }
                )
            }
            item {
                SupportCard(
                    title = "Enviar Comentarios",
                    description = "Comparte tus sugerencias para mejorar la app",
                    icon = Icons.Default.Feedback,
                    onClick = { }
                )
            }
            item {
                SupportCard(
                    title = "Reportar Problema",
                    description = "Informa sobre errores o problemas técnicos",
                    icon = Icons.Default.BugReport,
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun SupportCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

// PROFILE SCREEN
@Composable
fun ProfileScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "Mi Perfil",
            onBackClick = { navController.popBackStack() }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Juan Pérez",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "juan.perez@empresa.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Cliente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            item {
                ProfileOption("Editar Información", Icons.Default.Edit) { }
            }
            item {
                ProfileOption("Configuración", Icons.Default.Settings) { }
            }
            item {
                ProfileOption("Notificaciones", Icons.Default.Notifications) { }
            }
            item {
                ProfileOption("Cerrar Sesión", Icons.Default.Logout) { }
            }
        }
    }
}

@Composable
private fun ProfileOption(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

// DATA CLASSES
data class NotificationItem(
    val title: String,
    val time: String,
    val icon: ImageVector,
    val color: Color
)

data class QuoteItem(
    val id: String,
    val client: String,
    val amount: String,
    val status: String
)

data class InvoiceItem(
    val id: String,
    val amount: String,
    val status: String,
    val dueDate: String
)

// HELPER FUNCTIONS
private fun getStatusColor(status: EstatusOperacion): Color {
    return when (status) {
        EstatusOperacion.EN_TRANSITO -> Color(0xFF1976D2)
        EstatusOperacion.ENTREGADO -> Color(0xFF388E3C)
        EstatusOperacion.EN_ADUANA -> Color(0xFFF57C00)
        EstatusOperacion.PENDIENTE_DOCUMENTOS -> Color(0xFFE91E63)
        EstatusOperacion.CANCELADO -> Color(0xFF757575)
    }
}

private fun getStatusText(status: EstatusOperacion): String {
    return when (status) {
        EstatusOperacion.EN_TRANSITO -> "En Tránsito"
        EstatusOperacion.ENTREGADO -> "Entregado"
        EstatusOperacion.EN_ADUANA -> "En Aduana"
        EstatusOperacion.PENDIENTE_DOCUMENTOS -> "Pendiente Documentos"
        EstatusOperacion.CANCELADO -> "Cancelado"
    }
}

// STUB SCREENS - Solo placeholders con mensaje
@Composable
fun OperationDetailScreen(navController: NavController, operationId: String?) {
    GenericScreen(navController, "Detalle de Operación", "Operación: $operationId")
}

@Composable
fun CreateOperationScreen(navController: NavController) {
    GenericScreen(navController, "Crear Operación", "Formulario para crear nueva operación")
}

@Composable
fun EditOperationScreen(navController: NavController, operationId: String?) {
    GenericScreen(navController, "Editar Operación", "Editar operación: $operationId")
}

@Composable
fun QuoteDetailScreen(navController: NavController, quoteId: String?) {
    GenericScreen(navController, "Detalle de Cotización", "Cotización: $quoteId")
}

@Composable
fun CreateQuoteRequestScreen(navController: NavController) {
    GenericScreen(navController, "Solicitar Cotización", "Formulario de solicitud de cotización")
}

@Composable
fun QuoteRequestsScreen(navController: NavController) {
    GenericScreen(navController, "Solicitudes de Cotización", "Lista de solicitudes pendientes")
}

@Composable
fun InvoiceDetailScreen(navController: NavController, invoiceId: String?) {
    GenericScreen(navController, "Detalle de Factura", "Factura: $invoiceId")
}

@Composable
fun CreditNotesScreen(navController: NavController) {
    GenericScreen(navController, "Notas de Crédito", "Lista de notas de crédito")
}

@Composable
fun IncidentsListScreen(navController: NavController) {
    GenericScreen(navController, "Incidencias", "Lista de incidencias reportadas")
}

@Composable
fun IncidentDetailScreen(navController: NavController, incidentId: String?) {
    GenericScreen(navController, "Detalle de Incidencia", "Incidencia: $incidentId")
}

@Composable
fun CreateIncidentScreen(navController: NavController, operationId: String?) {
    GenericScreen(navController, "Reportar Incidencia", "Crear incidencia para operación: $operationId")
}

@Composable
fun DelaysListScreen(navController: NavController) {
    GenericScreen(navController, "Demoras", "Lista de demoras reportadas")
}

@Composable
fun CreateDelayScreen(navController: NavController, operationId: String?) {
    GenericScreen(navController, "Registrar Demora", "Registrar demora para operación: $operationId")
}

@Composable
fun TrackingMapScreen(navController: NavController, operationId: String?) {
    GenericScreen(navController, "Mapa de Seguimiento", "Tracking en mapa para operación: $operationId")
}

@Composable
fun TrackingHistoryScreen(navController: NavController, operationId: String?) {
    GenericScreen(navController, "Historial de Tracking", "Historial de operación: $operationId")
}

@Composable
fun ChatScreen(navController: NavController, operationId: String?) {
    GenericScreen(navController, "Chat", "Chat para operación: $operationId")
}

@Composable
fun ChatListScreen(navController: NavController) {
    GenericScreen(navController, "Chats", "Lista de conversaciones activas")
}

@Composable
fun FeedbackScreen(navController: NavController) {
    GenericScreen(navController, "Enviar Comentarios", "Formulario de feedback")
}

@Composable
fun SettingsScreen(navController: NavController) {
    GenericScreen(navController, "Configuración", "Ajustes de la aplicación")
}

@Composable
fun ClientsListScreen(navController: NavController) {
    GenericScreen(navController, "Clientes", "Lista de clientes registrados")
}

@Composable
fun ClientDetailScreen(navController: NavController, clientId: String?) {
    GenericScreen(navController, "Detalle de Cliente", "Cliente: $clientId")
}

@Composable
fun DocumentsListScreen(navController: NavController, entityId: String?, entityType: String?) {
    GenericScreen(navController, "Documentos", "Documentos para $entityType: $entityId")
}

// GENERIC SCREEN TEMPLATE
@Composable
private fun GenericScreen(navController: NavController, title: String, content: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = title,
            onBackClick = { navController.popBackStack() }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Construction,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Esta pantalla está en construcción",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}