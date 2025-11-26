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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.crm_logistico_movil.components.TopAppBar
import com.example.crm_logistico_movil.components.DetailRow
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.repository.ClientRepository
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationDetailScreen(
    navController: NavController,
    operationId: String
) {
    var isLoading by remember { mutableStateOf(true) }
    var operationDetail by remember { mutableStateOf<OperationDetail?>(null) }
    var trackingInfo by remember { mutableStateOf<List<TrackingInfo>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val clientRepository = remember { com.example.crm_logistico_movil.repository.UnifiedClientRepository() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(operationId) {
        scope.launch {
            isLoading = true
            try {
                val res = clientRepository.getOperationDetail(operationId)
                if (res.isSuccess) {
                    val proc = res.getOrNull()
                    if (proc == null) {
                        errorMessage = "Respuesta vacía del servidor"
                    } else {
                        val results = proc.results
                        if (results.isEmpty()) {
                            errorMessage = "No se recibieron resultados"
                        } else {
                            // El procedimiento devuelve varios resultsets:
                            // 0 -> operación (fila única)
                            // 1 -> tracking (lista)
                            // 2 -> incidencias (lista)
                            // 3 -> demoras (lista)
                            val opList = results.getOrNull(0) ?: emptyList()
                            if (opList.isEmpty()) {
                                errorMessage = "Operación no encontrada"
                            } else {
                                val opMap = opList[0]

                                fun Any?.asStr(): String? = this?.toString()
                                fun Any?.asDoubleOrNull(): Double? = when (this) {
                                    is Number -> this.toDouble()
                                    is String -> this.toDoubleOrNull()
                                    else -> null
                                }

                                val operacion = OperationExtended(
                                    id_operacion = opMap["id_operacion"]?.toString() ?: "",
                                    id_cotizacion = opMap["id_cotizacion"]?.toString(),
                                    id_cliente = opMap["id_cliente"]?.toString() ?: "",
                                    id_usuario_operativo = opMap["id_usuario_operativo"]?.toString() ?: "",
                                    id_proveedor = opMap["id_proveedor"]?.toString() ?: "",
                                    id_agente = opMap["id_agente"]?.toString(),
                                    tipo_servicio = opMap["tipo_servicio"]?.toString() ?: "",
                                    tipo_carga = opMap["tipo_carga"]?.toString() ?: "",
                                    incoterm = opMap["incoterm"]?.toString() ?: "",
                                    fecha_inicio_operacion = opMap["fecha_inicio_operacion"]?.toString() ?: "",
                                    fecha_estimada_arribo = opMap["fecha_estimada_arribo"]?.toString(),
                                    fecha_estimada_entrega = opMap["fecha_estimada_entrega"]?.toString(),
                                    fecha_arribo_real = opMap["fecha_arribo_real"]?.toString(),
                                    fecha_entrega_real = opMap["fecha_entrega_real"]?.toString(),
                                    estatus = opMap["estatus"]?.toString() ?: "",
                                    numero_referencia_proveedor = opMap["numero_referencia_proveedor"]?.toString(),
                                    notas_operacion = opMap["notas_operacion"]?.toString(),
                                    fecha_creacion = opMap["fecha_creacion"]?.toString() ?: "",
                                    proveedorNombre = opMap["proveedor_nombre"]?.toString(),
                                    operativoNombre = opMap["operativo_nombre"]?.toString(),
                                    operativoApellido = opMap["operativo_apellido"]?.toString()
                                )

                                val trackingList = (results.getOrNull(1) ?: emptyList()).map { m ->
                                    TrackingInfo(
                                        id_tracking = m["id_tracking"]?.toString() ?: "",
                                        fecha_hora_actualizacion = m["fecha_hora_actualizacion"]?.toString() ?: "",
                                        ubicacion_actual = m["ubicacion_actual"]?.toString(),
                                        latitud = m["latitud"]?.asDoubleOrNull(),
                                        longitud = m["longitud"]?.asDoubleOrNull(),
                                        estatus_seguimiento = m["estatus_seguimiento"]?.toString() ?: "",
                                        referencia_transportista = m["referencia_transportista"]?.toString(),
                                        nombre_transportista = m["nombre_transportista"]?.toString(),
                                        notas_tracking = m["notas_tracking"]?.toString()
                                    )
                                }

                                val incidenciasList = (results.getOrNull(2) ?: emptyList()).map { m ->
                                    IncidenciaInfo(
                                        id_incidencia = m["id_incidencia"]?.toString() ?: "",
                                        fecha_hora_incidencia = m["fecha_hora_incidencia"]?.toString() ?: "",
                                        descripcion_incidencia = m["descripcion_incidencia"]?.toString() ?: "",
                                        tipo_incidencia = m["tipo_incidencia"]?.toString() ?: "",
                                        estatus = m["estatus"]?.toString() ?: "",
                                        fecha_resolucion = m["fecha_resolucion"]?.toString(),
                                        comentarios_resolucion = m["comentarios_resolucion"]?.toString()
                                    )
                                }

                                val demorasList = (results.getOrNull(3) ?: emptyList()).map { m ->
                                    DemoraInfo(
                                        id_demora = m["id_demora"]?.toString() ?: "",
                                        fecha_hora_demora = m["fecha_hora_demora"]?.toString() ?: "",
                                        descripcion_demora = m["descripcion_demora"]?.toString(),
                                        tipo_demora = m["tipo_demora"]?.toString() ?: "",
                                        costo_asociado = m["costo_asociado"]?.let { v -> when (v) {
                                            is Number -> v.toDouble()
                                            is String -> v.toDoubleOrNull() ?: 0.0
                                            else -> 0.0
                                        } } ?: 0.0,
                                        moneda = m["moneda"]?.toString()
                                    )
                                }

                                operationDetail = OperationDetail(
                                    operacion = operacion,
                                    tracking = trackingList,
                                    incidencias = incidenciasList,
                                    demoras = demorasList
                                )
                            }
                        }
                    }
                } else {
                    // Error en la llamada a la API
                    errorMessage = res.exceptionOrNull()?.message ?: "Error al obtener detalle"
                }
                // Obtener información de tracking adicional desde la nueva API
                try {
                    val trackingRes = clientRepository.getTrackingPorOperacion(operationId)
                    if (trackingRes.isSuccess) {
                        val trackingData = trackingRes.getOrNull() ?: emptyList()
                        trackingInfo = trackingData.map { m ->
                            fun Any?.asDoubleOrNull(): Double? = when (this) {
                                is Number -> this.toDouble()
                                is String -> this.toDoubleOrNull()
                                else -> null
                            }

                            TrackingInfo(
                                id_tracking = m["id_tracking"]?.toString() ?: "",
                                fecha_hora_actualizacion = m["fecha_hora_actualizacion"]?.toString() ?: "",
                                ubicacion_actual = m["ubicacion_actual"]?.toString(),
                                latitud = m["latitud"]?.asDoubleOrNull(),
                                longitud = m["longitud"]?.asDoubleOrNull(),
                                estatus_seguimiento = m["estatus_seguimiento"]?.toString() ?: "",
                                referencia_transportista = m["referencia_transportista"]?.toString(),
                                nombre_transportista = m["nombre_transportista"]?.toString(),
                                notas_tracking = m["notas_tracking"]?.toString()
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Error al obtener tracking adicional, pero no cancela la operación principal
                    println("Error al obtener tracking: ${e.message}")
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar detalle: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = "Detalle de Operación",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Volver")
                        }
                    }
                }
            }
            
            operationDetail != null -> {
                OperationDetailContent(
                    operationDetail = operationDetail!!,
                    trackingInfo = trackingInfo,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun OperationDetailContent(
    operationDetail: OperationDetail,
    trackingInfo: List<TrackingInfo>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val op = operationDetail.operacion

        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Operación #${op.id_operacion}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${op.tipo_servicio} - ${op.tipo_carga}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Estado Actual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = getStatusColor(op.estatus),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = getStatusText(op.estatus),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Route Information
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Información de Ruta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Mostrar información disponible relacionada a la operación
                DetailRow(
                    label = "Proveedor",
                    value = op.proveedorNombre ?: op.id_proveedor,
                    icon = Icons.Default.Business
                )

                val operativoNombre = listOfNotNull(op.operativoNombre, op.operativoApellido).joinToString(" ").takeIf { it.isNotBlank() }
                operativoNombre?.let {
                    DetailRow(
                        label = "Operativo",
                        value = it,
                        icon = Icons.Default.Person
                    )
                }

                DetailRow(
                    label = "Incoterm",
                    value = op.incoterm ?: "-",
                    icon = Icons.Default.AccountBalance
                )
            }
        }
        
        // Dates Information
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Fechas Importantes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                DetailRow(
                    label = "Inicio de Operación",
                    value = op.fecha_inicio_operacion,
                    icon = Icons.Default.Start
                )
                
                op.fecha_estimada_arribo?.let { fecha ->
                    DetailRow(
                        label = "Arribo Estimado",
                        value = fecha,
                        icon = Icons.Default.Schedule
                    )
                }
                
                op.fecha_estimada_entrega?.let { fecha ->
                    DetailRow(
                        label = "Entrega Estimada",
                        value = fecha,
                        icon = Icons.Default.DeliveryDining
                    )
                }
                
                op.fecha_arribo_real?.let { fecha ->
                    DetailRow(
                        label = "Arribo Real",
                        value = fecha,
                        icon = Icons.Default.CheckCircle
                    )
                }
                
                op.fecha_entrega_real?.let { fecha ->
                    DetailRow(
                        label = "Entrega Real",
                        value = fecha,
                        icon = Icons.Default.CheckCircle
                    )
                }
            }
        }
        
        // Provider Information
        op.proveedorNombre?.let { proveedor ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información del Proveedor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DetailRow(
                        label = "Proveedor",
                        value = proveedor,
                        icon = Icons.Default.Business
                    )
                    
                    op.numero_referencia_proveedor?.let { referencia ->
                        DetailRow(
                            label = "Referencia",
                            value = referencia,
                            icon = Icons.Default.Tag
                        )
                    }
                }
            }
        }
        
        // Notes
        op.notas_operacion?.takeIf { it.isNotBlank() }?.let { notas ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Observaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = notas,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Mapa de Tracking
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
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
                        text = "Ubicación y Tracking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    var isMapAvailable by remember {mutableStateOf(true)}

                    if (isMapAvailable) {
                        // Usar coordenadas reales del tracking o posición por defecto
                        val latestTracking = trackingInfo.lastOrNull { it.latitud != null && it.longitud != null }
                        val position = if (latestTracking != null) {
                            LatLng(latestTracking.latitud!!, latestTracking.longitud!!)
                        } else {
                            LatLng(19.4326, -99.1332) // Ciudad de México por defecto
                        }

                        val cameraPositionState = rememberCameraPositionState {
                            this.position = CameraPosition.fromLatLngZoom(position, 13f)
                        }

                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {
                            // Marcador con coordenadas reales o por defecto
                            Marker(
                                state = rememberMarkerState(position = position),
                                title = "Operación ${op.id_operacion}",
                                snippet = if (latestTracking != null) {
                                    "Última ubicación: ${latestTracking.ubicacion_actual ?: "Coordenadas GPS"}"
                                } else {
                                    "Ubicación por defecto - Sin coordenadas disponibles"
                                }
                            )
                        }
                    } else {
                        // Fallback si Google Maps no está disponible
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Mapa de tracking",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Próximamente disponible",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Información de Tracking Detallada
        if (trackingInfo.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
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
                            text = "Historial de Tracking",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mostrar los últimos 3 registros de tracking
                    trackingInfo.takeLast(3).reversed().forEachIndexed { index, tracking ->
                        if (index > 0) {
                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }

                        TrackingItemCard(tracking = tracking)
                    }

                    if (trackingInfo.size > 3) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "y ${trackingInfo.size - 3} registros más...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TrackingItemCard(tracking: TrackingInfo) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Fecha y estatus
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tracking.fecha_hora_actualizacion.take(16).replace("T", " "), // Formato de fecha simplificado
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Surface(
                color = getTrackingStatusColor(tracking.estatus_seguimiento),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = tracking.estatus_seguimiento,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Ubicación
        tracking.ubicacion_actual?.let { ubicacion ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = ubicacion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Coordenadas si están disponibles
        if (tracking.latitud != null && tracking.longitud != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.GpsFixed,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Lat: %.6f, Lng: %.6f".format(tracking.latitud, tracking.longitud),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Transportista
        tracking.nombre_transportista?.let { transportista ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = transportista,
                    style = MaterialTheme.typography.bodyMedium
                )
                tracking.referencia_transportista?.let { referencia ->
                    Text(
                        text = "($referencia)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Notas
        tracking.notas_tracking?.takeIf { it.isNotBlank() }?.let { notas ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Notes,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = notas,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun getTrackingStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "EN_TRANSITO", "EN_RUTA" -> Color(0xFF1976D2)
        "ENTREGADO", "COMPLETADO" -> Color(0xFF388E3C)
        "RETENIDO", "EN_ADUANA" -> Color(0xFFF57C00)
        "PENDIENTE", "ESPERANDO" -> Color(0xFFE91E63)
        "CANCELADO", "PERDIDO" -> Color(0xFF757575)
        "ORIGEN", "RECOGIDO" -> Color(0xFF6200EE)
        else -> Color(0xFF607D8B)
    }
}

// DetailRow moved to com.example.crm_logistico_movil.components.DetailRow

private fun getStatusColor(status: String): Color {
    return when (status) {
        "EN_TRANSITO" -> Color(0xFF1976D2)
        "ENTREGADO" -> Color(0xFF388E3C)
        "EN_ADUANA" -> Color(0xFFF57C00)
        "PENDIENTE_DOCUMENTOS" -> Color(0xFFE91E63)
        "CANCELADO" -> Color(0xFF757575)
        else -> Color(0xFF6200EE)
    }
}

private fun getStatusText(status: String): String {
    return when (status) {
        "EN_TRANSITO" -> "En Tránsito"
        "ENTREGADO" -> "Entregado"
        "EN_ADUANA" -> "En Aduana"
        "PENDIENTE_DOCUMENTOS" -> "Pendiente Documentos"
        "CANCELADO" -> "Cancelado"
        else -> status
    }
}
