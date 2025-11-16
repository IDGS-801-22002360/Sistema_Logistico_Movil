package com.example.crm_logistico_movil.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.repository.ClientRepository

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

    private val clientRepository = ClientRepository()

    private val _uiState = MutableStateFlow(ClientDashboardUiState())
    val uiState: StateFlow<ClientDashboardUiState> = _uiState.asStateFlow()

    fun loadDashboardData(clientId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // En lugar de usar el procedimiento que no funciona,
                // calculamos las estadísticas usando las APIs existentes

                // 1. Cargar todas las operaciones para contar activas
                val operationsResult = clientRepository.getClientOperations(clientId, limit = 100)

                // 2. Cargar todas las facturas para contar pendientes
                val facturasResult = clientRepository.getFacturasCliente(clientId)

                // 3. Cargar solicitudes de cotización
                val requestsResult = clientRepository.getClientQuoteRequests(clientId, limit = 100)

                // Calcular estadísticas
                var operacionesActivas = 0
                var facturasPendientes = 0
                var solicitudesPendientes = 0

                // Contar operaciones activas (no entregadas ni canceladas)
                if (operationsResult.isSuccess) {
                    val proc = operationsResult.getOrNull()
                    val rows = proc?.results?.getOrNull(0) ?: emptyList()
                    operacionesActivas = rows.count { row ->
                        val estatus = row["estatus"]?.toString()?.lowercase() ?: ""
                        estatus !in listOf("entregado", "cancelado", "finalizado")
                    }
                }

                // Contar facturas pendientes (no pagadas)
                if (facturasResult.isSuccess) {
                    val facturas = facturasResult.getOrNull() ?: emptyList()
                    facturasPendientes = facturas.count { factura ->
                        factura.estatus.lowercase() in listOf("pendiente", "vencida")
                    }
                }

                // Contar solicitudes pendientes (no respondidas)
                if (requestsResult.isSuccess) {
                    val proc = requestsResult.getOrNull()
                    val rows = proc?.results?.getOrNull(0) ?: emptyList()
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

                // Cargar solicitudes recientes (máximo 5)
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

    private fun mapToSolicitudes(proc: ProcedureResponse?): List<SolicitudCotizacionExtended> {
        val rows = proc?.results?.getOrNull(0) ?: emptyList()
        return rows.map { m ->
            SolicitudCotizacionExtended(
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
    }

    fun refreshData(clientId: String) {
        loadDashboardData(clientId)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}