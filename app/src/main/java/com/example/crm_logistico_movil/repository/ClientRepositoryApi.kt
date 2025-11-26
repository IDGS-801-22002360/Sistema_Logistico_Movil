package com.example.crm_logistico_movil.repository

import com.example.crm_logistico_movil.api.ApiClient
import com.example.crm_logistico_movil.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Nueva implementación del ClientRepository que usa las APIs REST de Render
 * en lugar de stored procedures y conexión directa a MySQL.
 *
 * URL Base: https://pwa-sistema-logistico-backend.onrender.com/movil/
 *
 * Endpoints implementados:
 * - GET /cliente/{client_id}/operaciones - Listar operaciones del cliente
 * - GET /cliente/{client_id}/cotizaciones - Listar cotizaciones del cliente
 * - GET /cliente/{client_id}/solicitudes - Listar solicitudes del cliente
 * - GET /cliente/{client_id}/facturas - Listar facturas del cliente
 * - GET /cliente/{client_id}/info - Información completa del cliente
 * - PUT /cliente/{client_id} - Editar información del cliente
 * - GET /tracking/{id_operacion} - Tracking de operación
 * - POST /cliente/{client_id}/crear_solicitud - Crear solicitud de cotización
 */
class ClientRepositoryAPI {
    private val apiService = ApiClient.apiService

    /**
     * Obtener operaciones del cliente usando el endpoint REST dedicado
     * Endpoint: GET /cliente/{client_id}/operaciones
     */
    suspend fun getClientOperations(clientId: String, limit: Int = 10, offset: Int = 0): Result<OperacionesListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOperacionesCliente(clientId, limit, offset)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Obtener cotizaciones del cliente
     * Endpoint: GET /cliente/{client_id}/cotizaciones
     */
    suspend fun getClientQuotes(clientId: String, limit: Int = 10, offset: Int = 0): Result<CotizacionesListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCotizacionesCliente(clientId, limit, offset)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Obtener solicitudes de cotización del cliente
     * Endpoint: GET /cliente/{client_id}/solicitudes
     */
    suspend fun getClientQuoteRequests(clientId: String, limit: Int = 10, offset: Int = 0): Result<SolicitudesListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSolicitudesCliente(clientId, limit, offset)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Obtener facturas del cliente
     * Endpoint: GET /cliente/{client_id}/facturas
     */
    suspend fun getClientInvoices(clientId: String): Result<List<FacturaClienteTmp>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFacturasCliente(clientId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Obtener información completa del cliente
     * Endpoint: GET /cliente/{client_id}/info
     */
    suspend fun getClientInfo(clientId: String): Result<Map<String, Any>?> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getClienteInfo(clientId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.cliente)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Editar información del cliente
     * Endpoint: PUT /cliente/{client_id}
     */
    suspend fun editClient(clientId: String, payload: Map<String, Any?>): Result<EditClientResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.editarCliente(clientId, payload)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Obtener tracking de una operación
     * Endpoint: GET /tracking/{id_operacion}
     */
    suspend fun getTrackingByOperation(operationId: String): Result<List<Map<String, Any>>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTrackingPorOperacion(operationId)
            if (response.isSuccessful) {
                response.body()?.let { trackingResponse ->
                    Result.success(trackingResponse.tracking)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Crear solicitud de cotización para el cliente
     * Endpoint: POST /cliente/{client_id}/crear_solicitud
     */
    suspend fun createQuoteRequest(clientId: String, solicitudRequest: SolicitudRequest): Result<SolicitudResponse> = withContext(Dispatchers.IO) {
        try {
            // Crear body sin id_cliente
            val requestBody = SolicitudRequestBody(
                tipo_servicio = solicitudRequest.tipo_servicio,
                tipo_carga = solicitudRequest.tipo_carga,
                origen_ciudad = solicitudRequest.origen_ciudad,
                origen_pais = solicitudRequest.origen_pais,
                destino_ciudad = solicitudRequest.destino_ciudad,
                destino_pais = solicitudRequest.destino_pais,
                descripcion_mercancia = solicitudRequest.descripcion_mercancia,
                valor_estimado_mercancia = solicitudRequest.valor_estimado_mercancia
            )

            val response = apiService.crearSolicitudCliente(clientId, requestBody)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Obtener detalle específico de una operación
     * Endpoint: GET /operacion/{id_operacion}
     */
    suspend fun getOperationDetail(operationId: String): Result<Map<String, Any>?> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOperacionDetalle(operationId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Obtener detalle específico de una factura
     * Endpoint: GET /factura/{id_factura}
     */
    suspend fun getInvoiceDetail(invoiceId: String): Result<FacturaClienteTmp?> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFacturaDetalle(invoiceId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Llamar stored procedure genérica (fallback)
     * Endpoint: POST /call/{procName}
     */
    suspend fun callStoredProcedure(procName: String, params: Map<String, Any?>): Result<ProcedureResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.callProcedure(procName, params)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                Result.failure(Exception("Error ${response.code()}: ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}