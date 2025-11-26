package com.example.crm_logistico_movil.client

import android.util.Log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.crm_logistico_movil.components.TopAppBar
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.viewmodels.AuthViewModel
import com.example.crm_logistico_movil.viewmodels.NotificationViewModel
import com.example.crm_logistico_movil.repository.UnifiedClientRepository
import com.example.crm_logistico_movil.services.NotificationService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewQuoteRequestScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel
) {
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Form fields - con valores de ejemplo para facilitar pruebas
    var paisOrigen by remember { mutableStateOf("") }
    var ciudadOrigen by remember { mutableStateOf("") }
    var paisDestino by remember { mutableStateOf("") }
    var ciudadDestino by remember { mutableStateOf("") }
    var tipoServicio by remember { mutableStateOf("") }
    var tipoCarga by remember { mutableStateOf("") }
    var incoterm by remember { mutableStateOf("") }
    var descripcionCarga by remember { mutableStateOf("") }
    var valorEstimado by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val authState = authViewModel.uiState.collectAsState()
    val repository = remember { UnifiedClientRepository() }

    // Inicializar NotificationService
    LaunchedEffect(Unit) {
        NotificationService.initialize(notificationViewModel)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "Nueva Solicitud de Cotización",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Origin Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Origen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = paisOrigen,
                        onValueChange = { paisOrigen = it },
                        label = { Text("País de Origen") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null)
                        }
                    )

                    OutlinedTextField(
                        value = ciudadOrigen,
                        onValueChange = { ciudadOrigen = it },
                        label = { Text("Ciudad de Origen") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.LocationCity, contentDescription = null)
                        }
                    )
                }
            }

            // Destination Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Destino",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = paisDestino,
                        onValueChange = { paisDestino = it },
                        label = { Text("País de Destino") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Place, contentDescription = null)
                        }
                    )

                    OutlinedTextField(
                        value = ciudadDestino,
                        onValueChange = { ciudadDestino = it },
                        label = { Text("Ciudad de Destino") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.LocationCity, contentDescription = null)
                        }
                    )
                }
            }

            // Service Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Detalles del Servicio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Service Type Dropdown
                    var expandedTipoServicio by remember { mutableStateOf(false) }
                    val tiposServicio = listOf("Terrestre", "Marítimo", "Aéreo", "Multimodal")

                    ExposedDropdownMenuBox(
                        expanded = expandedTipoServicio,
                        onExpandedChange = { expandedTipoServicio = !expandedTipoServicio }
                    ) {
                        OutlinedTextField(
                            value = tipoServicio,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Tipo de Servicio") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoServicio) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            leadingIcon = {
                                Icon(Icons.Default.LocalShipping, contentDescription = null)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTipoServicio,
                            onDismissRequest = { expandedTipoServicio = false }
                        ) {
                            tiposServicio.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo) },
                                    onClick = {
                                        tipoServicio = tipo
                                        expandedTipoServicio = false
                                    }
                                )
                            }
                        }
                    }

                    // Cargo Type Dropdown
                    var expandedTipoCarga by remember { mutableStateOf(false) }
                    val tiposCarga = listOf("FCL", "LCL", "Carga General", "Refrigerada", "Peligrosa")

                    ExposedDropdownMenuBox(
                        expanded = expandedTipoCarga,
                        onExpandedChange = { expandedTipoCarga = !expandedTipoCarga }
                    ) {
                        OutlinedTextField(
                            value = tipoCarga,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Tipo de Carga") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipoCarga) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            leadingIcon = {
                                Icon(Icons.Default.Inventory, contentDescription = null)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTipoCarga,
                            onDismissRequest = { expandedTipoCarga = false }
                        ) {
                            tiposCarga.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo) },
                                    onClick = {
                                        tipoCarga = tipo
                                        expandedTipoCarga = false
                                    }
                                )
                            }
                        }
                    }

                    // Incoterm Dropdown
                    var expandedIncoterm by remember { mutableStateOf(false) }
                    val incoterms = listOf("EXW", "FCA", "CPT", "CIP", "DAP", "DPU", "DDP", "FAS", "FOB", "CFR", "CIF")

                    ExposedDropdownMenuBox(
                        expanded = expandedIncoterm,
                        onExpandedChange = { expandedIncoterm = !expandedIncoterm }
                    ) {
                        OutlinedTextField(
                            value = incoterm,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Incoterm") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedIncoterm) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            leadingIcon = {
                                Icon(Icons.Default.AccountBalance, contentDescription = null)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedIncoterm,
                            onDismissRequest = { expandedIncoterm = false }
                        ) {
                            incoterms.forEach { term ->
                                DropdownMenuItem(
                                    text = { Text(term) },
                                    onClick = {
                                        incoterm = term
                                        expandedIncoterm = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Cargo Description Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información de la Carga",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = descripcionCarga,
                        onValueChange = { descripcionCarga = it },
                        label = { Text("Descripción de la Carga") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null)
                        }
                    )

                    OutlinedTextField(
                        value = valorEstimado,
                        onValueChange = { valorEstimado = it },
                        label = { Text("Valor Estimado (USD)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null)
                        }
                    )

                    OutlinedTextField(
                        value = observaciones,
                        onValueChange = { observaciones = it },
                        label = { Text("Observaciones (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        leadingIcon = {
                            Icon(Icons.Default.Note, contentDescription = null)
                        }
                    )
                }
            }

            // Submit Button
            Button(
                onClick = {
                    scope.launch {
                        val clientId = authState.value.currentUser?.id_usuario
                        if (clientId != null) {
                            isLoading = true
                            errorMessage = null

                            try {
                                val request = SolicitudRequest(
                                    id_cliente = clientId,
                                    tipo_servicio = tipoServicio,
                                    tipo_carga = tipoCarga,
                                    origen_ciudad = ciudadOrigen,
                                    origen_pais = paisOrigen,
                                    destino_ciudad = ciudadDestino,
                                    destino_pais = paisDestino,
                                    descripcion_mercancia = descripcionCarga,
                                    valor_estimado_mercancia = valorEstimado.toDoubleOrNull()
                                )

                                val result = repository.createSolicitud(request)

                                if (result.isSuccess) {
                                    // Emitir notificación de solicitud creada
                                    val solicitudId = result.getOrNull()?.id_solicitud ?: "SOL-${System.currentTimeMillis()}"
                                    Log.d("NewQuoteRequestScreen", "Creating notification for solicitud: $solicitudId, client: $clientId")
                                    NotificationService.notifySolicitudCreated(solicitudId, clientId)
                                    Log.d("NewQuoteRequestScreen", "Notification created successfully")

                                    showSuccessDialog = true
                                    // Limpiar formulario
                                    paisOrigen = ""
                                    ciudadOrigen = ""
                                    paisDestino = ""
                                    ciudadDestino = ""
                                    tipoServicio = ""
                                    tipoCarga = ""
                                    incoterm = ""
                                    descripcionCarga = ""
                                    valorEstimado = ""
                                    observaciones = ""
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Error al enviar solicitud"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        } else {
                            errorMessage = "Usuario no autenticado"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && paisOrigen.isNotBlank() && ciudadOrigen.isNotBlank() &&
                        paisDestino.isNotBlank() && ciudadDestino.isNotBlank() &&
                        tipoServicio.isNotBlank() && tipoCarga.isNotBlank() &&
                        descripcionCarga.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Enviar Solicitud")
            }

            // Error message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Solicitud Enviada") },
            text = { Text("Tu solicitud de cotización ha sido enviada exitosamente. Recibirás una respuesta pronto.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Aceptar")
                }
            },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
            }
        )
    }

    // Error message
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or handle error
        }
    }
}
