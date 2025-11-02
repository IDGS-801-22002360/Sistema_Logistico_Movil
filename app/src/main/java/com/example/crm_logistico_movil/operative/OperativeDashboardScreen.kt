package com.example.crm_logistico_movil.operative

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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.crm_logistico_movil.R
import com.example.crm_logistico_movil.components.TopAppBar
import com.example.crm_logistico_movil.dummy.DummyData
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.navigation.Screen
import com.example.crm_logistico_movil.ui.theme.CRM_Logistico_MovilTheme
import com.example.crm_logistico_movil.client.QuickAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperativeDashboardScreen(navController: NavController) {
    val operations = remember { DummyData.dummyOperationsList }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = "Panel Operativo",
            showBackButton = false,
            actions = {
                IconButton(
                    onClick = { navController.navigate(Screen.Notifications.route) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones"
                    )
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
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OperativeWelcomeCard()
            }
            
            item {
                OperativeQuickActionsRow(navController)
            }
            
            item {
                OperativeStatsCards()
            }
            
            item {
                PendingTasksCard(navController)
            }
            
            item {
                RecentOperationsCard(operations, navController)
            }
        }
    }
}

@Composable
private fun OperativeWelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WorkOutline,
                contentDescription = null,
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Panel Operativo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Text(
                    text = "Gestiona operaciones, incidencias y seguimiento",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1976D2).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun OperativeQuickActionsRow(navController: NavController) {
    Column {
        Text(
            text = "Acciones Operativas",
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
                        title = "Crear Operación",
                        icon = Icons.Default.Add,
                        color = Color(0xFF388E3C),
                        onClick = { navController.navigate(Screen.CreateOperation.route) }
                    ),
                    QuickAction(
                        title = "Reportar Incidencia",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFE91E63),
                        onClick = { navController.navigate(Screen.IncidentsList.route) }
                    ),
                    QuickAction(
                        title = "Registrar Demora",
                        icon = Icons.Default.Schedule,
                        color = Color(0xFFF57C00),
                        onClick = { navController.navigate(Screen.DelaysList.route) }
                    ),
                    QuickAction(
                        title = "Chat Interno",
                        icon = Icons.Default.Chat,
                        color = Color(0xFF7B1FA2),
                        onClick = { navController.navigate(Screen.ChatList.route) }
                    )
                )
            ) { action ->
                OperativeActionCard(action)
            }
        }
    }
}

@Composable
private fun OperativeActionCard(action: QuickAction) {
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
private fun OperativeStatsCards() {
    Column {
        Text(
            text = "Estadísticas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OperativeStatCard(
                title = "Operaciones Asignadas",
                value = "8",
                icon = Icons.Default.Assignment,
                color = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            )
            OperativeStatCard(
                title = "Incidencias Activas",
                value = "3",
                icon = Icons.Default.Warning,
                color = Color(0xFFE91E63),
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OperativeStatCard(
                title = "Demoras Reportadas",
                value = "2",
                icon = Icons.Default.Schedule,
                color = Color(0xFFF57C00),
                modifier = Modifier.weight(1f)
            )
            OperativeStatCard(
                title = "Entregas Hoy",
                value = "5",
                icon = Icons.Default.LocalShipping,
                color = Color(0xFF388E3C),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OperativeStatCard(
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
private fun PendingTasksCard(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tareas Pendientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PendingTaskItem(
                title = "Actualizar OP001 - En Aduana",
                subtitle = "Actualizar estatus de operación marítima",
                icon = Icons.Default.Update,
                color = Color(0xFFF57C00),
                onClick = { /* Navigate to operation detail */ }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            PendingTaskItem(
                title = "Resolver INC001 - Daño mercancía",
                subtitle = "Incidencia reportada hace 2 horas",
                icon = Icons.Default.Warning,
                color = Color(0xFFE91E63),
                onClick = { /* Navigate to incident detail */ }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            PendingTaskItem(
                title = "Responder chat OP002",
                subtitle = "Cliente solicita actualización",
                icon = Icons.Default.Chat,
                color = Color(0xFF7B1FA2),
                onClick = { /* Navigate to chat */ }
            )
        }
    }
}

@Composable
private fun PendingTaskItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun RecentOperationsCard(
    operations: List<Operation>,
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
                    text = "Operaciones Asignadas",
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
                OperativeOperationItem(
                    operation = operation,
                    onClick = {
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
private fun OperativeOperationItem(
    operation: Operation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                text = "${operation.tipo_servicio} - ${getStatusText(operation.estatus)}",
                style = MaterialTheme.typography.bodySmall,
                color = getStatusColor(operation.estatus)
            )
        }
        
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

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

@Preview(showBackground = true)
@Composable
fun PreviewOperativeDashboardScreen() {
    CRM_Logistico_MovilTheme {
        OperativeDashboardScreen(navController = rememberNavController())
    }
}