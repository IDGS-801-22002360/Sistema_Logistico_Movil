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
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.repository.ClientRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationDetailScreen(
    navController: NavController,
    operationId: String
) {
    var isLoading by remember { mutableStateOf(true) }
    var operationDetail by remember { mutableStateOf<OperationDetail?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val clientRepository = remember { ClientRepository() }
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
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun OperationDetailContent(
    operationDetail: OperationDetail,
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
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

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
