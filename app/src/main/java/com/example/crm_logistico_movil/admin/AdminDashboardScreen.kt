package com.example.crm_logistico_movil.admin

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.crm_logistico_movil.components.TopAppBar
import com.example.crm_logistico_movil.navigation.Screen
import com.example.crm_logistico_movil.ui.theme.CRM_Logistico_MovilTheme
import com.example.crm_logistico_movil.client.QuickAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = "Panel Administrativo",
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
                    onClick = { navController.navigate(Screen.Settings.route) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración"
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
                AdminWelcomeCard()
            }
            
            item {
                AdminQuickActionsRow(navController)
            }
            
            item {
                AdminStatsGrid()
            }
            
            item {
                SystemOverviewCard()
            }
            
            item {
                RecentActivityCard()
            }
        }
    }
}

@Composable
private fun AdminWelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = Color(0xFF6A1B9A),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Panel Administrativo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
                Text(
                    text = "Control total del sistema y usuarios",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6A1B9A).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun AdminQuickActionsRow(navController: NavController) {
    Column {
        Text(
            text = "Administración",
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
                        title = "Gestionar Usuarios",
                        icon = Icons.Default.People,
                        color = Color(0xFF1976D2),
                        onClick = { /* Navigate to users */ }
                    ),
                    QuickAction(
                        title = "Ver Clientes",
                        icon = Icons.Default.Business,
                        color = Color(0xFF388E3C),
                        onClick = { navController.navigate(Screen.ClientsList.route) }
                    ),
                    QuickAction(
                        title = "Reportes",
                        icon = Icons.Default.Assessment,
                        color = Color(0xFFF57C00),
                        onClick = { /* Navigate to reports */ }
                    ),
                    QuickAction(
                        title = "Configuración",
                        icon = Icons.Default.Settings,
                        color = Color(0xFF6A1B9A),
                        onClick = { navController.navigate(Screen.Settings.route) }
                    )
                )
            ) { action ->
                AdminActionCard(action)
            }
        }
    }
}

@Composable
private fun AdminActionCard(action: QuickAction) {
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
private fun AdminStatsGrid() {
    Column {
        Text(
            text = "Estadísticas del Sistema",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                title = "Usuarios Activos",
                value = "24",
                icon = Icons.Default.People,
                color = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Clientes",
                value = "156",
                icon = Icons.Default.Business,
                color = Color(0xFF388E3C),
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminStatCard(
                title = "Operaciones Total",
                value = "1,234",
                icon = Icons.Default.LocalShipping,
                color = Color(0xFFF57C00),
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "Ingresos Mes",
                value = "$52.5K",
                icon = Icons.Default.AttachMoney,
                color = Color(0xFF6A1B9A),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AdminStatCard(
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
private fun SystemOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Estado del Sistema",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SystemStatusItem(
                title = "Servidor Principal",
                status = "Operativo",
                icon = Icons.Default.Storage,
                color = Color(0xFF388E3C)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SystemStatusItem(
                title = "Base de Datos",
                status = "Conectada",
                icon = Icons.Default.Storage,
                color = Color(0xFF388E3C)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SystemStatusItem(
                title = "API Externa",
                status = "Funcional",
                icon = Icons.Default.Api,
                color = Color(0xFF388E3C)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SystemStatusItem(
                title = "Notificaciones",
                status = "Activas",
                icon = Icons.Default.Notifications,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
private fun SystemStatusItem(
    title: String,
    status: String,
    icon: ImageVector,
    color: Color
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
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
        
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun RecentActivityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actividad Reciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ActivityItem(
                title = "Nuevo usuario registrado: Juan Pérez",
                time = "Hace 5 minutos",
                icon = Icons.Default.PersonAdd,
                color = Color(0xFF388E3C)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            ActivityItem(
                title = "Operación OP001 completada",
                time = "Hace 15 minutos",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF1976D2)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            ActivityItem(
                title = "Incidencia reportada en OP003",
                time = "Hace 30 minutos",
                icon = Icons.Default.Warning,
                color = Color(0xFFE91E63)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            ActivityItem(
                title = "Nuevo cliente: Transportes ABC",
                time = "Hace 1 hora",
                icon = Icons.Default.Business,
                color = Color(0xFF6A1B9A)
            )
        }
    }
}

@Composable
private fun ActivityItem(
    title: String,
    time: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAdminDashboardScreen() {
    CRM_Logistico_MovilTheme {
        AdminDashboardScreen(navController = rememberNavController())
    }
}