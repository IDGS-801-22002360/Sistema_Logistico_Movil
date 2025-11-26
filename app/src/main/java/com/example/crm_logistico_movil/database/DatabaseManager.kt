package com.example.crm_logistico_movil.database

import android.util.Log
import java.sql.*
import java.util.Properties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import com.example.crm_logistico_movil.config.DatabaseConfig
import com.example.crm_logistico_movil.models.*

class DatabaseManager private constructor() {
    
    private var connection: Connection? = null
    
    companion object {
        @Volatile
        private var INSTANCE: DatabaseManager? = null
        
        fun getInstance(): DatabaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseManager().also { INSTANCE = it }
            }
        }
    }
    
    private suspend fun getConnection(): Connection? = withContext(Dispatchers.IO) {
        try {
            if (connection?.isClosed != false) {
                // Class.forName may throw an Error (NoClassDefFoundError) if the JDBC driver
                // is not present on the device. Catch Throwable to avoid crashing the app and
                // let callers handle the null connection gracefully.
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver")
                } catch (t: Throwable) {
                    Log.e("DatabaseManager", "JDBC Driver not available: ${t.message}")
                    return@withContext null
                }

                val props = Properties()
                props.setProperty("user", DatabaseConfig.DB_USER)
                props.setProperty("password", DatabaseConfig.DB_PASSWORD)
                props.setProperty("useSSL", "false")
                props.setProperty("allowPublicKeyRetrieval", "true")
                props.setProperty("serverTimezone", "UTC")
                props.setProperty("connectTimeout", DatabaseConfig.CONNECTION_TIMEOUT.toString())
                props.setProperty("socketTimeout", DatabaseConfig.SOCKET_TIMEOUT.toString())

                try {
                    connection = DriverManager.getConnection(DatabaseConfig.DB_URL, props)
                    Log.d("DatabaseManager", "Conexión establecida con la base de datos")
                } catch (t: Throwable) {
                    Log.e("DatabaseManager", "Error al obtener conexión JDBC: ${t.message}")
                    return@withContext null
                }
            }
            connection
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al conectar con la base de datos: ${e.message}")
            null
        }
    }
    
    suspend fun loginUser(email: String, password: String): ApiResponse<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            // Usar SHA-256 para mantener compatibilidad con los stored procedures
            val hashedPassword = java.security.MessageDigest.getInstance("SHA-256")
                .digest(password.toByteArray())
                .joinToString("") { "%02x".format(it) }
            
            val statement = conn.prepareCall("{CALL sp_login_user(?, ?, ?, ?)}")
            statement.setString(1, email)
            statement.setString(2, hashedPassword)
            statement.registerOutParameter(3, Types.VARCHAR) // o_status
            statement.registerOutParameter(4, Types.VARCHAR) // o_message
            
            statement.execute()
            
            val status = statement.getString(3)
            val message = statement.getString(4)
            
            if (status == "OK") {
                val resultSet = statement.resultSet
                if (resultSet?.next() == true) {
                    val user = User(
                        id_usuario = resultSet.getString("id_usuario"),
                        nombre = resultSet.getString("nombre"),
                        apellido = resultSet.getString("apellido"),
                        email = resultSet.getString("email"),
                        rol = resultSet.getString("rol"),
                        activo = resultSet.getBoolean("activo"),
                        fecha_creacion = try {
                            val s = resultSet.getString("fecha_creacion")
                            if (s != null) Instant.parse(s) else Clock.System.now()
                        } catch (e: Exception) {
                            Clock.System.now()
                        }
                    )
                    
                    ApiResponse("OK", message, LoginResponse(1, message, listOf(user)))
                } else {
                    ApiResponse("ERROR", "Error al obtener datos del usuario")
                }
            } else {
                ApiResponse(status, message)
            }
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error en login: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun registerClient(request: RegisterRequest): ApiResponse<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            // Usar SHA-256 para mantener compatibilidad con los stored procedures
            val hashedPassword = java.security.MessageDigest.getInstance("SHA-256")
                .digest(request.password.toByteArray())
                .joinToString("") { "%02x".format(it) }
            
            val statement = conn.prepareCall("{CALL sp_registrar_cliente(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")
            statement.setString(1, request.nombre)
            statement.setString(2, request.apellido)
            statement.setString(3, request.email)
            statement.setString(4, hashedPassword)
            statement.setString(5, request.nombreEmpresa)
            statement.setString(6, request.rfc)
            statement.setString(7, request.direccion)
            statement.setString(8, request.ciudad)
            statement.setString(9, request.pais)
            statement.setString(10, request.telefono)
            statement.registerOutParameter(11, Types.VARCHAR) // o_id_usuario
            statement.registerOutParameter(12, Types.VARCHAR) // o_status
            statement.registerOutParameter(13, Types.VARCHAR) // o_message
            
            statement.execute()
            
            val idUsuario = statement.getString(11)
            val status = statement.getString(12)
            val message = statement.getString(13)
            
            if (status == "OK") {
                val resultSet = statement.resultSet
                if (resultSet?.next() == true) {
                    val user = User(
                        id_usuario = resultSet.getString("id_usuario"),
                        nombre = resultSet.getString("nombre"),
                        apellido = resultSet.getString("apellido"),
                        email = resultSet.getString("email"),
                        rol = resultSet.getString("rol"),
                        activo = resultSet.getBoolean("activo"),
                        fecha_creacion = try {
                            val s = resultSet.getString("fecha_creacion")
                            if (s != null) Instant.parse(s) else Clock.System.now()
                        } catch (e: Exception) {
                            Clock.System.now()
                        }
                    )
                    
                    ApiResponse("OK", message, LoginResponse(1, message, listOf(user)))
                } else {
                    ApiResponse("ERROR", "Error al obtener datos del usuario registrado")
                }
            } else {
                ApiResponse(status, message)
            }
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error en registro: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun requestPasswordReset(email: String): ApiResponse<String> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_request_password_reset(?, ?, ?, ?)}")
            statement.setString(1, email)
            statement.registerOutParameter(2, Types.VARCHAR) // o_status
            statement.registerOutParameter(3, Types.VARCHAR) // o_message
            statement.registerOutParameter(4, Types.VARCHAR) // o_token
            
            statement.execute()
            
            val status = statement.getString(2)
            val message = statement.getString(3)
            val token = statement.getString(4)
            
            if (status == "OK") {
                ApiResponse("OK", message, token)
            } else {
                ApiResponse(status, message)
            }
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error en solicitud de reset: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun resetPassword(token: String, newPassword: String): ApiResponse<Unit> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            // Usar SHA-256 para mantener compatibilidad con los stored procedures
            val hashedPassword = java.security.MessageDigest.getInstance("SHA-256")
                .digest(newPassword.toByteArray())
                .joinToString("") { "%02x".format(it) }
            
            val statement = conn.prepareCall("{CALL sp_reset_password(?, ?, ?, ?)}")
            statement.setString(1, token)
            statement.setString(2, hashedPassword)
            statement.registerOutParameter(3, Types.VARCHAR) // o_status
            statement.registerOutParameter(4, Types.VARCHAR) // o_message
            
            statement.execute()
            
            val status = statement.getString(3)
            val message = statement.getString(4)
            
            ApiResponse(status, message)
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error en reset de contraseña: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun checkEmailExists(email: String): ApiResponse<Boolean> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_verificar_email(?, ?, ?)}")
            statement.setString(1, email)
            statement.registerOutParameter(2, Types.VARCHAR) // o_status
            statement.registerOutParameter(3, Types.VARCHAR) // o_message
            
            statement.execute()
            
            val status = statement.getString(2)
            val message = statement.getString(3)
            
            ApiResponse("OK", message, status == "EXISTS")
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al verificar email: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun checkRfcExists(rfc: String): ApiResponse<Boolean> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_verificar_rfc(?, ?, ?)}")
            statement.setString(1, rfc)
            statement.registerOutParameter(2, Types.VARCHAR) // o_status
            statement.registerOutParameter(3, Types.VARCHAR) // o_message
            
            statement.execute()
            
            val status = statement.getString(2)
            val message = statement.getString(3)
            
            ApiResponse("OK", message, status == "EXISTS")
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al verificar RFC: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    // =====================================================
    // MÉTODOS PARA FUNCIONALIDAD DEL CLIENTE
    // =====================================================
    
    suspend fun getClientSummary(clientId: String): ApiResponse<ClientSummary> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_obtener_resumen_cliente(?)}")
            statement.setString(1, clientId)
            
            val resultSet = statement.executeQuery()
            
            if (resultSet.next()) {
                val summary = ClientSummary(
                    operacionesActivas = resultSet.getInt("operaciones_activas"),
                    facturasPendientes = resultSet.getInt("facturas_pendientes"),
                    cotizacionesPendientes = resultSet.getInt("cotizaciones_pendientes"),
                    solicitudesPendientes = resultSet.getInt("solicitudes_pendientes")
                )
                ApiResponse("OK", "Resumen obtenido exitosamente", summary)
            } else {
                ApiResponse("ERROR", "No se pudo obtener el resumen del cliente")
            }
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al obtener resumen del cliente: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun getClientOperations(clientId: String, limit: Int = 10, offset: Int = 0): ApiResponse<List<OperationExtended>> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_obtener_operaciones_cliente(?, ?, ?)}")
            statement.setString(1, clientId)
            statement.setInt(2, limit)
            statement.setInt(3, offset)
            
            val resultSet = statement.executeQuery()
            val operations = mutableListOf<OperationExtended>()
            
            while (resultSet.next()) {
                val operation = OperationExtended(
                    id_operacion = resultSet.getString("id_operacion"),
                    id_cotizacion = resultSet.getString("id_cotizacion"),
                    id_cliente = resultSet.getString("id_cliente"),
                    id_usuario_operativo = resultSet.getString("id_usuario_operativo"),
                    id_proveedor = resultSet.getString("id_proveedor"),
                    id_agente = resultSet.getString("id_agente"),
                    tipo_servicio = resultSet.getString("tipo_servicio"),
                    tipo_carga = resultSet.getString("tipo_carga"),
                    incoterm = resultSet.getString("incoterm"),
                    fecha_inicio_operacion = resultSet.getString("fecha_inicio_operacion"),
                    fecha_estimada_arribo = resultSet.getString("fecha_estimada_arribo"),
                    fecha_estimada_entrega = resultSet.getString("fecha_estimada_entrega"),
                    fecha_arribo_real = resultSet.getString("fecha_arribo_real"),
                    fecha_entrega_real = resultSet.getString("fecha_entrega_real"),
                    estatus = resultSet.getString("estatus"),
                    numero_referencia_proveedor = resultSet.getString("numero_referencia_proveedor"),
                    notas_operacion = resultSet.getString("notas_operacion"),
                    fecha_creacion = resultSet.getString("fecha_creacion"),
                    proveedorNombre = resultSet.getString("proveedor_nombre"),
                    operativoNombre = resultSet.getString("operativo_nombre"),
                    operativoApellido = resultSet.getString("operativo_apellido")
                )
                operations.add(operation)
            }
            
            ApiResponse("OK", "Operaciones obtenidas exitosamente", operations)
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al obtener operaciones del cliente: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun getClientQuotes(clientId: String, limit: Int = 10, offset: Int = 0): ApiResponse<List<CotizacionExtended>> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_obtener_cotizaciones_cliente(?, ?, ?)}")
            statement.setString(1, clientId)
            statement.setInt(2, limit)
            statement.setInt(3, offset)
            
            val resultSet = statement.executeQuery()
            val quotes = mutableListOf<CotizacionExtended>()
            
            while (resultSet.next()) {
                val quote = CotizacionExtended(
                    id_cotizacion = resultSet.getString("id_cotizacion"),
                    id_cliente = resultSet.getString("id_cliente"),
                    id_usuario_ventas = resultSet.getString("id_usuario_ventas"),
                    id_usuario_operativo = resultSet.getString("id_usuario_operativo"),
                    id_origen_localizacion = resultSet.getString("id_origen_localizacion"),
                    id_destino_localizacion = resultSet.getString("id_destino_localizacion"),
                    id_proveedor = resultSet.getString("id_proveedor"),
                    id_agente = resultSet.getString("id_agente"),
                    tipo_servicio = resultSet.getString("tipo_servicio"),
                    tipo_carga = resultSet.getString("tipo_carga"),
                    incoterm = resultSet.getString("incoterm"),
                    fecha_solicitud = resultSet.getString("fecha_solicitud"),
                    fecha_estimada_arribo = resultSet.getString("fecha_estimada_arribo"),
                    fecha_estimada_entrega = resultSet.getString("fecha_estimada_entrega"),
                    descripcion_mercancia = resultSet.getString("descripcion_mercancia"),
                    estatus = resultSet.getString("estatus"),
                    motivo_rechazo = resultSet.getString("motivo_rechazo"),
                    fecha_aprobacion_rechazo = resultSet.getString("fecha_aprobacion_rechazo"),
                    fecha_creacion = resultSet.getString("fecha_creacion"),
                    id_solicitud_cliente = resultSet.getString("id_solicitud_cliente"),
                    proveedorNombre = resultSet.getString("proveedor_nombre"),
                    vendedorNombre = resultSet.getString("vendedor_nombre"),
                    vendedorApellido = resultSet.getString("vendedor_apellido"),
                    origenCiudad = resultSet.getString("origen_ciudad"),
                    destinoCiudad = resultSet.getString("destino_ciudad"),
                    origenPais = resultSet.getString("origen_pais"),
                    destinoPais = resultSet.getString("destino_pais")
                )
                quotes.add(quote)
            }
            
            ApiResponse("OK", "Cotizaciones obtenidas exitosamente", quotes)
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al obtener cotizaciones del cliente: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun getClientInvoices(clientId: String, limit: Int = 10, offset: Int = 0): ApiResponse<List<FacturaExtended>> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_obtener_facturas_cliente(?, ?, ?)}")
            statement.setString(1, clientId)
            statement.setInt(2, limit)
            statement.setInt(3, offset)
            
            val resultSet = statement.executeQuery()
            val invoices = mutableListOf<FacturaExtended>()
            
            while (resultSet.next()) {
                val invoice = FacturaExtended(
                    id_factura_cliente = resultSet.getString("id_factura_cliente"),
                    id_cliente = resultSet.getString("id_cliente"),
                    id_operacion = resultSet.getString("id_operacion"),
                    id_cotizacion = resultSet.getString("id_cotizacion"),
                    numero_factura = resultSet.getString("numero_factura"),
                    fecha_emision = resultSet.getString("fecha_emision"),
                    fecha_vencimiento = resultSet.getString("fecha_vencimiento"),
                    monto_total = resultSet.getDouble("monto_total"),
                    monto_pagado = resultSet.getDouble("monto_pagado"),
                    moneda = resultSet.getString("moneda"),
                    estatus = resultSet.getString("estatus"),
                    observaciones = resultSet.getString("observaciones"),
                    fecha_pago = resultSet.getString("fecha_pago"),
                    fecha_creacion = resultSet.getString("fecha_creacion"),
                    operacionReferencia = resultSet.getString("operacion_referencia"),
                    cotizacionReferencia = resultSet.getString("cotizacion_referencia")
                )
                invoices.add(invoice)
            }
            
            ApiResponse("OK", "Facturas obtenidas exitosamente", invoices)
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al obtener facturas del cliente: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun getClientQuoteRequests(clientId: String, limit: Int = 10, offset: Int = 0): ApiResponse<List<SolicitudCotizacionExtended>> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_obtener_solicitudes_cotizacion_cliente(?, ?, ?)}")
            statement.setString(1, clientId)
            statement.setInt(2, limit)
            statement.setInt(3, offset)
            
            val resultSet = statement.executeQuery()
            val requests = mutableListOf<SolicitudCotizacionExtended>()
            
            while (resultSet.next()) {
                val request = SolicitudCotizacionExtended(
                    id_solicitud = resultSet.getString("id_solicitud"),
                    id_cliente = resultSet.getString("id_cliente"),
                    tipo_servicio = resultSet.getString("tipo_servicio"),
                    tipo_carga = resultSet.getString("tipo_carga"),
                    origen_ciudad = resultSet.getString("origen_ciudad"),
                    origen_pais = resultSet.getString("origen_pais"),
                    destino_ciudad = resultSet.getString("destino_ciudad"),
                    destino_pais = resultSet.getString("destino_pais"),
                    fecha_solicitud = resultSet.getString("fecha_solicitud"),
                    descripcion_mercancia = resultSet.getString("descripcion_mercancia"),
                    valor_estimado_mercancia = resultSet.getDouble("valor_estimado_mercancia"),
                    estatus = resultSet.getString("estatus")
                )
                requests.add(request)
            }
            
            ApiResponse("OK", "Solicitudes obtenidas exitosamente", requests)
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al obtener solicitudes del cliente: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun createQuoteRequest(clientId: String, request: NuevaSolicitudCotizacion): ApiResponse<SolicitudCotizacionExtended> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_crear_solicitud_cotizacion(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}")
            statement.setString(1, clientId)
            statement.setString(2, request.tipo_servicio)
            statement.setString(3, request.tipo_carga)
            statement.setString(4, request.origen_ciudad)
            statement.setString(5, request.origen_pais)
            statement.setString(6, request.destino_ciudad)
            statement.setString(7, request.destino_pais)
            statement.setString(8, request.descripcion_mercancia)
            request.valor_estimado_mercancia?.let { 
                statement.setDouble(9, it) 
            } ?: statement.setNull(9, Types.DECIMAL)
            statement.registerOutParameter(10, Types.VARCHAR) // o_id_solicitud
            statement.registerOutParameter(11, Types.VARCHAR) // o_status
            statement.registerOutParameter(12, Types.VARCHAR) // o_message
            
            statement.execute()
            
            val idSolicitud = statement.getString(10)
            val status = statement.getString(11)
            val message = statement.getString(12)
            
            if (status == "OK") {
                val resultSet = statement.resultSet
                if (resultSet?.next() == true) {
                    val newRequest = SolicitudCotizacionExtended(
                        id_solicitud = resultSet.getString("id_solicitud"),
                        id_cliente = resultSet.getString("id_cliente"),
                        tipo_servicio = resultSet.getString("tipo_servicio"),
                        tipo_carga = resultSet.getString("tipo_carga"),
                        origen_ciudad = resultSet.getString("origen_ciudad"),
                        origen_pais = resultSet.getString("origen_pais"),
                        destino_ciudad = resultSet.getString("destino_ciudad"),
                        destino_pais = resultSet.getString("destino_pais"),
                        fecha_solicitud = resultSet.getString("fecha_solicitud"),
                        descripcion_mercancia = resultSet.getString("descripcion_mercancia"),
                        valor_estimado_mercancia = resultSet.getDouble("valor_estimado_mercancia"),
                        estatus = resultSet.getString("estatus")
                    )
                    ApiResponse("OK", message, newRequest)
                } else {
                    ApiResponse("ERROR", "Error al obtener datos de la solicitud creada")
                }
            } else {
                ApiResponse(status, message)
            }
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al crear solicitud de cotización: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun getOperationDetail(operationId: String): ApiResponse<OperationDetail> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_obtener_detalle_operacion(?)}")
            statement.setString(1, operationId)
            
            statement.execute()
            
            // Obtener datos básicos de la operación (primer result set)
            val resultSet1 = statement.resultSet
            val operation = if (resultSet1?.next() == true) {
                OperationExtended(
                    id_operacion = resultSet1.getString("id_operacion"),
                    id_cotizacion = resultSet1.getString("id_cotizacion"),
                    id_cliente = resultSet1.getString("id_cliente"),
                    id_usuario_operativo = resultSet1.getString("id_usuario_operativo"),
                    id_proveedor = resultSet1.getString("id_proveedor"),
                    id_agente = resultSet1.getString("id_agente"),
                    tipo_servicio = resultSet1.getString("tipo_servicio"),
                    tipo_carga = resultSet1.getString("tipo_carga"),
                    incoterm = resultSet1.getString("incoterm"),
                    fecha_inicio_operacion = resultSet1.getString("fecha_inicio_operacion"),
                    fecha_estimada_arribo = resultSet1.getString("fecha_estimada_arribo"),
                    fecha_estimada_entrega = resultSet1.getString("fecha_estimada_entrega"),
                    fecha_arribo_real = resultSet1.getString("fecha_arribo_real"),
                    fecha_entrega_real = resultSet1.getString("fecha_entrega_real"),
                    estatus = resultSet1.getString("estatus"),
                    numero_referencia_proveedor = resultSet1.getString("numero_referencia_proveedor"),
                    notas_operacion = resultSet1.getString("notas_operacion"),
                    fecha_creacion = resultSet1.getString("fecha_creacion"),
                    proveedorNombre = resultSet1.getString("proveedor_nombre"),
                    operativoNombre = resultSet1.getString("operativo_nombre"),
                    operativoApellido = resultSet1.getString("operativo_apellido")
                )
            } else {
                return@withContext ApiResponse("ERROR", "Operación no encontrada")
            }
            
            // Obtener tracking (segundo result set)
            val tracking = mutableListOf<TrackingInfo>()
            if (statement.getMoreResults()) {
                val resultSet2 = statement.resultSet
                while (resultSet2?.next() == true) {
                    tracking.add(TrackingInfo(
                        id_tracking = resultSet2.getString("id_tracking"),
                        fecha_hora_actualizacion = resultSet2.getString("fecha_hora_actualizacion"),
                        ubicacion_actual = resultSet2.getString("ubicacion_actual"),
                        estatus_seguimiento = resultSet2.getString("estatus_seguimiento"),
                        referencia_transportista = resultSet2.getString("referencia_transportista"),
                        nombre_transportista = resultSet2.getString("nombre_transportista"),
                        notas_tracking = resultSet2.getString("notas_tracking")
                    ))
                }
            }
            
            // Obtener incidencias (tercer result set)
            val incidencias = mutableListOf<IncidenciaInfo>()
            if (statement.getMoreResults()) {
                val resultSet3 = statement.resultSet
                while (resultSet3?.next() == true) {
                    incidencias.add(IncidenciaInfo(
                        id_incidencia = resultSet3.getString("id_incidencia"),
                        fecha_hora_incidencia = resultSet3.getString("fecha_hora_incidencia"),
                        descripcion_incidencia = resultSet3.getString("descripcion_incidencia"),
                        tipo_incidencia = resultSet3.getString("tipo_incidencia"),
                        estatus = resultSet3.getString("estatus"),
                        fecha_resolucion = resultSet3.getString("fecha_resolucion"),
                        comentarios_resolucion = resultSet3.getString("comentarios_resolucion")
                    ))
                }
            }
            
            // Obtener demoras (cuarto result set)
            val demoras = mutableListOf<DemoraInfo>()
            if (statement.getMoreResults()) {
                val resultSet4 = statement.resultSet
                while (resultSet4?.next() == true) {
                    demoras.add(DemoraInfo(
                        id_demora = resultSet4.getString("id_demora"),
                        fecha_hora_demora = resultSet4.getString("fecha_hora_demora"),
                        descripcion_demora = resultSet4.getString("descripcion_demora"),
                        tipo_demora = resultSet4.getString("tipo_demora"),
                        costo_asociado = resultSet4.getDouble("costo_asociado"),
                        moneda = resultSet4.getString("moneda")
                    ))
                }
            }
            
            val operationDetail = OperationDetail(
                operacion = operation,
                tracking = tracking,
                incidencias = incidencias,
                demoras = demoras
            )
            
            ApiResponse("OK", "Detalle de operación obtenido exitosamente", operationDetail)
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al obtener detalle de operación: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun getInvoiceDetail(invoiceId: String): ApiResponse<FacturaDetail> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_obtener_detalle_factura(?)}")
            statement.setString(1, invoiceId)
            
            statement.execute()
            
            // Obtener datos básicos de la factura (primer result set)
            val resultSet1 = statement.resultSet
            val factura = if (resultSet1?.next() == true) {
                FacturaExtended(
                    id_factura_cliente = resultSet1.getString("id_factura_cliente"),
                    id_cliente = resultSet1.getString("id_cliente"),
                    id_operacion = resultSet1.getString("id_operacion"),
                    id_cotizacion = resultSet1.getString("id_cotizacion"),
                    numero_factura = resultSet1.getString("numero_factura"),
                    fecha_emision = resultSet1.getString("fecha_emision"),
                    fecha_vencimiento = resultSet1.getString("fecha_vencimiento"),
                    monto_total = resultSet1.getDouble("monto_total"),
                    monto_pagado = resultSet1.getDouble("monto_pagado"),
                    moneda = resultSet1.getString("moneda"),
                    estatus = resultSet1.getString("estatus"),
                    observaciones = resultSet1.getString("observaciones"),
                    fecha_pago = resultSet1.getString("fecha_pago"),
                    fecha_creacion = resultSet1.getString("fecha_creacion"),
                    operacionReferencia = resultSet1.getString("operacion_referencia"),
                    cotizacionReferencia = resultSet1.getString("cotizacion_referencia")
                )
            } else {
                return@withContext ApiResponse("ERROR", "Factura no encontrada")
            }
            
            // Obtener notas de crédito (segundo result set)
            val notasCredito = mutableListOf<NotaCreditoInfo>()
            if (statement.getMoreResults()) {
                val resultSet2 = statement.resultSet
                while (resultSet2?.next() == true) {
                    notasCredito.add(NotaCreditoInfo(
                        id_nota_credito = resultSet2.getString("id_nota_credito"),
                        numero_nota_credito = resultSet2.getString("numero_nota_credito"),
                        fecha_emision = resultSet2.getString("fecha_emision"),
                        monto = resultSet2.getDouble("monto"),
                        moneda = resultSet2.getString("moneda"),
                        motivo = resultSet2.getString("motivo")
                    ))
                }
            }
            
            val facturaDetail = FacturaDetail(
                factura = factura,
                notasCredito = notasCredito
            )
            
            ApiResponse("OK", "Detalle de factura obtenido exitosamente", facturaDetail)
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al obtener detalle de factura: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun getCountries(): ApiResponse<List<Pais>> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_obtener_paises()}")
            
            val resultSet = statement.executeQuery()
            val countries = mutableListOf<Pais>()
            
            while (resultSet.next()) {
                countries.add(Pais(
                    id_pais = resultSet.getString("id_pais"),
                    nombre_pais = resultSet.getString("nombre_pais"),
                    codigo_iso2 = resultSet.getString("codigo_iso2")
                ))
            }
            
            ApiResponse("OK", "Países obtenidos exitosamente", countries)
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al obtener países: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    suspend fun getCitiesByCountry(countryId: String): ApiResponse<List<Localizacion>> = withContext(Dispatchers.IO) {
        try {
            val conn = getConnection() ?: return@withContext ApiResponse("ERROR", "Error de conexión a la base de datos")
            
            val statement = conn.prepareCall("{CALL sp_obtener_ciudades_por_pais(?)}")
            statement.setString(1, countryId)
            
            val resultSet = statement.executeQuery()
            val cities = mutableListOf<Localizacion>()
            
            while (resultSet.next()) {
                cities.add(Localizacion(
                    id_localizacion = resultSet.getString("id_localizacion"),
                    id_pais = countryId,
                    nombre_ciudad = resultSet.getString("nombre_ciudad"),
                    tipo_ubicacion = resultSet.getString("tipo_ubicacion"),
                    codigo_iata_icao = resultSet.getString("codigo_iata_icao"),
                    direccion = resultSet.getString("direccion")
                ))
            }
            
            ApiResponse("OK", "Ciudades obtenidas exitosamente", cities)
            
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al obtener ciudades: ${e.message}")
            ApiResponse("ERROR", "Error interno del servidor")
        }
    }
    
    fun closeConnection() {
        try {
            connection?.close()
            Log.d("DatabaseManager", "Conexión cerrada")
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error al cerrar conexión: ${e.message}")
        }
    }
}
