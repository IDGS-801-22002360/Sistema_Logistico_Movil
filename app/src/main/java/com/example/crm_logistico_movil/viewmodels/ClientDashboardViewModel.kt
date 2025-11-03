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
                // Cargar resumen del cliente

                val summaryResult = clientRepository.getClientSummary(clientId)

                // Cargar operaciones recientes (máximo 3)
                val operationsResult = clientRepository.getClientOperations(clientId, limit = 3)

                // Cargar solicitudes de cotización (máximo 5)
                val requestsResult = clientRepository.getClientQuoteRequests(clientId, limit = 5)

                // Cargar facturas recientes (máximo 3)
                val invoicesResult = clientRepository.getClientInvoices(clientId, limit = 3)

                // Parse results
                fun mapToClientSummary(proc: ProcedureResponse?): ClientSummary? {
                    val firstRow = proc?.results?.getOrNull(0)?.getOrNull(0) ?: return null
                    val operaciones = firstRow["operaciones_activas"]?.toString()?.toIntOrNull() ?: 0
                    val facturas = firstRow["facturas_pendientes"]?.toString()?.toIntOrNull() ?: 0
                    val cotizaciones = firstRow["cotizaciones_pendientes"]?.toString()?.toIntOrNull() ?: 0
                    val solicitudes = firstRow["solicitudes_pendientes"]?.toString()?.toIntOrNull() ?: 0
                    return ClientSummary(operaciones, facturas, cotizaciones, solicitudes)
                }

                fun mapToOperations(proc: ProcedureResponse?): List<OperationExtended> {
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

                fun mapToSolicitudes(proc: ProcedureResponse?): List<SolicitudCotizacionExtended> {
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

                fun mapToFacturas(proc: ProcedureResponse?): List<FacturaExtended> {
                    val rows = proc?.results?.getOrNull(0) ?: emptyList()
                    return rows.map { m ->
                        FacturaExtended(
                            id_factura_cliente = m["id_factura_cliente"]?.toString() ?: "",
                            id_cliente = m["id_cliente"]?.toString() ?: "",
                            id_operacion = m["id_operacion"]?.toString(),
                            id_cotizacion = m["id_cotizacion"]?.toString(),
                            numero_factura = m["numero_factura"]?.toString() ?: "",
                            fecha_emision = m["fecha_emision"]?.toString() ?: "",
                            fecha_vencimiento = m["fecha_vencimiento"]?.toString() ?: "",
                            monto_total = m["monto_total"]?.toString()?.toDoubleOrNull() ?: 0.0,
                            monto_pagado = m["monto_pagado"]?.toString()?.toDoubleOrNull() ?: 0.0,
                            moneda = m["moneda"]?.toString() ?: "",
                            estatus = m["estatus"]?.toString() ?: "",
                            observaciones = m["observaciones"]?.toString(),
                            fecha_pago = m["fecha_pago"]?.toString(),
                            fecha_creacion = m["fecha_creacion"]?.toString() ?: "",
                            operacionReferencia = m["operacion_referencia"]?.toString(),
                            cotizacionReferencia = m["cotizacion_referencia"]?.toString()
                        )
                    }
                }

                // Si necesitas cargar cotizaciones (CotizacionExtended) por separado, descomenta y úsalo
                // val quotesResponse = clientRepository.getClientQuotes(clientId, limit = 3)


                val summary = if (summaryResult.isSuccess) mapToClientSummary(summaryResult.getOrNull()) else null
                val operations = if (operationsResult.isSuccess) mapToOperations(operationsResult.getOrNull()) else emptyList()
                val requests = if (requestsResult.isSuccess) mapToSolicitudes(requestsResult.getOrNull()) else emptyList()
                val invoices = if (invoicesResult.isSuccess) mapToFacturas(invoicesResult.getOrNull()) else emptyList()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    clientSummary = summary,
                    recentOperations = operations,
                    quoteRequests = requests,
                    recentInvoices = invoices,
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