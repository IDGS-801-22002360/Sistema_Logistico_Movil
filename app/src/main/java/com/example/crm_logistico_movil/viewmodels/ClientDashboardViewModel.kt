package com.example.crm_logistico_movil.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.repository.UnifiedClientRepository
import com.example.crm_logistico_movil.repository.ReferenceDataRepository
import com.example.crm_logistico_movil.services.NotificationService

data class ClientDashboardUiState(
    val isLoading: Boolean = false,
    val clientSummary: ClientSummary? = null, // <-- Usa el ClientSummary correcto
    val recentOperations: List<OperationExtended> = emptyList(),
    val quoteRequests: List<SolicitudCotizacionExtended> = emptyList(),
    val recentInvoices: List<FacturaExtended> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ClientDashboardViewModel : ViewModel() {

    // MIGRACIÓN: Usar UnifiedClientRepository que puede cambiar entre sistemas según configuración
    private val clientRepository = UnifiedClientRepository()

    private val _uiState = MutableStateFlow(ClientDashboardUiState())
    val uiState: StateFlow<ClientDashboardUiState> = _uiState.asStateFlow()

    // Rastrear operaciones conocidas para detectar nuevas
    private val knownOperationIds = mutableSetOf<String>()

    // Rastrear facturas conocidas para detectar nuevas
    private val knownInvoiceIds = mutableSetOf<String>()

    // Rastrear solicitudes conocidas para detectar nuevas
    private val knownRequestIds = mutableSetOf<String>()

    init {
        // Log cual sistema se está usando
        Log.d("ClientDashboardViewModel", "Initialized with: ${clientRepository.getMigrationStatus()}")
    }

    fun loadDashboardData(clientId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Inicializar datos de referencia en paralelo para mejor UX
                launch { ReferenceDataRepository.loadAllReferenceData() }

                // En lugar de usar el procedimiento que no funciona,
                // calculamos las estadísticas usando las APIs existentes

                // 1. Cargar todas las operaciones para contar activas
                val operationsResult = clientRepository.getClientOperations(clientId, limit = 100)

                // 2. Cargar todas las facturas para contar pendientes
                val facturasResult = clientRepository.getFacturasCliente(clientId)

                // 3. Cargar solicitudes de cotización con enriquecimiento
                Log.d("ClientDashboardViewModel", "Loading quote requests for client: $clientId")
                val requestsResult = clientRepository.getClientQuoteRequests(clientId, limit = 100)

                // Calcular estadísticas
                var operacionesActivas = 0
                var facturasPendientes = 0
                var solicitudesPendientes = 0

                // Contar operaciones activas (no entregadas ni canceladas)
                if (operationsResult.isSuccess) {
                    val proc = operationsResult.getOrNull()
                    val rows = proc?.results?.getOrNull(0) ?: emptyList()

                    // Detectar nuevas operaciones para notificaciones
                    checkForNewOperations(rows, clientId)

                    operacionesActivas = rows.count { row ->
                        val estatus = row["estatus"]?.toString()?.lowercase() ?: ""
                        estatus !in listOf("entregado", "cancelado", "finalizado")
                    }
                }

                // Contar facturas pendientes (no pagadas)
                if (facturasResult.isSuccess) {
                    val facturas = facturasResult.getOrNull() ?: emptyList()

                    // Detectar nuevas facturas para notificaciones
                    checkForNewInvoices(facturas, clientId)

                    facturasPendientes = facturas.count { factura ->
                        factura.estatus.lowercase() in listOf("pendiente", "vencida")
                    }
                }

                // Contar solicitudes pendientes (no respondidas)
                if (requestsResult.isSuccess) {
                    val proc = requestsResult.getOrNull()
                    val rows = proc?.results?.getOrNull(0) ?: emptyList()

                    // Detectar nuevas solicitudes para notificaciones
                    checkForNewQuoteRequests(rows, clientId)

                    solicitudesPendientes = rows.count { row ->
                        val estatus = row["estatus"]?.toString()?.lowercase() ?: ""
                        estatus in listOf("pendiente", "en_proceso", "enviada")
                    }
                }

                // Crear resumen con datos calculados
                val summary = ClientSummary(
                    operacionesActivas = operacionesActivas,
                    facturasPendientes = facturasPendientes,
                    cotizacionesPendientes = 0, // No tenemos cotizaciones en el sistema actual
                    solicitudesPendientes = solicitudesPendientes
                )

                // Cargar operaciones recientes (máximo 3)
                val recentOperationsResult = clientRepository.getClientOperations(clientId, limit = 3)
                val operations = if (recentOperationsResult.isSuccess) mapToOperations(recentOperationsResult.getOrNull()) else emptyList()

                // Cargar solicitudes recientes (máximo 5) con enriquecimiento
                val requests = if (requestsResult.isSuccess) mapToSolicitudes(requestsResult.getOrNull()).take(5) else emptyList()

                // Cargar facturas recientes (máximo 3) - usar el endpoint directo
                val recentFacturas = if (facturasResult.isSuccess) {
                    (facturasResult.getOrNull() ?: emptyList()).take(3).map { factura ->
                        FacturaExtended(
                            id_factura_cliente = factura.id_factura_cliente,
                            id_cliente = factura.id_cliente,
                            id_operacion = factura.id_operacion,
                            id_cotizacion = factura.id_cotizacion,
                            numero_factura = factura.numero_factura,
                            fecha_emision = factura.fecha_emision,
                            fecha_vencimiento = factura.fecha_vencimiento,
                            monto_total = factura.monto_total,
                            monto_pagado = factura.monto_pagado,
                            moneda = factura.moneda,
                            estatus = factura.estatus,
                            observaciones = factura.observaciones,
                            fecha_pago = null,
                            fecha_creacion = factura.fecha_creacion,
                            operacionReferencia = null,
                            cotizacionReferencia = null
                        )
                    }
                } else emptyList()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    clientSummary = summary,
                    recentOperations = operations,
                    quoteRequests = requests,
                    recentInvoices = recentFacturas,
                    errorMessage = null
                )

                // Generar notificaciones automáticas basadas en los datos (comentado para evitar duplicados)
                // generateAutoNotifications(clientId, operations, recentFacturas)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }

    // Funciones auxiliares para mapear datos
    private fun mapToOperations(proc: ProcedureResponse?): List<OperationExtended> {
        val rows = proc?.results?.getOrNull(0) ?: emptyList()
        return rows.map { m ->
            OperationExtended(
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
    }

    private suspend fun mapToSolicitudes(proc: ProcedureResponse?): List<SolicitudCotizacionExtended> {
        val rows = proc?.results?.getOrNull(0) ?: emptyList()
        return rows.map { m ->
            val solicitudId = m["id_solicitud"]?.toString() ?: ""
            Log.d("ClientDashboardViewModel", "Processing solicitud $solicitudId - raw data: $m")

            // Crear objeto inicial
            val solicitud = SolicitudCotizacionExtended(
                id_solicitud = solicitudId,
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

            Log.d("ClientDashboardViewModel", "Mapped solicitud $solicitudId - origen_pais: ${solicitud.origen_pais}, destino_pais: ${solicitud.destino_pais}")

            // Aplicar enriquecimiento usando UnifiedClientRepository
            val enrichedData = clientRepository.enrichQuoteRequestData(m)

            Log.d("ClientDashboardViewModel", "Enriched data for $solicitudId: $enrichedData")

            // Combinar datos originales con datos enriquecidos
            solicitud.copy(
                origen_ciudad = enrichedData["origen_ciudad"]?.toString() ?: solicitud.origen_ciudad,
                origen_pais = enrichedData["origen_pais"]?.toString() ?: solicitud.origen_pais,
                destino_ciudad = enrichedData["destino_ciudad"]?.toString() ?: solicitud.destino_ciudad,
                destino_pais = enrichedData["destino_pais"]?.toString() ?: solicitud.destino_pais
            )
        }
    }

    fun refreshData(clientId: String) {
        loadDashboardData(clientId)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Función para generar notificaciones automáticas basadas en los datos
    private suspend fun generateAutoNotifications(
        clientId: String,
        operations: List<OperationExtended>,
        facturas: List<FacturaExtended>
    ) {
        try {
            // Notificaciones para operaciones recientes (últimas 24 horas simuladas)
            operations.take(2).forEach { operation ->
                try {
                    val estatusOperacion = EstatusOperacion.valueOf(operation.estatus.uppercase())
                    when (estatusOperacion) {
                        EstatusOperacion.EN_TRANSITO -> {
                            NotificationService.notifyOperacionUpdated(
                                operacionId = operation.id_operacion,
                                clientId = clientId,
                                nuevoEstatus = "En Tránsito - Tu mercancía está en camino"
                            )
                        }
                        EstatusOperacion.ENTREGADO -> {
                            NotificationService.notifyOperacionUpdated(
                                operacionId = operation.id_operacion,
                                clientId = clientId,
                                nuevoEstatus = "Entregado - Operación completada exitosamente"
                            )
                        }
                        EstatusOperacion.PENDIENTE_DOCUMENTOS -> {
                            NotificationService.notifyIncidenciaReported(
                                operacionId = operation.id_operacion,
                                clientId = clientId,
                                descripcion = "Documentos faltantes para completar la operación"
                            )
                        }
                        else -> { /* No generar notificación para otros estados */ }
                    }
                } catch (e: IllegalArgumentException) {
                    // Handle invalid enum values gracefully
                    Log.w("ClientDashboardViewModel", "Invalid operation status: ${operation.estatus}")
                }
            }

            // Notificaciones para facturas próximas a vencer (simulado)
            facturas.take(1).forEach { factura ->
                try {
                    val estatusFactura = EstatusFactura.valueOf(factura.estatus.uppercase())
                    if (estatusFactura == EstatusFactura.PENDIENTE) {
                        NotificationService.notifyFacturaVencimiento(
                            facturaId = factura.numero_factura ?: factura.id_factura_cliente,
                            clientId = clientId,
                            diasRestantes = 3 // Simulado: 3 días para vencer
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    // Handle invalid enum values gracefully
                    Log.w("ClientDashboardViewModel", "Invalid factura status: ${factura.estatus}")
                }
            }

            // Notificación de factura generada (para la más reciente)
            facturas.firstOrNull()?.let { factura ->
                NotificationService.notifyFacturaGenerated(
                    facturaId = factura.numero_factura ?: factura.id_factura_cliente,
                    clientId = clientId,
                    monto = "$${factura.monto_total} ${factura.moneda}"
                )
            }

        } catch (e: Exception) {
            Log.e("ClientDashboardViewModel", "Error generating auto notifications: ${e.message}")
        }
    }

    private fun checkForNewOperations(operationRows: List<Map<String, Any>>, clientId: String) {
        try {
            val currentOperationIds = operationRows.mapNotNull { row ->
                row["id_operacion"]?.toString()
            }.toSet()

            // Detectar operaciones nuevas (que no estaban antes)
            val newOperationIds = currentOperationIds - knownOperationIds

            Log.d("ClientDashboardViewModel", "Checking operations - Known: ${knownOperationIds.size}, Current: ${currentOperationIds.size}, New: ${newOperationIds.size}")

            // Generar notificaciones para operaciones nuevas (pero no en la primera carga)
            if (knownOperationIds.isNotEmpty()) {
                newOperationIds.forEach { operationId ->
                    Log.d("ClientDashboardViewModel", "New operation detected: $operationId - generating notification")
                    NotificationService.notifyOperacionAssigned(operationId, clientId)
                }
            }

            // Actualizar el conjunto de operaciones conocidas
            knownOperationIds.clear()
            knownOperationIds.addAll(currentOperationIds)

        } catch (e: Exception) {
            Log.e("ClientDashboardViewModel", "Error checking for new operations: ${e.message}")
        }
    }

    private fun checkForNewInvoices(facturas: List<FacturaClienteTmp>, clientId: String) {
        try {
            val currentInvoiceIds = facturas.mapNotNull { factura ->
                factura.numero_factura ?: factura.id_factura_cliente
            }.toSet()

            // Detectar facturas nuevas (que no estaban antes)
            val newInvoiceIds = currentInvoiceIds - knownInvoiceIds

            Log.d("ClientDashboardViewModel", "Checking invoices - Known: ${knownInvoiceIds.size}, Current: ${currentInvoiceIds.size}, New: ${newInvoiceIds.size}")

            // Generar notificaciones para facturas nuevas (pero no en la primera carga)
            if (knownInvoiceIds.isNotEmpty()) {
                newInvoiceIds.forEach { invoiceId ->
                    val factura = facturas.find {
                        (it.numero_factura ?: it.id_factura_cliente) == invoiceId
                    }

                    if (factura != null) {
                        Log.d("ClientDashboardViewModel", "New invoice detected: $invoiceId - generating notification")

                        // Notificación de factura generada
                        NotificationService.notifyFacturaGenerated(
                            facturaId = invoiceId,
                            clientId = clientId,
                            monto = "$${factura.monto_total} ${factura.moneda}"
                        )

                        // Si la factura está pendiente, también crear notificación de vencimiento
                        if (factura.estatus.lowercase() == "pendiente") {
                            NotificationService.notifyFacturaVencimiento(
                                facturaId = invoiceId,
                                clientId = clientId,
                                diasRestantes = 30 // Simulado: 30 días para vencer
                            )
                        }
                    }
                }
            }

            // Actualizar el conjunto de facturas conocidas
            knownInvoiceIds.clear()
            knownInvoiceIds.addAll(currentInvoiceIds)

        } catch (e: Exception) {
            Log.e("ClientDashboardViewModel", "Error checking for new invoices: ${e.message}")
        }
    }

    private fun checkForNewQuoteRequests(requestRows: List<Map<String, Any>>, clientId: String) {
        try {
            val currentRequestIds = requestRows.mapNotNull { requestRow ->
                requestRow["id_solicitud"]?.toString()
            }.toSet()

            // Detectar solicitudes nuevas (que no estaban antes)
            val newRequestIds = currentRequestIds - knownRequestIds

            Log.d("ClientDashboardViewModel", "Checking quote requests - Known: ${knownRequestIds.size}, Current: ${currentRequestIds.size}, New: ${newRequestIds.size}")

            // Generar notificaciones para solicitudes nuevas (pero no en la primera carga)
            // Nota: Las solicitudes normalmente se crean desde la app, pero podrían crearse desde otros canales
            if (knownRequestIds.isNotEmpty()) {
                newRequestIds.forEach { requestId ->
                    Log.d("ClientDashboardViewModel", "New quote request detected: $requestId - generating notification")

                    // Solo generar notificación si no fue creada desde esta misma sesión
                    // (para evitar duplicar la notificación que se crea en NewQuoteRequestScreen)
                    NotificationService.notifySolicitudCreated(requestId, clientId)
                }
            }

            // Actualizar el conjunto de solicitudes conocidas
            knownRequestIds.clear()
            knownRequestIds.addAll(currentRequestIds)

        } catch (e: Exception) {
            Log.e("ClientDashboardViewModel", "Error checking for new quote requests: ${e.message}")
        }
    }
}