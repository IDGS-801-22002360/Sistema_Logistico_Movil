package com.example.crm_logistico_movil.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import com.example.crm_logistico_movil.components.CustomOutlinedTextField
import com.example.crm_logistico_movil.dummy.DummyData
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.models.FacturaCliente
import com.example.crm_logistico_movil.models.FacturaClienteTmp
import com.example.crm_logistico_movil.viewmodels.AuthViewModel
import com.example.crm_logistico_movil.repository.ClientRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.crm_logistico_movil.navigation.Screen
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

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

// OPERATIONS LIST SCREEN - load operations for the logged-in client
@Composable
fun OperationsListScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val clientRepository = remember { ClientRepository() }

    var searchQuery by remember { mutableStateOf("") }
    var operations by remember { mutableStateOf<List<com.example.crm_logistico_movil.models.OperationExtended>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // load when user available
    LaunchedEffect(authState.currentUser?.id_usuario) {
        val clientId = authState.currentUser?.id_usuario
        if (clientId != null) {
            isLoading = true
            errorMessage = null
            val res = clientRepository.getClientOperations(clientId, limit = 50)
            if (res.isSuccess) {
                // map ProcedureResponse -> List<OperationExtended>
                val proc = res.getOrNull()
                val rows = proc?.results?.getOrNull(0) ?: emptyList()
                val list = rows.map { m ->
                    com.example.crm_logistico_movil.models.OperationExtended(
                        id_operacion = m["id_operacion"]?.toString() ?: "",
                        id_cotizacion = m["id_cotizacion"]?.toString(),
                        id_cliente = m["id_cliente"]?.toString() ?: "",
                        id_usuario_operativo = m["id_usuario_operativo"]?.toString() ?: "",
                        id_proveedor = m["id_proveedor"]?.toString() ?: "",
                        id_agente = m["id_agente"]?.toString(),
                        tipo_servicio = m["tipo_servicio"]?.toString() ?: "",
                        tipo_carga = m["tipo_carga"]?.toString() ?: "",
                        incoterm = m["incoterm"]?.toString() ?: "",
                        fecha_inicio_operacion = m["fecha_inicio_operacion"]?.toString() ?: "",
                        fecha_estimada_arribo = m["fecha_estimada_arribo"]?.toString(),
                        fecha_estimada_entrega = m["fecha_estimada_entrega"]?.toString(),
                        fecha_arribo_real = m["fecha_arribo_real"]?.toString(),
                        fecha_entrega_real = m["fecha_entrega_real"]?.toString(),
                        estatus = m["estatus"]?.toString() ?: "",
                        numero_referencia_proveedor = m["numero_referencia_proveedor"]?.toString(),
                        notas_operacion = m["notas_operacion"]?.toString(),
                        fecha_creacion = m["fecha_creacion"]?.toString() ?: "",
                        proveedorNombre = m["proveedor_nombre"]?.toString(),
                        operativoNombre = m["operativo_nombre"]?.toString(),
                        operativoApellido = m["operativo_apellido"]?.toString()
                    )
                }
                operations = list
            } else {
                errorMessage = res.exceptionOrNull()?.message ?: "Error al cargar operaciones"
            }
            isLoading = false
        }
    }

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

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(operations) { operation ->
                        OperationListItem(
                            operation = operation,
                            onClick = { navController.navigate(Screen.OperationDetail.createRoute(operation.id_operacion)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationListItem(operation: com.example.crm_logistico_movil.models.OperationExtended, onClick: () -> Unit) {
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
                imageVector = when (operation.tipo_servicio.uppercase()) {
                    "MARITIMO" -> Icons.Default.DirectionsBoat
                    "AEREO" -> Icons.Default.Flight
                    "TERRESTRE" -> Icons.Default.LocalShipping
                    else -> Icons.Default.LocalShipping
                },
                contentDescription = null,
                tint = getStatusColor(operation.estatus ?: ""),
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
                QuoteListCard(quote = quote, onClick = { navController.navigate(Screen.QuoteDetail.createRoute(quote.id)) })
            }
        }
    }
}

@Composable
private fun QuoteListCard(quote: QuoteItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val clientRepository = remember { ClientRepository() }

    var searchQuery by remember { mutableStateOf("") }
    var facturas by remember { mutableStateOf<List<FacturaClienteTmp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar facturas cuando se obtiene el usuario actual
    LaunchedEffect(authState.currentUser?.id_usuario) {
        val clientId = authState.currentUser?.id_usuario
        if (clientId != null) {
            isLoading = true
            val res = clientRepository.getFacturasCliente(clientId)
            if (res.isSuccess) {
                facturas = res.getOrNull() ?: emptyList()
            } else {
                errorMessage = res.exceptionOrNull()?.message ?: "Error al cargar facturas"
            }
            isLoading = false
        }
    }

    val filteredFacturas = facturas.filter {
        it.numero_factura.contains(searchQuery, ignoreCase = true) ||
                it.estatus.contains(searchQuery, ignoreCase = true) ||
                it.moneda.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "Facturas",
            onBackClick = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SearchTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Buscar facturas...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFacturas) { factura ->
                        InvoiceListCard(factura) {
                            navController.navigate(Screen.InvoiceDetail.createRoute(factura.id_factura_cliente))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceListCard(factura: FacturaClienteTmp, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                tint = when (factura.estatus.lowercase()) {
                    "pagada" -> Color(0xFF2E7D32)
                    "pendiente" -> Color(0xFFF57C00)
                    "vencida" -> Color(0xFFD32F2F)
                    "cancelada" -> Color(0xFF757575)
                    else -> Color.Gray
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = factura.numero_factura,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${factura.moneda} ${String.format("%.2f", factura.monto_total)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = factura.estatus.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (factura.estatus.lowercase()) {
                        "pagada" -> Color(0xFF2E7D32)
                        "pendiente" -> Color(0xFFF57C00)
                        "vencida" -> Color(0xFFD32F2F)
                        "cancelada" -> Color(0xFF757575)
                        else -> Color.Gray
                    }
                )
                Text(
                    text = "Vence: ${factura.fecha_vencimiento.substring(0, 10)}", // Solo la fecha
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
    val authViewModel: AuthViewModel = viewModel()

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
                        val authState by authViewModel.uiState.collectAsState()
                        val clientRepository = remember { ClientRepository() }
                        var clienteInfo by remember { mutableStateOf<Map<String, Any>?>(null) }
                        var loadingInfo by remember { mutableStateOf(false) }

                        LaunchedEffect(authState.currentUser?.id_usuario) {
                            val clientId = authState.currentUser?.id_usuario
                            if (clientId != null) {
                                loadingInfo = true
                                val res = clientRepository.getClientInfo(clientId)
                                if (res.isSuccess) {
                                    clienteInfo = res.getOrNull()
                                }
                                loadingInfo = false
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val displayName = clienteInfo?.get("nombre_usuario")?.toString()
                            ?: clienteInfo?.get("nombre")?.toString()
                            ?: authState.currentUser?.nombre
                            ?: "Usuario"
                        val displayEmail = clienteInfo?.get("email_usuario")?.toString()
                            ?: authState.currentUser?.email
                            ?: ""

                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = displayEmail,
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
                ProfileOption("Editar Información", Icons.Default.Edit) { navController.navigate(com.example.crm_logistico_movil.navigation.Screen.EditProfile.route) }
            }
            item {
                ProfileOption("Cerrar Sesión", Icons.Default.Logout) {
                    // Llamar la función logout para limpiar la sesión
                    authViewModel.logout()
                    // Navegar al login y limpiar el stack de navegación
                    navController.navigate(com.example.crm_logistico_movil.navigation.Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Limpiar todo el stack de navegación
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val clientRepository = remember { ClientRepository() }
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    // form fields
    var nombreEmpresa by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var ciudad by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var emailContacto by remember { mutableStateOf("") }
    var contactoNombre by remember { mutableStateOf("") }
    var contactoPuesto by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var apellidoUsuario by remember { mutableStateOf("") }
    var emailUsuario by remember { mutableStateOf("") }

    // load current info
    LaunchedEffect(authState.currentUser?.id_usuario) {
        val clientId = authState.currentUser?.id_usuario
        if (clientId != null) {
            isLoading = true
            val res = clientRepository.getClientInfo(clientId)
            if (res.isSuccess) {
                val map = res.getOrNull()
                // fill fields from map with safe casts
                nombreEmpresa = map?.get("nombre_empresa")?.toString() ?: nombreEmpresa
                rfc = map?.get("rfc")?.toString() ?: rfc
                direccion = map?.get("direccion")?.toString() ?: direccion
                ciudad = map?.get("ciudad")?.toString() ?: ciudad
                pais = map?.get("pais")?.toString() ?: pais
                telefono = map?.get("telefono")?.toString() ?: telefono
                emailContacto = map?.get("email_contacto")?.toString() ?: emailContacto
                contactoNombre = map?.get("contacto_nombre")?.toString() ?: contactoNombre
                contactoPuesto = map?.get("contacto_puesto")?.toString() ?: contactoPuesto
                nombreUsuario = map?.get("nombre_usuario")?.toString() ?: authState.currentUser?.nombre ?: nombreUsuario
                apellidoUsuario = map?.get("apellido_usuario")?.toString() ?: authState.currentUser?.apellido ?: apellidoUsuario
                emailUsuario = map?.get("email_usuario")?.toString() ?: authState.currentUser?.email ?: emailUsuario
            } else {
                message = res.exceptionOrNull()?.message ?: "Error al cargar datos"
            }
            isLoading = false
        }
    }

    Scaffold(topBar = { TopAppBar(title = "Editar Información", onBackClick = { navController.popBackStack() }) }) { padding ->
        val scrollState = rememberScrollState()
        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(padding)
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                CustomOutlinedTextField(value = nombreEmpresa, onValueChange = { nombreEmpresa = it }, label = "Nombre de la Empresa")
                CustomOutlinedTextField(value = rfc, onValueChange = { rfc = it }, label = "RFC")
                CustomOutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = "Dirección")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) { CustomOutlinedTextField(value = ciudad, onValueChange = { ciudad = it }, label = "Ciudad") }
                    Column(modifier = Modifier.weight(1f)) { CustomOutlinedTextField(value = pais, onValueChange = { pais = it }, label = "País") }
                }
                CustomOutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = "Teléfono", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                CustomOutlinedTextField(value = emailContacto, onValueChange = { emailContacto = it }, label = "Email de contacto", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                CustomOutlinedTextField(value = contactoNombre, onValueChange = { contactoNombre = it }, label = "Nombre del contacto")
                CustomOutlinedTextField(value = contactoPuesto, onValueChange = { contactoPuesto = it }, label = "Puesto del contacto")

                Divider()
                Text("Usuario relacionado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                CustomOutlinedTextField(value = nombreUsuario, onValueChange = { nombreUsuario = it }, label = "Nombre")
                CustomOutlinedTextField(value = apellidoUsuario, onValueChange = { apellidoUsuario = it }, label = "Apellido")
                CustomOutlinedTextField(value = emailUsuario, onValueChange = { emailUsuario = it }, label = "Email", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val clientId = authState.currentUser?.id_usuario
                    if (clientId != null) {
                        isLoading = true
                        message = null
                        val payload = mapOf<String, Any?>(
                            "nombre_empresa" to nombreEmpresa,
                            "rfc" to rfc,
                            "direccion" to direccion,
                            "ciudad" to ciudad,
                            "pais" to pais,
                            "telefono" to telefono,
                            "email_contacto" to emailContacto,
                            "contacto_nombre" to contactoNombre,
                            "contacto_puesto" to contactoPuesto,
                            "nombre_usuario" to nombreUsuario,
                            "apellido_usuario" to apellidoUsuario,
                            "email_usuario" to emailUsuario
                        )
                        coroutineScope.launch {
                            val res = clientRepository.editClient(clientId, payload)
                            isLoading = false
                            if (res.isSuccess) {
                                message = res.getOrNull()?.message ?: "Guardado"
                                // after saving, go back
                                navController.popBackStack()
                            } else {
                                message = res.exceptionOrNull()?.message ?: "Error al guardar"
                            }
                        }
                    } else {
                        message = "Usuario no autenticado"
                    }
                }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Text("Guardar")
                }

                message?.let { Text(text = it, color = if (it.contains("correcto") || it.contains("Guardado")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error) }
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
    // Delegate to the client-specific detailed screen when an operationId is provided.
    if (operationId == null) {
        GenericScreen(navController, "Detalle de Operación", "Operación: $operationId")
    } else {
        // Call the richer implementation living in the client package
        com.example.crm_logistico_movil.client.OperationDetailScreen(navController = navController, operationId = operationId)
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuoteRequestScreen(navController: NavController) {
    val authViewModel: com.example.crm_logistico_movil.viewmodels.AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val clientRepository = remember { com.example.crm_logistico_movil.repository.ClientRepository() }
    val coroutineScope = rememberCoroutineScope()
    var tipoServicio by remember { mutableStateOf("") }
    var tipoCarga by remember { mutableStateOf("") }
    var origenCiudad by remember { mutableStateOf("") }
    var origenPais by remember { mutableStateOf("") }
    var destinoCiudad by remember { mutableStateOf("") }
    var destinoPais by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = "Solicitar Cotización", onBackClick = { navController.popBackStack() }) }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Nuevo pedido de cotización", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Completa los datos para que podamos cotizar tu envío", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Form fields in two columns for compactness on larger screens
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomOutlinedTextField(value = tipoServicio, onValueChange = { tipoServicio = it }, label = "Tipo de servicio", leadingIcon = Icons.Default.LocalShipping)
                CustomOutlinedTextField(value = tipoCarga, onValueChange = { tipoCarga = it }, label = "Tipo de carga", leadingIcon = Icons.Default.Inventory)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        CustomOutlinedTextField(value = origenCiudad, onValueChange = { origenCiudad = it }, label = "Origen - Ciudad", leadingIcon = Icons.Default.Place)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        CustomOutlinedTextField(value = origenPais, onValueChange = { origenPais = it }, label = "Origen - País", leadingIcon = Icons.Default.Flag)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        CustomOutlinedTextField(value = destinoCiudad, onValueChange = { destinoCiudad = it }, label = "Destino - Ciudad", leadingIcon = Icons.Default.Place)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        CustomOutlinedTextField(value = destinoPais, onValueChange = { destinoPais = it }, label = "Destino - País", leadingIcon = Icons.Default.Flag)
                    }
                }

                CustomOutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = "Descripción", leadingIcon = Icons.Default.Description, maxLines = 4)
                CustomOutlinedTextField(value = valor, onValueChange = { valor = it }, label = "Valor estimado", leadingIcon = Icons.Default.AttachMoney, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val clientId = authState.currentUser?.id_usuario
                    if (clientId != null) {
                        isLoading = true
                        message = null
                        val req = com.example.crm_logistico_movil.models.SolicitudRequest(
                            id_cliente = clientId,
                            tipo_servicio = tipoServicio,
                            tipo_carga = tipoCarga,
                            origen_ciudad = origenCiudad,
                            origen_pais = origenPais,
                            destino_ciudad = destinoCiudad,
                            destino_pais = destinoPais,
                            descripcion_mercancia = descripcion,
                            valor_estimado_mercancia = valor.toDoubleOrNull()
                        )
                        coroutineScope.launch {
                            val res = clientRepository.createSolicitud(req)
                            isLoading = false
                            if (res.isSuccess) {
                                message = "Solicitud creada correctamente"
                                // clear form
                                tipoServicio = ""; tipoCarga = ""; origenCiudad = ""; origenPais = ""; destinoCiudad = ""; destinoPais = ""; descripcion = ""; valor = ""
                            } else {
                                message = res.exceptionOrNull()?.message ?: "Error al crear solicitud"
                            }
                        }
                    } else {
                        message = "Usuario no autenticado"
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(if (isLoading) "Enviando..." else "Enviar solicitud")
            }

            message?.let { Text(text = it, color = if (it.contains("correctamente")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun QuoteRequestsScreen(navController: NavController) {
    val authViewModel: com.example.crm_logistico_movil.viewmodels.AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val repo = remember { com.example.crm_logistico_movil.repository.ClientRepository() }

    var solicitudes by remember { mutableStateOf<List<com.example.crm_logistico_movil.models.SolicitudCotizacionExtended>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState.currentUser?.id_usuario) {
        val clientId = authState.currentUser?.id_usuario
        if (clientId != null) {
            loading = true
            val res = repo.getClientQuoteRequests(clientId, limit = 50)
            if (res.isSuccess) {
                val proc = res.getOrNull()
                val rows = proc?.results?.getOrNull(0) ?: emptyList()
                solicitudes = rows.map { m ->
                    com.example.crm_logistico_movil.models.SolicitudCotizacionExtended(
                        id_solicitud = m["id_solicitud"]?.toString() ?: "",
                        id_cliente = m["id_cliente"]?.toString() ?: "",
                        tipo_servicio = m["tipo_servicio"]?.toString() ?: "",
                        tipo_carga = m["tipo_carga"]?.toString() ?: "",
                        origen_ciudad = m["origen_ciudad"]?.toString() ?: "",
                        origen_pais = m["origen_pais"]?.toString() ?: "",
                        destino_ciudad = m["destino_ciudad"]?.toString() ?: "",
                        destino_pais = m["destino_pais"]?.toString() ?: "",
                        fecha_solicitud = m["fecha_solicitud"]?.toString() ?: "",
                        descripcion_mercancia = m["descripcion_mercancia"]?.toString(),
                        valor_estimado_mercancia = m["valor_estimado_mercancia"]?.toString()?.toDoubleOrNull(),
                        estatus = m["estatus"]?.toString() ?: ""
                    )
                }
            } else {
                error = res.exceptionOrNull()?.message ?: "Error al cargar solicitudes"
            }
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = "Solicitudes de Cotización", onBackClick = { navController.popBackStack() })
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (error != null) {
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(solicitudes) { s ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(com.example.crm_logistico_movil.navigation.Screen.QuoteDetail.createRoute(s.id_solicitud)) },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                // left color/status chip
                                Surface(
                                    modifier = Modifier
                                        .size(width = 10.dp, height = 60.dp)
                                        .padding(end = 8.dp),
                                    color = getStatusColor(s.estatus),
                                    shape = RoundedCornerShape(6.dp)
                                ) {}

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = s.id_solicitud, fontWeight = FontWeight.Bold)
                                    Text(text = "${s.tipo_servicio} - ${s.tipo_carga}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = s.descripcion_mercancia ?: "-", maxLines = 3, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(color = getStatusColor(s.estatus), shape = RoundedCornerShape(8.dp)) {
                                            Text(text = getStatusText(s.estatus), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Cliente: ${s.id_cliente.takeUnless { it.isBlank() } ?: "-"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceDetailScreen(navController: NavController, invoiceId: String?) {
    if (invoiceId == null) {
        GenericScreen(navController, "Error", "ID de factura no válido")
        return
    }

    val clientRepository = remember { ClientRepository() }
    var factura by remember { mutableStateOf<FacturaClienteTmp?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(invoiceId) {
        val result = clientRepository.getFacturaDetail(invoiceId)
        if (result.isSuccess) {
            factura = result.getOrNull()
        } else {
            errorMessage = result.exceptionOrNull()?.message ?: "Error al cargar la factura"
        }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = "Detalle de Factura",
            onBackClick = { navController.popBackStack() }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            factura?.let { f ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Información básica de la factura
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Título y ID de la factura
                                    Text(
                                        text = f.numero_factura,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Factura ${f.id_factura_cliente}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Badge del estatus
                                    Row {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (f.estatus.lowercase()) {
                                                    "pagada" -> Color(0xFF2E7D32).copy(alpha = 0.1f)
                                                    "pendiente" -> Color(0xFFF57C00).copy(alpha = 0.1f)
                                                    "vencida" -> Color(0xFFD32F2F).copy(alpha = 0.1f)
                                                    else -> Color.Gray.copy(alpha = 0.1f)
                                                }
                                            )
                                        ) {
                                            Text(
                                                text = f.estatus.replaceFirstChar {
                                                    if (it.isLowerCase()) it.titlecase() else it.toString()
                                                },
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = when (f.estatus.lowercase()) {
                                                    "pagada" -> Color(0xFF2E7D32)
                                                    "pendiente" -> Color(0xFFF57C00)
                                                    "vencida" -> Color(0xFFD32F2F)
                                                    else -> Color.Gray
                                                },
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Información de montos
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Información Financiera",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                DetailRow("Monto Total", "${f.moneda} ${String.format("%.2f", f.monto_total)}")
                                DetailRow("Monto Pagado", "${f.moneda} ${String.format("%.2f", f.monto_pagado)}")
                                DetailRow("Saldo Pendiente", "${f.moneda} ${String.format("%.2f", f.monto_total - f.monto_pagado)}")
                                DetailRow("Moneda", f.moneda)
                            }
                        }
                    }

                    // Información de fechas
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Fechas Importantes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                DetailRow("Fecha de Emisión", f.fecha_emision.substring(0, 10))
                                DetailRow("Fecha de Vencimiento", f.fecha_vencimiento.substring(0, 10))
                                DetailRow("Fecha de Creación", f.fecha_creacion.substring(0, 10))
                            }
                        }
                    }

                    // Información relacionada
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Información Relacionada",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                f.id_operacion?.let { DetailRow("Operación", it) }
                                f.id_cotizacion?.let { DetailRow("Cotización", it) }
                                f.cotizacion_tipo_servicio?.let { DetailRow("Tipo de Servicio", it) }
                                f.cotizacion_tipo_carga?.let { DetailRow("Tipo de Carga", it) }
                                f.descripcion_mercancia?.let { DetailRow("Descripción de Mercancía", it) }
                            }
                        }
                    }

                    // Observaciones
                    if (!f.observaciones.isNullOrBlank()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = "Observaciones",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = f.observaciones!!,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Factura no encontrada")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
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

// Overloads that accept a string representation (from backend) and map to the enum
private fun parseEstatusOperacion(status: String?): EstatusOperacion {
    if (status == null) return EstatusOperacion.CANCELADO
    return when (status.uppercase().replace(" ", "_").replace("-", "_").replace("Á", "A")) {
        "EN_TRANSITO", "ENTRÁNSITO", "EN_TRÁNSITO", "EN-TRANSITO", "EN TRÁNSITO" -> EstatusOperacion.EN_TRANSITO
        "ENTREGADO" -> EstatusOperacion.ENTREGADO
        "EN_ADUANA", "ENADUANA", "EN ADUANA" -> EstatusOperacion.EN_ADUANA
        "PENDIENTE_DOCUMENTOS", "PENDIENTE DOCUMENTOS", "PENDIENTE_DOCUMENTO", "PENDIENTE" -> EstatusOperacion.PENDIENTE_DOCUMENTOS
        "CANCELADO", "CANCELADA" -> EstatusOperacion.CANCELADO
        else -> {
            // Try matching by literal enum name
            try {
                EstatusOperacion.valueOf(status.uppercase())
            } catch (e: Exception) {
                EstatusOperacion.CANCELADO
            }
        }
    }
}

private fun getStatusColor(status: String?): Color {
    return getStatusColor(parseEstatusOperacion(status))
}

private fun getStatusText(status: String?): String {
    return getStatusText(parseEstatusOperacion(status))
}