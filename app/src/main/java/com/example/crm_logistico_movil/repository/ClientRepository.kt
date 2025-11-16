package com.example.crm_logistico_movil.repository

import com.example.crm_logistico_movil.api.ApiClient
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.models.FacturaCliente
import com.example.crm_logistico_movil.models.FacturaClienteTmp


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

    suspend fun getFacturasCliente(clientId: String): Result<List<FacturaClienteTmp>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFacturasCliente(clientId)
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

    suspend fun getFacturaDetail(facturaId: String): Result<FacturaClienteTmp?> = withContext(Dispatchers.IO) {
        try {
            // Por ahora usamos el endpoint genérico para obtener detalles de factura
            // Podrías crear un endpoint específico en tu backend más adelante
            val response = apiService.callProcedure(
                "sp_obtener_detalle_factura",
                mapOf("params" to listOf(facturaId))
            )
            if (response.isSuccessful) {
                response.body()?.let { procedureResponse ->
                    // Extraer el primer resultado si existe
                    val results = procedureResponse.results
                    if (results.isNotEmpty() && results[0].isNotEmpty()) {
                        val facturaData = results[0][0] // Primera fila del primer resultado
                        // Convertir el Map a FacturaCliente
                        val factura = FacturaClienteTmp(
                            id_factura_cliente = facturaData["id_factura_cliente"].toString(),
                            id_cliente = facturaData["id_cliente"].toString(),
                            id_operacion = facturaData["id_operacion"]?.toString(),
                            id_cotizacion = facturaData["id_cotizacion"]?.toString(),
                            numero_factura = facturaData["numero_factura"].toString(),
                            fecha_emision = facturaData["fecha_emision"].toString(),
                            fecha_vencimiento = facturaData["fecha_vencimiento"].toString(),
                            monto_total = (facturaData["monto_total"] as? Number)?.toDouble() ?: 0.0,
                            monto_pagado = (facturaData["monto_pagado"] as? Number)?.toDouble() ?: 0.0,
                            moneda = facturaData["moneda"].toString(),
                            estatus = facturaData["estatus"].toString(),
                            observaciones = facturaData["observaciones"]?.toString(),
                            fecha_creacion = facturaData["fecha_creacion"].toString(),
                            cotizacion_tipo_servicio = facturaData["cotizacion_tipo_servicio"]?.toString(),
                            cotizacion_tipo_carga = facturaData["cotizacion_tipo_carga"]?.toString(),
                            cotizacion_incoterm = facturaData["cotizacion_incoterm"]?.toString(),
                            descripcion_mercancia = facturaData["descripcion_mercancia"]?.toString(),
                            operacion_tipo_servicio = facturaData["operacion_tipo_servicio"]?.toString(),
                            fecha_inicio_operacion = facturaData["fecha_inicio_operacion"]?.toString(),
                            fecha_estimada_entrega = facturaData["fecha_estimada_entrega"]?.toString()
                        )
                        Result.success(factura)
                    } else {
                        Result.success(null)
                    }
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
