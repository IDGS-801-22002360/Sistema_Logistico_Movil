package com.example.crm_logistico_movil.repository

import com.example.crm_logistico_movil.api.ApiClient
import com.example.crm_logistico_movil.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClientRepository {
    private val apiService = ApiClient.apiService

    suspend fun createSolicitud(solicitudRequest: SolicitudRequest): Result<SolicitudResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.crearSolicitud(solicitudRequest)
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClientQuotes(clientId: String, limit: Int? = null, offset: Int? = null): Result<ProcedureResponse> = withContext(Dispatchers.IO) {
        try {
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
    
    suspend fun getClientInvoices(clientId: String, limit: Int = 10, offset: Int = 0): ApiResponse<List<FacturaExtended>> {
        return databaseManager.getClientInvoices(clientId, limit, offset)
    }
    
    suspend fun getClientQuoteRequests(clientId: String, limit: Int = 10, offset: Int = 0): ApiResponse<List<SolicitudCotizacionExtended>> {
        return databaseManager.getClientQuoteRequests(clientId, limit, offset)
    }
    
    suspend fun createQuoteRequest(clientId: String, request: NuevaSolicitudCotizacion): ApiResponse<SolicitudCotizacionExtended> {
        return databaseManager.createQuoteRequest(clientId, request)
    }
    
    suspend fun getOperationDetail(operationId: String): ApiResponse<OperationDetail> {
        return databaseManager.getOperationDetail(operationId)
    }
    
    suspend fun getInvoiceDetail(invoiceId: String): ApiResponse<FacturaDetail> {
        return databaseManager.getInvoiceDetail(invoiceId)
    }
    
    suspend fun getCountries(): ApiResponse<List<Pais>> {
        return databaseManager.getCountries()
    }
    
    suspend fun getCitiesByCountry(countryId: String): ApiResponse<List<Localizacion>> {
        return databaseManager.getCitiesByCountry(countryId)
    }
}
