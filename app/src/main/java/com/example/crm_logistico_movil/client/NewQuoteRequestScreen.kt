package com.example.crm_logistico_movil.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.crm_logistico_movil.components.TopAppBar
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewQuoteRequestScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Form fields
    var paisOrigen by remember { mutableStateOf("") }
    var ciudadOrigen by remember { mutableStateOf("") }
    var paisDestino by remember { mutableStateOf("") }
    var ciudadDestino by remember { mutableStateOf("") }
    var tipoServicio by remember { mutableStateOf("") }
    var tipoCarga by remember { mutableStateOf("") }
    var incoterm by remember { mutableStateOf("") }
    var descripcionCarga by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    
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
                        // TODO: Implement submission logic
                        isLoading = true
                        // Simulate API call
                        kotlinx.coroutines.delay(2000)
                        isLoading = false
                        showSuccessDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && paisOrigen.isNotBlank() && paisDestino.isNotBlank() && 
                         tipoServicio.isNotBlank() && tipoCarga.isNotBlank() && 
                         incoterm.isNotBlank() && descripcionCarga.isNotBlank()
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
