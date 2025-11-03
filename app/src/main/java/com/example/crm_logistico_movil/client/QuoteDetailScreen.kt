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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.crm_logistico_movil.components.TopAppBar
import com.example.crm_logistico_movil.components.DetailRow
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.repository.ClientRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crm_logistico_movil.viewmodels.AuthViewModel
import com.example.crm_logistico_movil.utils.getStatusColor
import com.example.crm_logistico_movil.utils.getStatusText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(navController: NavController, quoteId: String) {
    var isLoading by remember { mutableStateOf(true) }
    var cotizacion by remember { mutableStateOf<CotizacionExtended?>(null) }
    var solicitud by remember { mutableStateOf<SolicitudCotizacionExtended?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val repo = remember { ClientRepository() }
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    LaunchedEffect(quoteId, authState.currentUser?.id_usuario) {
        isLoading = true
        try {
            // Determine client id (use authenticated user's id when available)
            val clientId = authState.currentUser?.id_usuario ?: ""
            // Try fetching cotizaciones
            val clientQuotesRes = repo.getClientQuotes(clientId, 100)
            if (clientQuotesRes.isSuccess) {
                val proc = clientQuotesRes.getOrNull()
                if (proc != null) {
                    // Response may be in proc.cotizaciones or in proc.results[0]
                    val rows = proc.cotizaciones ?: proc.results.firstOrNull() ?: emptyList()
                    val found = rows.firstOrNull { row ->
                        (row["id_cotizacion"]?.toString() ?: "") == quoteId
                    }
                    if (found != null) {
                            cotizacion = CotizacionExtended(
                                id_cotizacion = found["id_cotizacion"]?.toString() ?: "",
                                id_cliente = found["id_cliente"]?.toString() ?: "",
                                id_usuario_ventas = found["id_usuario_ventas"]?.toString() ?: "",
                                id_usuario_operativo = found["id_usuario_operativo"]?.toString(),
                                id_origen_localizacion = found["id_origen_localizacion"]?.toString() ?: "",
                                id_destino_localizacion = found["id_destino_localizacion"]?.toString() ?: "",
                                id_proveedor = found["id_proveedor"]?.toString() ?: "",
                                id_agente = found["id_agente"]?.toString(),
                                tipo_servicio = found["tipo_servicio"]?.toString() ?: "",
                                tipo_carga = found["tipo_carga"]?.toString() ?: "",
                                incoterm = found["incoterm"]?.toString() ?: "",
                                fecha_solicitud = found["fecha_solicitud"]?.toString() ?: "",
                                fecha_estimada_arribo = found["fecha_estimada_arribo"]?.toString(),
                                fecha_estimada_entrega = found["fecha_estimada_entrega"]?.toString(),
                                descripcion_mercancia = found["descripcion_mercancia"]?.toString(),
                                estatus = found["estatus"]?.toString() ?: "",
                                motivo_rechazo = found["motivo_rechazo"]?.toString(),
                                fecha_aprobacion_rechazo = found["fecha_aprobacion_rechazo"]?.toString(),
                                fecha_creacion = found["fecha_creacion"]?.toString() ?: "",
                                id_solicitud_cliente = found["id_solicitud_cliente"]?.toString(),
                                proveedorNombre = found["proveedor_nombre"]?.toString(),
                                vendedorNombre = found["vendedor_nombre"]?.toString(),
                                vendedorApellido = found["vendedor_apellido"]?.toString(),
                                origenCiudad = found["origen_ciudad"]?.toString(),
                                destinoCiudad = found["destino_ciudad"]?.toString(),
                                origenPais = found["origen_pais"]?.toString(),
                                destinoPais = found["destino_pais"]?.toString()
                            )
                        }
                    }
                }

            // If not found as cotizacion, look in solicitudes
            if (cotizacion == null) {
                val reqsRes = repo.getClientQuoteRequests(clientId, 100)
                if (reqsRes.isSuccess) {
                    val proc = reqsRes.getOrNull()
                    if (proc != null) {
                        // Response may be in proc.solicitudes or in proc.results[0]
                        val rows = proc.solicitudes ?: proc.results.firstOrNull() ?: emptyList()
                        val found = rows.firstOrNull { row -> (row["id_solicitud"]?.toString() ?: "") == quoteId }
                        if (found != null) {
                            solicitud = SolicitudCotizacionExtended(
                                id_solicitud = found["id_solicitud"]?.toString() ?: "",
                                id_cliente = found["id_cliente"]?.toString() ?: "",
                                tipo_servicio = found["tipo_servicio"]?.toString() ?: "",
                                tipo_carga = found["tipo_carga"]?.toString() ?: "",
                                origen_ciudad = found["origen_ciudad"]?.toString() ?: "",
                                origen_pais = found["origen_pais"]?.toString() ?: "",
                                destino_ciudad = found["destino_ciudad"]?.toString() ?: "",
                                destino_pais = found["destino_pais"]?.toString() ?: "",
                                fecha_solicitud = found["fecha_solicitud"]?.toString() ?: "",
                                descripcion_mercancia = found["descripcion_mercancia"]?.toString(),
                                valor_estimado_mercancia = found["valor_estimado_mercancia"]?.toString()?.toDoubleOrNull(),
                                estatus = found["estatus"]?.toString() ?: ""
                            )
                        }
                    }
                }
            }

            if (cotizacion == null && solicitud == null) {
                errorMessage = "Detalle no encontrado"
            } else {
                errorMessage = null
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar detalle: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = "Detalle de Cotización",
                onBackClick = { navController.popBackStack() }
            ) 
        }
    ) { paddingValues ->
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { 
                CircularProgressIndicator() 
            }
            errorMessage != null -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
            cotizacion != null -> QuoteDetailContentCotizacion(cotizacion = cotizacion!!, modifier = Modifier.padding(paddingValues))
            solicitud != null -> QuoteDetailContentSolicitud(solicitud = solicitud!!, modifier = Modifier.padding(paddingValues))
            else -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No se encontró información")
            }
        }
    }
}


