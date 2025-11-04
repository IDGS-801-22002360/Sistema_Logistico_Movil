package com.example.crm_logistico_movil.repository

import com.example.crm_logistico_movil.api.ApiClient
import com.example.crm_logistico_movil.models.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClientRepository {
    private val apiService = ApiClient.apiService

    suspend fun createSolicitud(solicitudRequest: SolicitudRequest): Result<SolicitudResponse> = withContext(Dispatchers.IO) {
        try {
            // Prefer the client-specific endpoint if available (backend path: /cliente/{client_id}/crear_solicitud)
            val clientId = solicitudRequest.id_cliente
            val response = if (!clientId.isNullOrBlank()) {
                apiService.crearSolicitudCliente(clientId, solicitudRequest)
            } else {
                apiService.crearSolicitud(solicitudRequest)
            }
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getClientSummary(clientId: String): Result<ProcedureResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.callProcedure(
                "sp_obtener_resumen_cliente",
                mapOf("params" to listOf(clientId))
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClientOperations(clientId: String, limit: Int? = null, offset: Int? = null): Result<ProcedureResponse> = withContext(Dispatchers.IO) {
        try {
            // Call dedicated endpoint if available on backend
            val resp = apiService.getOperacionesCliente(clientId, limit ?: 10, offset ?: 0)
            if (resp.isSuccessful) {
                val body = resp.body()
                val rows = body?.operaciones ?: emptyList()
                // adapt to ProcedureResponse shape expected by ViewModel
                val proc = ProcedureResponse(args = emptyList(), results = listOf(rows))
                Result.success(proc)
            } else {
                // fallback to generic stored-proc call
                val response = apiService.callProcedure(
                    "sp_obtener_operaciones_cliente",
                    mapOf("params" to listOf(clientId, limit, offset))
                )
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Response body is null"))
                } else {
                    Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClientQuotes(clientId: String, limit: Int? = null, offset: Int? = null): Result<ProcedureResponse> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.getCotizacionesCliente(clientId, limit ?: 10, offset ?: 0)
            if (resp.isSuccessful) {
                val body = resp.body()
                val rows = body?.cotizaciones ?: emptyList()
                val proc = ProcedureResponse(args = emptyList(), results = listOf(rows))
                Result.success(proc)
            } else {
                val response = apiService.callProcedure(
                    "sp_obtener_cotizaciones_cliente",
                    mapOf("params" to listOf(clientId, limit, offset))
                )
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Response body is null"))
                } else {
                    Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClientInvoices(clientId: String, limit: Int? = null, offset: Int? = null): Result<ProcedureResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.callProcedure(
                "sp_obtener_facturas_cliente",
                mapOf("params" to listOf(clientId, limit, offset))
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClientQuoteRequests(clientId: String, limit: Int? = null, offset: Int? = null): Result<ProcedureResponse> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.getSolicitudesCliente(clientId, limit ?: 10, offset ?: 0)
            if (resp.isSuccessful) {
                val body = resp.body()
                val rows = body?.solicitudes ?: emptyList()
                val proc = ProcedureResponse(args = emptyList(), results = listOf(rows))
                Result.success(proc)
            } else {
                val response = apiService.callProcedure(
                    "sp_obtener_solicitudes_cotizacion_cliente",
                    mapOf("params" to listOf(clientId, limit, offset))
                )
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Response body is null"))
                } else {
                    Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClientInfo(clientId: String): Result<Map<String, Any>?> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.getClienteInfo(clientId)
            if (resp.isSuccessful) {
                val body = resp.body()
                Result.success(body?.cliente)
            } else {
                Result.failure(Exception("Error: ${resp.code()} - ${resp.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editClient(clientId: String, payload: Map<String, Any?>): Result<com.example.crm_logistico_movil.models.EditClientResponse> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.editarCliente(clientId, payload)
            if (resp.isSuccessful) {
                resp.body()?.let { Result.success(it) } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Error: ${resp.code()} - ${resp.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOperationDetail(operationId: String): Result<ProcedureResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.callProcedure(
                "sp_obtener_detalle_operacion",
                mapOf("params" to listOf(operationId))
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
 
