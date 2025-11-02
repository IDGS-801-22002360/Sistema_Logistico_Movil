package com.example.crm_logistico_movil.sales

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
fun SalesDashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = "Panel de Ventas",
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
                SalesWelcomeCard()
            }
            
            item {
                SalesQuickActionsRow(navController)
            }
            
            item {
                SalesStatsGrid()
            }
            
            item {
                QuotesOverviewCard(navController)
            }
            
            item {
                ClientsActivityCard(navController)
            }
        }
    }
}

@Composable
private fun SalesWelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF388E3C).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF388E3C),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Panel de Ventas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF388E3C)
                )
                Text(
                    text = "Gestiona cotizaciones y clientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF388E3C).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SalesQuickActionsRow(navController: NavController) {
    Column {
        Text(
            text = "Acciones de Ventas",
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
                        title = "Nueva Cotización",
                        icon = Icons.Default.Add,
                        color = Color(0xFF388E3C),
                        onClick = { /* Navigate to create quote */ }
                    ),
                    QuickAction(
                        title = "Ver Cotizaciones",
                        icon = Icons.Default.RequestQuote,
                        color = Color(0xFF1976D2),
                        onClick = { navController.navigate(Screen.QuotesList.route) }
                    ),
                    QuickAction(
                        title = "Clientes",
                        icon = Icons.Default.Business,
                        color = Color(0xFFF57C00),
                        onClick = { navController.navigate(Screen.ClientsList.route) }
                    ),
                    QuickAction(
                        title = "Reportes",
                        icon = Icons.Default.Assessment,
                        color = Color(0xFF6A1B9A),
                        onClick = { /* Navigate to reports */ }
                    )
                )
            ) { action ->
                SalesActionCard(action)
            }
        }
    }
}

@Composable
private fun SalesActionCard(action: QuickAction) {
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
private fun SalesStatsGrid() {
    Column {
        Text(
            text = "Indicadores de Ventas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SalesStatCard(
                title = "Cotizaciones Mes",
                value = "47",
                icon = Icons.Default.RequestQuote,
                color = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            )
            SalesStatCard(
                title = "Cerradas",
                value = "23",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF388E3C),
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SalesStatCard(
                title = "Meta Mes",
                value = "85%",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFFF57C00),
                modifier = Modifier.weight(1f)
            )
            SalesStatCard(
                title = "Comisiones",
                value = "$8.2K",
                icon = Icons.Default.AttachMoney,
                color = Color(0xFF6A1B9A),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SalesStatCard(
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
private fun QuotesOverviewCard(navController: NavController) {
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
                    text = "Cotizaciones Pendientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { navController.navigate(Screen.QuotesList.route) }
                ) {
                    Text("Ver todas")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            QuoteItem(
                quoteId = "COT001",
                client = "Transportes ABC",
                amount = "$4,500",
                status = "Pendiente",
                color = Color(0xFFF57C00)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            QuoteItem(
                quoteId = "COT002",
                client = "Logística XYZ",
                amount = "$2,800",
                status = "Enviada",
                color = Color(0xFF1976D2)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            QuoteItem(
                quoteId = "COT003",
                client = "Comercial DEF",
                amount = "$6,200",
                status = "Esperando respuesta",
                color = Color(0xFF6A1B9A)
            )
        }
    }
}

@Composable
private fun QuoteItem(
    quoteId: String,
    client: String,
    amount: String,
    status: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.RequestQuote,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "$quoteId - $client",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$status • $amount",
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ClientsActivityCard(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actividad de Clientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ClientActivityItem(
                title = "Nuevo cliente: Transportes 123",
                time = "Hace 2 horas",
                icon = Icons.Default.PersonAdd,
                color = Color(0xFF388E3C)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            ClientActivityItem(
                title = "Cotización aprobada: COT001",
                time = "Hace 4 horas",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF1976D2)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            ClientActivityItem(
                title = "Solicitud de cotización recibida",
                time = "Hace 6 horas",
                icon = Icons.Default.Mail,
                color = Color(0xFFF57C00)
            )
        }
    }
}

@Composable
private fun ClientActivityItem(
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
fun PreviewSalesDashboardScreen() {
    CRM_Logistico_MovilTheme {
        SalesDashboardScreen(navController = rememberNavController())
    }
}