@Composable
fun QuoteDetailContentCotizacion(cotizacion: CotizacionExtended, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Cotización #${cotizacion.id_cotizacion}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${cotizacion.tipo_servicio} - ${cotizacion.tipo_carga}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Estado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(color = getStatusColor(cotizacion.estatus), shape = MaterialTheme.shapes.medium) { Text(text = getStatusText(cotizacion.estatus), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White) }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Detalles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Proveedor", value = cotizacion.proveedorNombre ?: cotizacion.id_proveedor, icon = Icons.Default.Business)
                DetailRow(label = "Origen", value = listOfNotNull(cotizacion.origenCiudad, cotizacion.origenPais).joinToString(", "), icon = Icons.Default.Place)
                DetailRow(label = "Destino", value = listOfNotNull(cotizacion.destinoCiudad, cotizacion.destinoPais).joinToString(", "), icon = Icons.Default.Place)
                cotizacion.descripcion_mercancia?.let { DetailRow(label = "Descripción", value = it, icon = Icons.Default.Description) }
                DetailRow(label = "Fecha solicitud", value = cotizacion.fecha_solicitud, icon = Icons.Default.Schedule)
            }
        }
    }
}


@Composable
fun QuoteDetailContentSolicitud(solicitud: SolicitudCotizacionExtended, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Solicitud #${solicitud.id_solicitud}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${solicitud.tipo_servicio} - ${solicitud.tipo_carga}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Estado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(color = getStatusColor(solicitud.estatus), shape = MaterialTheme.shapes.medium) { Text(text = getStatusText(solicitud.estatus), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White) }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Detalles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(label = "Origen", value = "${solicitud.origen_ciudad}, ${solicitud.origen_pais}", icon = Icons.Default.Place)
                DetailRow(label = "Destino", value = "${solicitud.destino_ciudad}, ${solicitud.destino_pais}", icon = Icons.Default.Place)
                solicitud.descripcion_mercancia?.let { DetailRow(label = "Descripción", value = it, icon = Icons.Default.Description) }
                solicitud.valor_estimado_mercancia?.let { DetailRow(label = "Valor estimado", value = it.toString(), icon = Icons.Default.AttachMoney) }
                DetailRow(label = "Fecha solicitud", value = solicitud.fecha_solicitud, icon = Icons.Default.Schedule)
            }
        }
    }
}


// Status utils moved to com.example.crm_logistico_movil.utils.StatusUtils
