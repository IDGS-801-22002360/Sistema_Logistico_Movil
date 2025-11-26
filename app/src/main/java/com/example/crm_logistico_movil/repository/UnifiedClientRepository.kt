package com.example.crm_logistico_movil.repository

import android.util.Log
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.config.MigrationConfig

/**
 * Repositorio unificado para operaciones de cliente que puede usar tanto
 * el nuevo sistema REST como el DatabaseManager legacy.
 *
 * La selección del sistema se basa en MigrationConfig.USE_REST_APIS
 *
 * Migración gradual:
 * - REST APIs = true: Usa ClientRepositoryAPI (Nest.js en Render)
 * - REST APIs = false: Usa DatabaseManager (MySQL local legacy)
 */
class UnifiedClientRepository {

    private val clientRepositoryAPI = ClientRepositoryAPI()
    private val databaseManager = com.example.crm_logistico_movil.database.DatabaseManager.getInstance()
    private val referenceDataRepository = ReferenceDataRepository

    fun getMigrationStatus(): String {
        return if (MigrationConfig.USE_REST_APIS) {
            "Using REST APIs (Nest.js on Render)"
        } else {
            "Using Legacy Database (MySQL local)"
        }
    }

    /**
     * Obtener operaciones del cliente
     * Unifica ambos sistemas devolviendo siempre un ProcedureResponse para compatibilidad
     */
    suspend fun getClientOperations(clientId: String, limit: Int? = null, offset: Int? = null): Result<ProcedureResponse> {
        return if (MigrationConfig.USE_REST_APIS) {
            try {
                Log.d("UnifiedClientRepository", "Getting client operations via REST API for client: $clientId")
                val result = clientRepositoryAPI.getClientOperations(clientId, limit ?: 10, offset ?: 0)

                if (result.isSuccess) {
                    val operacionesResponse = result.getOrNull()
                    // Convertir OperacionesListResponse a ProcedureResponse para compatibilidad
                    val operacionesList = operacionesResponse?.operaciones ?: emptyList()

                    // Enriquecer operaciones con datos de referencia
                    val enrichedOperaciones = operacionesList.map { operacion ->
                        enrichOperationData(operacion)
                    }

                    val procedureResponse = ProcedureResponse(
                        args = emptyList(),
                        results = listOf(enrichedOperaciones),
                        status = "OK"
                    )
                    Result.success(procedureResponse)
                } else {
                    result.exceptionOrNull()?.let { Result.failure(it) }
                        ?: Result.failure(Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in REST API call: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                Log.d("UnifiedClientRepository", "Getting client operations via Database for client: $clientId")
                val result = databaseManager.getClientOperations(clientId, limit ?: 10, offset ?: 0)

                if (result.status == "OK") {
                    // Convertir ApiResponse a ProcedureResponse
                    val operations = result.data ?: emptyList()
                    val procedureResponse = ProcedureResponse(
                        args = emptyList(),
                        results = listOf(operations.map { op ->
                            mapOf<String, Any>(
                                "id_operacion" to (op.id_operacion ?: ""),
                                "id_cotizacion" to (op.id_cotizacion ?: ""),
                                "id_cliente" to (op.id_cliente ?: ""),
                                "id_usuario_operativo" to (op.id_usuario_operativo ?: ""),
                                "id_proveedor" to (op.id_proveedor ?: ""),
                                "id_agente" to (op.id_agente ?: ""),
                                "tipo_servicio" to (op.tipo_servicio ?: ""),
                                "tipo_carga" to (op.tipo_carga ?: ""),
                                "incoterm" to (op.incoterm ?: ""),
                                "fecha_inicio_operacion" to (op.fecha_inicio_operacion ?: ""),
                                "fecha_estimada_arribo" to (op.fecha_estimada_arribo ?: ""),
                                "fecha_estimada_entrega" to (op.fecha_estimada_entrega ?: ""),
                                "fecha_arribo_real" to (op.fecha_arribo_real ?: ""),
                                "fecha_entrega_real" to (op.fecha_entrega_real ?: ""),
                                "estatus" to (op.estatus ?: ""),
                                "numero_referencia_proveedor" to (op.numero_referencia_proveedor ?: ""),
                                "notas_operacion" to (op.notas_operacion ?: ""),
                                "fecha_creacion" to (op.fecha_creacion ?: ""),
                                "proveedorNombre" to (op.proveedorNombre ?: ""),
                                "operativoNombre" to (op.operativoNombre ?: ""),
                                "operativoApellido" to (op.operativoApellido ?: "")
                            )
                        }),
                        status = result.status
                    )
                    Result.success(procedureResponse)
                } else {
                    Result.failure(Exception(result.message))
                }
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in Database call: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener cotizaciones del cliente
     */
    suspend fun getClientQuotes(clientId: String, limit: Int? = null, offset: Int? = null): Result<ProcedureResponse> {
        return if (MigrationConfig.USE_REST_APIS) {
            try {
                Log.d("UnifiedClientRepository", "Getting client quotes via REST API for client: $clientId")
                val result = clientRepositoryAPI.getClientQuotes(clientId, limit ?: 10, offset ?: 0)

                if (result.isSuccess) {
                    val cotizacionesResponse = result.getOrNull()
                    val cotizacionesList = cotizacionesResponse?.cotizaciones ?: emptyList()

                    // Enriquecer cotizaciones con datos de referencia
                    val enrichedCotizaciones = cotizacionesList.map { cotizacion ->
                        enrichQuoteData(cotizacion)
                    }

                    val procedureResponse = ProcedureResponse(
                        args = emptyList(),
                        results = listOf(enrichedCotizaciones),
                        status = "OK"
                    )
                    Result.success(procedureResponse)
                } else {
                    result.exceptionOrNull()?.let { Result.failure(it) }
                        ?: Result.failure(Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in REST API call: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                Log.d("UnifiedClientRepository", "Getting client quotes via Database for client: $clientId")
                val result = databaseManager.getClientQuotes(clientId, limit ?: 10, offset ?: 0)

                if (result.status == "OK") {
                    val quotes = result.data ?: emptyList()
                    val procedureResponse = ProcedureResponse(
                        args = emptyList(),
                        results = listOf(quotes.map { quote ->
                            mapOf<String, Any>(
                                "id_cotizacion" to (quote.id_cotizacion ?: ""),
                                "id_cliente" to (quote.id_cliente ?: ""),
                                "id_usuario_ventas" to (quote.id_usuario_ventas ?: ""),
                                "id_usuario_operativo" to (quote.id_usuario_operativo ?: ""),
                                "tipo_servicio" to (quote.tipo_servicio ?: ""),
                                "tipo_carga" to (quote.tipo_carga ?: ""),
                                "incoterm" to (quote.incoterm ?: ""),
                                "fecha_solicitud" to (quote.fecha_solicitud ?: ""),
                                "estatus" to (quote.estatus ?: "")
                            )
                        }),
                        status = result.status
                    )
                    Result.success(procedureResponse)
                } else {
                    Result.failure(Exception(result.message))
                }
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in Database call: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener solicitudes de cotización del cliente
     */
    suspend fun getClientQuoteRequests(clientId: String, limit: Int? = null, offset: Int? = null): Result<ProcedureResponse> {
        return if (MigrationConfig.USE_REST_APIS) {
            try {
                Log.d("UnifiedClientRepository", "Getting client quote requests via REST API for client: $clientId")
                val result = clientRepositoryAPI.getClientQuoteRequests(clientId, limit ?: 10, offset ?: 0)

                if (result.isSuccess) {
                    val solicitudesResponse = result.getOrNull()
                    val solicitudesList = solicitudesResponse?.solicitudes ?: emptyList()

                    // Enriquecer solicitudes con datos de referencia
                    Log.d("UnifiedClientRepository", "Enriching ${solicitudesList.size} quote requests with reference data")
                    val enrichedSolicitudes = solicitudesList.map { solicitud ->
                        enrichQuoteRequestData(solicitud)
                    }

                    val procedureResponse = ProcedureResponse(
                        args = emptyList(),
                        results = listOf(enrichedSolicitudes),
                        status = "OK"
                    )
                    Result.success(procedureResponse)
                } else {
                    result.exceptionOrNull()?.let { Result.failure(it) }
                        ?: Result.failure(Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in REST API call: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                Log.d("UnifiedClientRepository", "Getting client quote requests via Database for client: $clientId")
                val result = databaseManager.getClientQuoteRequests(clientId, limit ?: 10, offset ?: 0)

                if (result.status == "OK") {
                    val requests = result.data ?: emptyList()
                    val procedureResponse = ProcedureResponse(
                        args = emptyList(),
                        results = listOf(requests.map { request ->
                            mapOf<String, Any>(
                                "id_solicitud" to (request.id_solicitud ?: ""),
                                "id_cliente" to (request.id_cliente ?: ""),
                                "tipo_servicio" to (request.tipo_servicio ?: ""),
                                "tipo_carga" to (request.tipo_carga ?: ""),
                                "origen_ciudad" to (request.origen_ciudad ?: ""),
                                "origen_pais" to (request.origen_pais ?: ""),
                                "destino_ciudad" to (request.destino_ciudad ?: ""),
                                "destino_pais" to (request.destino_pais ?: ""),
                                "fecha_solicitud" to (request.fecha_solicitud ?: ""),
                                "descripcion_mercancia" to (request.descripcion_mercancia ?: ""),
                                "valor_estimado_mercancia" to (request.valor_estimado_mercancia ?: 0.0),
                                "estatus" to (request.estatus ?: "")
                            )
                        }),
                        status = result.status
                    )
                    Result.success(procedureResponse)
                } else {
                    Result.failure(Exception(result.message))
                }
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in Database call: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener facturas del cliente
     */
    suspend fun getFacturasCliente(clientId: String): Result<List<FacturaClienteTmp>> {
        return if (MigrationConfig.USE_REST_APIS) {
            try {
                Log.d("UnifiedClientRepository", "Getting client invoices via REST API for client: $clientId")
                clientRepositoryAPI.getClientInvoices(clientId)
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in REST API call: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                Log.d("UnifiedClientRepository", "Getting client invoices via Database for client: $clientId")
                val result = databaseManager.getClientInvoices(clientId)

                if (result.status == "OK") {
                    val facturas = result.data ?: emptyList()
                    // Convertir FacturaExtended a FacturaClienteTmp
                    val facturasTmp = facturas.map { factura ->
                        FacturaClienteTmp(
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
                            fecha_creacion = factura.fecha_creacion
                        )
                    }
                    Result.success(facturasTmp)
                } else {
                    Result.failure(Exception(result.message))
                }
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in Database call: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener información del cliente
     */
    suspend fun getClientInfo(clientId: String): Result<Map<String, Any>?> {
        return if (MigrationConfig.USE_REST_APIS) {
            try {
                Log.d("UnifiedClientRepository", "Getting client info via REST API for client: $clientId")
                clientRepositoryAPI.getClientInfo(clientId)
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in REST API call: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                Log.d("UnifiedClientRepository", "Getting client info via Database for client: $clientId")
                // DatabaseManager no tiene este método implementado, usar fallback
                Result.success(mapOf<String, Any>())
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in Database call: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Editar información del cliente
     */
    suspend fun editClient(clientId: String, payload: Map<String, Any?>): Result<EditClientResponse> {
        return if (MigrationConfig.USE_REST_APIS) {
            try {
                Log.d("UnifiedClientRepository", "Editing client via REST API for client: $clientId")
                clientRepositoryAPI.editClient(clientId, payload)
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in REST API call: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                Log.d("UnifiedClientRepository", "Editing client via Database for client: $clientId")
                // DatabaseManager no tiene este método, devolver error temporal
                Result.failure(Exception("Edit client not implemented in legacy database"))
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in Database call: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener tracking de operación
     */
    suspend fun getTrackingPorOperacion(operationId: String): Result<List<Map<String, Any>>> {
        return if (MigrationConfig.USE_REST_APIS) {
            try {
                Log.d("UnifiedClientRepository", "Getting tracking via REST API for operation: $operationId")
                clientRepositoryAPI.getTrackingByOperation(operationId)
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in REST API call: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                Log.d("UnifiedClientRepository", "Getting tracking via Database for operation: $operationId")
                // DatabaseManager no tiene este método específico, usar fallback
                Result.success(emptyList())
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in Database call: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Crear solicitud de cotización
     */
    suspend fun createSolicitud(solicitudRequest: SolicitudRequest): Result<SolicitudResponse> {
        return if (MigrationConfig.USE_REST_APIS) {
            try {
                Log.d("UnifiedClientRepository", "Creating solicitud via REST API for client: ${solicitudRequest.id_cliente}")
                clientRepositoryAPI.createQuoteRequest(solicitudRequest.id_cliente, solicitudRequest)
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in REST API call: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                Log.d("UnifiedClientRepository", "Creating solicitud via Database for client: ${solicitudRequest.id_cliente}")

                // Convertir SolicitudRequest a NuevaSolicitudCotizacion
                val nuevaSolicitud = NuevaSolicitudCotizacion(
                    tipo_servicio = solicitudRequest.tipo_servicio,
                    tipo_carga = solicitudRequest.tipo_carga,
                    origen_ciudad = solicitudRequest.origen_ciudad,
                    origen_pais = solicitudRequest.origen_pais,
                    destino_ciudad = solicitudRequest.destino_ciudad,
                    destino_pais = solicitudRequest.destino_pais,
                    descripcion_mercancia = solicitudRequest.descripcion_mercancia ?: "",
                    valor_estimado_mercancia = solicitudRequest.valor_estimado_mercancia
                )

                val result = databaseManager.createQuoteRequest(solicitudRequest.id_cliente, nuevaSolicitud)

                if (result.status == "OK") {
                    val solicitud = result.data
                    val solicitudResponse = SolicitudResponse(
                        id_solicitud = solicitud?.id_solicitud,
                        status = result.status,
                        message = result.message,
                        created = solicitud?.let { listOf(
                            Solicitud(
                                id_solicitud = it.id_solicitud,
                                id_cliente = it.id_cliente,
                                tipo_servicio = it.tipo_servicio,
                                tipo_carga = it.tipo_carga,
                                origen_ciudad = it.origen_ciudad,
                                origen_pais = it.origen_pais,
                                destino_ciudad = it.destino_ciudad,
                                destino_pais = it.destino_pais,
                                fecha_solicitud = it.fecha_solicitud,
                                descripcion_mercancia = it.descripcion_mercancia ?: "",
                                valor_estimado_mercancia = it.valor_estimado_mercancia,
                                estatus = it.estatus
                            )
                        ) }
                    )
                    Result.success(solicitudResponse)
                } else {
                    Result.failure(Exception(result.message))
                }
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in Database call: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener detalle específico de una operación
     * Unifica ambos sistemas para obtener información detallada de una operación
     * Devuelve un ProcedureResponse para compatibilidad con la pantalla de detalle
     */
    suspend fun getOperationDetail(operationId: String): Result<ProcedureResponse?> {
        return if (MigrationConfig.USE_REST_APIS) {
            try {
                Log.d("UnifiedClientRepository", "Getting operation detail via REST API for operation: $operationId")

                // ESTRATEGIA: Usar endpoint de tracking que SÍ existe y devuelve información completa
                // /movil/tracking/{operationId} - Este endpoint ya existe y funciona
                Log.d("UnifiedClientRepository", "Using tracking endpoint to get operation detail")

                val trackingResult = clientRepositoryAPI.getTrackingByOperation(operationId)

                if (trackingResult.isSuccess) {
                    val trackingList = trackingResult.getOrNull() ?: emptyList()

                    if (trackingList.isNotEmpty()) {
                        // Extraer datos de la operación del primer registro de tracking
                        val firstTracking = trackingList.first()
                        val operacionData = firstTracking["operacion"] as? Map<String, Any>

                        val operationData = if (operacionData != null) {
                            // Crear datos básicos de la operación
                            val baseData = mutableMapOf<String, Any>(
                                "id_operacion" to (operacionData["id_operacion"] ?: operationId),
                                "tipo_servicio" to (operacionData["tipo_servicio"] ?: "No disponible"),
                                "tipo_carga" to (operacionData["tipo_carga"] ?: "No disponible"),
                                "estatus" to (operacionData["estatus"] ?: "No disponible"),
                                "fecha_inicio_operacion" to (operacionData["fecha_inicio_operacion"] ?: ""),
                                "fecha_estimada_arribo" to (operacionData["fecha_estimada_arribo"] ?: ""),
                                "fecha_estimada_entrega" to (operacionData["fecha_estimada_entrega"] ?: ""),
                                "fecha_arribo_real" to (operacionData["fecha_arribo_real"] ?: ""),
                                "fecha_entrega_real" to (operacionData["fecha_entrega_real"] ?: ""),
                                "incoterm" to (operacionData["incoterm"] ?: ""),
                                "numero_referencia_proveedor" to (operacionData["numero_referencia_proveedor"] ?: ""),
                                "notas_operacion" to (operacionData["notas_operacion"] ?: ""),
                                "operativo_nombre" to "", // No disponible en tracking
                                "operativo_apellido" to "" // No disponible en tracking
                            )

                            // Agregar IDs para enriquecimiento solo si existen
                            operacionData["id_proveedor"]?.let { baseData["id_proveedor"] = it }
                            operacionData["id_agente"]?.let { baseData["id_agente"] = it }

                            // Enriquecer con datos de referencia
                            enrichOperationData(baseData)
                        } else {
                            mapOf<String, Any>(
                                "id_operacion" to operationId,
                                "tipo_servicio" to "No disponible",
                                "tipo_carga" to "No disponible",
                                "estatus" to "No disponible",
                                "fecha_inicio_operacion" to "",
                                "fecha_estimada_arribo" to "",
                                "fecha_estimada_entrega" to "",
                                "fecha_arribo_real" to "",
                                "fecha_entrega_real" to "",
                                "incoterm" to "",
                                "numero_referencia_proveedor" to "",
                                "notas_operacion" to "Información no disponible en tracking",
                                "proveedor_nombre" to "",
                                "operativo_nombre" to "",
                                "operativo_apellido" to ""
                            )
                        }

                        // Preparar datos de tracking para el resultado
                        val trackingData = trackingList.map { tracking ->
                            mapOf<String, Any>(
                                "id_tracking" to (tracking["id_tracking"] ?: 0),
                                "fecha_hora_actualizacion" to (tracking["fecha_hora_actualizacion"] ?: ""),
                                "ubicacion_actual" to (tracking["ubicacion_actual"] ?: ""),
                                "latitud" to (tracking["latitud"] ?: 0.0),
                                "longitud" to (tracking["longitud"] ?: 0.0),
                                "estatus_seguimiento" to (tracking["estatus_seguimiento"] ?: ""),
                                "referencia_transportista" to (tracking["referencia_transportista"] ?: ""),
                                "nombre_transportista" to (tracking["nombre_transportista"] ?: ""),
                                "notas_tracking" to (tracking["notas_tracking"] ?: "")
                            )
                        }

                        val procedureResponse = ProcedureResponse(
                            args = emptyList(),
                            results = listOf(
                                listOf(operationData), // Resultado 0: datos de la operación
                                trackingData, // Resultado 1: tracking (con datos reales)
                                emptyList<Map<String, Any>>(), // Resultado 2: incidencias (vacío)
                                emptyList<Map<String, Any>>()  // Resultado 3: demoras (vacío)
                            ),
                            status = "OK"
                        )

                        Result.success(procedureResponse)
                    } else {
                        // No hay tracking, devolver datos básicos
                        val basicOperationData = mapOf<String, Any>(
                            "id_operacion" to operationId,
                            "tipo_servicio" to "No disponible",
                            "tipo_carga" to "No disponible",
                            "estatus" to "No disponible",
                            "fecha_inicio_operacion" to "",
                            "fecha_estimada_arribo" to "",
                            "fecha_estimada_entrega" to "",
                            "fecha_arribo_real" to "",
                            "fecha_entrega_real" to "",
                            "incoterm" to "",
                            "numero_referencia_proveedor" to "",
                            "notas_operacion" to "Sin información de tracking disponible",
                            "proveedor_nombre" to "",
                            "operativo_nombre" to "",
                            "operativo_apellido" to ""
                        )

                        val procedureResponse = ProcedureResponse(
                            args = emptyList(),
                            results = listOf(
                                listOf(basicOperationData),
                                emptyList<Map<String, Any>>(),
                                emptyList<Map<String, Any>>(),
                                emptyList<Map<String, Any>>()
                            ),
                            status = "OK"
                        )

                        Result.success(procedureResponse)
                    }
                } else {
                    // Error obteniendo tracking
                    Result.failure(trackingResult.exceptionOrNull() ?: Exception("Error getting tracking data"))
                }

            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in REST API call: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                Log.d("UnifiedClientRepository", "Getting operation detail via Database for operation: $operationId")
                // El DatabaseManager SÍ tiene método específico de detalle
                val result = databaseManager.getOperationDetail(operationId)

                if (result.status == "OK") {
                    // Convertir ApiResponse<OperationDetail> a ProcedureResponse para compatibilidad
                    val operationDetail = result.data
                    if (operationDetail != null) {
                        // Crear resultado compatible con ProcedureResponse esperado por OperationDetailScreen
                        val procedureResponse = ProcedureResponse(
                            args = emptyList(),
                            results = listOf(
                                // Resultado 0: datos de la operación
                                listOf(mapOf<String, Any>(
                                    "id_operacion" to operationDetail.operacion.id_operacion,
                                    "tipo_servicio" to operationDetail.operacion.tipo_servicio,
                                    "tipo_carga" to operationDetail.operacion.tipo_carga,
                                    "estatus" to operationDetail.operacion.estatus,
                                    "fecha_inicio_operacion" to operationDetail.operacion.fecha_inicio_operacion,
                                    "fecha_estimada_arribo" to (operationDetail.operacion.fecha_estimada_arribo ?: ""),
                                    "fecha_estimada_entrega" to (operationDetail.operacion.fecha_estimada_entrega ?: ""),
                                    "fecha_arribo_real" to (operationDetail.operacion.fecha_arribo_real ?: ""),
                                    "fecha_entrega_real" to (operationDetail.operacion.fecha_entrega_real ?: ""),
                                    "incoterm" to (operationDetail.operacion.incoterm ?: ""),
                                    "numero_referencia_proveedor" to (operationDetail.operacion.numero_referencia_proveedor ?: ""),
                                    "notas_operacion" to (operationDetail.operacion.notas_operacion ?: ""),
                                    "proveedor_nombre" to (operationDetail.operacion.proveedorNombre ?: ""),
                                    "operativo_nombre" to (operationDetail.operacion.operativoNombre ?: ""),
                                    "operativo_apellido" to (operationDetail.operacion.operativoApellido ?: "")
                                )),
                                // Resultado 1: tracking (lista)
                                operationDetail.tracking.map { tracking ->
                                    mapOf<String, Any>(
                                        "id_tracking" to tracking.id_tracking,
                                        "fecha_hora_actualizacion" to tracking.fecha_hora_actualizacion,
                                        "ubicacion_actual" to (tracking.ubicacion_actual ?: ""),
                                        "latitud" to (tracking.latitud ?: 0.0),
                                        "longitud" to (tracking.longitud ?: 0.0),
                                        "estatus_seguimiento" to tracking.estatus_seguimiento,
                                        "referencia_transportista" to (tracking.referencia_transportista ?: ""),
                                        "nombre_transportista" to (tracking.nombre_transportista ?: ""),
                                        "notas_tracking" to (tracking.notas_tracking ?: "")
                                    )
                                },
                                // Resultado 2: incidencias (lista)
                                operationDetail.incidencias.map { incidencia ->
                                    mapOf<String, Any>(
                                        "id_incidencia" to incidencia.id_incidencia,
                                        "fecha_hora_incidencia" to incidencia.fecha_hora_incidencia,
                                        "descripcion_incidencia" to incidencia.descripcion_incidencia,
                                        "tipo_incidencia" to incidencia.tipo_incidencia,
                                        "estatus" to incidencia.estatus,
                                        "fecha_resolucion" to (incidencia.fecha_resolucion ?: ""),
                                        "comentarios_resolucion" to (incidencia.comentarios_resolucion ?: "")
                                    )
                                },
                                // Resultado 3: demoras (lista)
                                operationDetail.demoras.map { demora ->
                                    mapOf<String, Any>(
                                        "id_demora" to demora.id_demora,
                                        "fecha_hora_demora" to demora.fecha_hora_demora,
                                        "descripcion_demora" to (demora.descripcion_demora ?: ""),
                                        "tipo_demora" to demora.tipo_demora,
                                        "costo_asociado" to demora.costo_asociado,
                                        "moneda" to (demora.moneda ?: "")
                                    )
                                }
                            ),
                            status = "OK"
                        )
                        Result.success(procedureResponse)
                    } else {
                        Result.failure(Exception("Operation detail not found"))
                    }
                } else {
                    Result.failure(Exception(result.message))
                }
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in Database call: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Obtener detalle específico de una factura
     * Unifica ambos sistemas para obtener información detallada de una factura
     */
    suspend fun getFacturaDetail(invoiceId: String): Result<FacturaClienteTmp?> {
        return if (MigrationConfig.USE_REST_APIS) {
            try {
                Log.d("UnifiedClientRepository", "Getting invoice detail via REST API for invoice: $invoiceId")

                // ESTRATEGIA: No existe endpoint específico de detalle, pero tenemos toda la información
                // en la lista de facturas que ya obtuvimos anteriormente.
                // Vamos a buscar en todas las facturas del sistema para encontrar la específica
                Log.d("UnifiedClientRepository", "Searching for invoice in client invoice lists")

                // Necesitamos encontrar el cliente que tiene esta factura
                // Como no tenemos endpoint de detalle, buscaremos en el SessionManager para obtener el clientId actual
                val clientId = com.example.crm_logistico_movil.repository.SessionManager.currentUser?.id_cliente

                if (clientId != null && clientId.isNotEmpty()) {
                    // Obtener todas las facturas del cliente
                    val facturasResult = getFacturasCliente(clientId)

                    if (facturasResult.isSuccess) {
                        val facturas = facturasResult.getOrNull() ?: emptyList()

                        // Buscar la factura específica por ID
                        val facturaEncontrada = facturas.find { it.id_factura_cliente == invoiceId }

                        if (facturaEncontrada != null) {
                            Result.success(facturaEncontrada)
                        } else {
                            Log.w("UnifiedClientRepository", "Invoice $invoiceId not found in client $clientId invoices")
                            Result.failure(Exception("Factura no encontrada"))
                        }
                    } else {
                        Log.e("UnifiedClientRepository", "Error getting client invoices: ${facturasResult.exceptionOrNull()?.message}")
                        Result.failure(facturasResult.exceptionOrNull() ?: Exception("Error obteniendo facturas del cliente"))
                    }
                } else {
                    Log.e("UnifiedClientRepository", "No client ID available in session")
                    // Crear datos básicos si no tenemos ID del cliente
                    val basicInvoiceData = FacturaClienteTmp(
                        id_factura_cliente = invoiceId,
                        id_cliente = "unknown",
                        id_operacion = null,
                        id_cotizacion = null,
                        numero_factura = "Información no disponible",
                        fecha_emision = "",
                        fecha_vencimiento = "",
                        monto_total = 0.0,
                        monto_pagado = 0.0,
                        moneda = "USD",
                        estatus = "No disponible",
                        observaciones = "Detalle no disponible: No se puede identificar el cliente en sesión",
                        fecha_creacion = ""
                    )
                    Result.success(basicInvoiceData)
                }

            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in REST API call: ${e.message}")
                Result.failure(e)
            }
        } else {
            try {
                Log.d("UnifiedClientRepository", "Getting invoice detail via Database for invoice: $invoiceId")
                // El DatabaseManager SÍ tiene método específico de detalle
                val result = databaseManager.getInvoiceDetail(invoiceId)

                if (result.status == "OK") {
                    val facturaDetail = result.data
                    if (facturaDetail != null) {
                        // Convertir FacturaDetail a FacturaClienteTmp
                        val factura = FacturaClienteTmp(
                            id_factura_cliente = facturaDetail.factura.id_factura_cliente ?: "",
                            id_cliente = facturaDetail.factura.id_cliente ?: "",
                            id_operacion = facturaDetail.factura.id_operacion,
                            id_cotizacion = facturaDetail.factura.id_cotizacion,
                            numero_factura = facturaDetail.factura.numero_factura ?: "",
                            fecha_emision = facturaDetail.factura.fecha_emision ?: "",
                            fecha_vencimiento = facturaDetail.factura.fecha_vencimiento ?: "",
                            monto_total = facturaDetail.factura.monto_total ?: 0.0,
                            monto_pagado = facturaDetail.factura.monto_pagado ?: 0.0,
                            moneda = facturaDetail.factura.moneda ?: "",
                            estatus = facturaDetail.factura.estatus ?: "",
                            observaciones = facturaDetail.factura.observaciones,
                            fecha_creacion = facturaDetail.factura.fecha_creacion ?: ""
                        )
                        Result.success(factura)
                    } else {
                        Result.failure(Exception("Invoice detail not found"))
                    }
                } else {
                    Result.failure(Exception(result.message))
                }
            } catch (e: Exception) {
                Log.e("UnifiedClientRepository", "Error in Database call: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * Enriquece los datos de operación con información legible de proveedores, países y localizaciones
     */
    private suspend fun enrichOperationData(operationData: Map<String, Any>): Map<String, Any> {
        val enrichedData = operationData.toMutableMap()

        try {
            // Enriquecer información del proveedor
            val proveedorId = operationData["id_proveedor"]
            if (proveedorId != null) {
                val proveedorIdString = when (proveedorId) {
                    is Number -> proveedorId.toString()
                    is String -> proveedorId
                    else -> null
                }
                if (proveedorIdString != null) {
                    val proveedorNombre = referenceDataRepository.getProveedorName(proveedorIdString)
                    enrichedData["proveedor_nombre"] = proveedorNombre
                }
            }

            // Enriquecer información de origen (si existe)
            val origenCiudadId = operationData["id_origen_ciudad"]
            val origenPaisId = operationData["id_origen_pais"]
            if (origenCiudadId != null && origenPaisId != null) {
                val ciudadIdString = when (origenCiudadId) {
                    is Number -> origenCiudadId.toString()
                    is String -> origenCiudadId
                    else -> null
                }
                val paisIdString = when (origenPaisId) {
                    is Number -> origenPaisId.toString()
                    is String -> origenPaisId
                    else -> null
                }
                if (ciudadIdString != null && paisIdString != null) {
                    val origenCompleto = referenceDataRepository.getOrigenCompleto(ciudadIdString, paisIdString)
                    enrichedData["origen_completo"] = origenCompleto
                    enrichedData["origen_ciudad"] = referenceDataRepository.getCiudadName(ciudadIdString)
                    enrichedData["origen_pais"] = referenceDataRepository.getPaisName(paisIdString)
                }
            }

            // Enriquecer información de destino (si existe)
            val destinoCiudadId = operationData["id_destino_ciudad"]
            val destinoPaisId = operationData["id_destino_pais"]
            if (destinoCiudadId != null && destinoPaisId != null) {
                val ciudadIdString = when (destinoCiudadId) {
                    is Number -> destinoCiudadId.toString()
                    is String -> destinoCiudadId
                    else -> null
                }
                val paisIdString = when (destinoPaisId) {
                    is Number -> destinoPaisId.toString()
                    is String -> destinoPaisId
                    else -> null
                }
                if (ciudadIdString != null && paisIdString != null) {
                    val destinoCompleto = referenceDataRepository.getDestinoCompleto(ciudadIdString, paisIdString)
                    enrichedData["destino_completo"] = destinoCompleto
                    enrichedData["destino_ciudad"] = referenceDataRepository.getCiudadName(ciudadIdString)
                    enrichedData["destino_pais"] = referenceDataRepository.getPaisName(paisIdString)
                }
            }

            // Enriquecer información del agente (si existe)
            val agenteId = operationData["id_agente"]
            if (agenteId != null) {
                val agenteIdString = when (agenteId) {
                    is Number -> agenteId.toString()
                    is String -> agenteId
                    else -> null
                }
                if (agenteIdString != null) {
                    val agenteNombre = referenceDataRepository.getAgenteName(agenteIdString)
                    enrichedData["agente_nombre"] = agenteNombre
                }
            }

        } catch (e: Exception) {
            Log.e("UnifiedClientRepository", "Error enriching operation data: ${e.message}")
        }

        return enrichedData
    }

    /**
     * Enriquece los datos de solicitud de cotización con información legible de localizaciones
     */
    suspend fun enrichQuoteRequestData(solicitud: Map<String, Any>): Map<String, Any> {
        val enrichedData = solicitud.toMutableMap()
        val solicitudId = solicitud["id_solicitud"] ?: "unknown"
        Log.d("UnifiedClientRepository", "Enriching quote request $solicitudId")

        try {
            // Enriquecer información de origen
            val origenLocalizacionId = solicitud["id_origen_localizacion"]
            Log.d("UnifiedClientRepository", "Processing origen_localizacion_id: $origenLocalizacionId")
            if (origenLocalizacionId != null) {
                val origenIdString = when (origenLocalizacionId) {
                    is Number -> origenLocalizacionId.toString()
                    is String -> origenLocalizacionId
                    else -> null
                }
                Log.d("UnifiedClientRepository", "Converted origen ID to string: $origenIdString")
                if (origenIdString != null) {
                    val origenNombre = referenceDataRepository.getCiudadName(origenIdString)
                    Log.d("UnifiedClientRepository", "Got origen name: $origenNombre")
                    enrichedData["origen_ciudad"] = origenNombre

                    // También obtener información del país a través de la localización
                    val localizacion = referenceDataRepository.getLocalizacionById(origenIdString)
                    Log.d("UnifiedClientRepository", "Got localizacion: $localizacion")
                    if (localizacion?.id_pais != null) {
                        val paisNombre = referenceDataRepository.getPaisName(localizacion.id_pais)
                        Log.d("UnifiedClientRepository", "Got pais name: $paisNombre")
                        enrichedData["origen_pais"] = paisNombre
                    }
                }
            }

            // Enriquecer información de destino
            val destinoLocalizacionId = solicitud["id_destino_localizacion"]
            Log.d("UnifiedClientRepository", "Processing destino_localizacion_id: $destinoLocalizacionId")
            if (destinoLocalizacionId != null) {
                val destinoIdString = when (destinoLocalizacionId) {
                    is Number -> destinoLocalizacionId.toString()
                    is String -> destinoLocalizacionId
                    else -> null
                }
                Log.d("UnifiedClientRepository", "Converted destino ID to string: $destinoIdString")
                if (destinoIdString != null) {
                    val destinoNombre = referenceDataRepository.getCiudadName(destinoIdString)
                    Log.d("UnifiedClientRepository", "Got destino name: $destinoNombre")
                    enrichedData["destino_ciudad"] = destinoNombre

                    // También obtener información del país a través de la localización
                    val localizacion = referenceDataRepository.getLocalizacionById(destinoIdString)
                    Log.d("UnifiedClientRepository", "Got destino localizacion: $localizacion")
                    if (localizacion?.id_pais != null) {
                        val paisNombre = referenceDataRepository.getPaisName(localizacion.id_pais)
                        Log.d("UnifiedClientRepository", "Got destino pais name: $paisNombre")
                        enrichedData["destino_pais"] = paisNombre
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("UnifiedClientRepository", "Error enriching quote request data: ${e.message}")
        }

        Log.d("UnifiedClientRepository", "Final enriched data for solicitud $solicitudId: $enrichedData")
        return enrichedData
    }

    /**
     * Enriquece los datos de cotización con información legible de proveedores y localizaciones
     */
    private suspend fun enrichQuoteData(cotizacion: Map<String, Any>): Map<String, Any> {
        val enrichedData = cotizacion.toMutableMap()

        try {
            // Enriquecer información del proveedor si existe
            val proveedorId = cotizacion["id_proveedor"]
            if (proveedorId != null) {
                val proveedorIdString = when (proveedorId) {
                    is Number -> proveedorId.toString()
                    is String -> proveedorId
                    else -> null
                }
                if (proveedorIdString != null) {
                    val proveedorNombre = referenceDataRepository.getProveedorName(proveedorIdString)
                    enrichedData["proveedor_nombre"] = proveedorNombre
                }
            }

            // Enriquecer información de origen si existe localizacion
            val origenLocalizacionId = cotizacion["id_origen_localizacion"]
            if (origenLocalizacionId != null) {
                val origenIdString = when (origenLocalizacionId) {
                    is Number -> origenLocalizacionId.toString()
                    is String -> origenLocalizacionId
                    else -> null
                }
                if (origenIdString != null) {
                    val origenNombre = referenceDataRepository.getCiudadName(origenIdString)
                    enrichedData["origen_ciudad"] = origenNombre

                    // También obtener información del país a través de la localización
                    val localizacion = referenceDataRepository.getLocalizacionById(origenIdString)
                    if (localizacion?.id_pais != null) {
                        val paisNombre = referenceDataRepository.getPaisName(localizacion.id_pais)
                        enrichedData["origen_pais"] = paisNombre
                    }
                }
            }

            // Enriquecer información de destino si existe localizacion
            val destinoLocalizacionId = cotizacion["id_destino_localizacion"]
            if (destinoLocalizacionId != null) {
                val destinoIdString = when (destinoLocalizacionId) {
                    is Number -> destinoLocalizacionId.toString()
                    is String -> destinoLocalizacionId
                    else -> null
                }
                if (destinoIdString != null) {
                    val destinoNombre = referenceDataRepository.getCiudadName(destinoIdString)
                    enrichedData["destino_ciudad"] = destinoNombre

                    // También obtener información del país a través de la localización
                    val localizacion = referenceDataRepository.getLocalizacionById(destinoIdString)
                    if (localizacion?.id_pais != null) {
                        val paisNombre = referenceDataRepository.getPaisName(localizacion.id_pais)
                        enrichedData["destino_pais"] = paisNombre
                    }
                }
            }

            // Enriquecer información del agente si existe
            val agenteId = cotizacion["id_agente"]
            if (agenteId != null) {
                val agenteIdString = when (agenteId) {
                    is Number -> agenteId.toString()
                    is String -> agenteId
                    else -> null
                }
                if (agenteIdString != null) {
                    val agenteNombre = referenceDataRepository.getAgenteName(agenteIdString)
                    enrichedData["agente_nombre"] = agenteNombre
                }
            }

        } catch (e: Exception) {
            Log.e("UnifiedClientRepository", "Error enriching quote data: ${e.message}")
        }

        return enrichedData
    }
}