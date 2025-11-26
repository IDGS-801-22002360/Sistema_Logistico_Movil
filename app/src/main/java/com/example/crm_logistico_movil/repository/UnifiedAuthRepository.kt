package com.example.crm_logistico_movil.repository

import com.example.crm_logistico_movil.config.MigrationConfig
import com.example.crm_logistico_movil.config.MigrationLogger
import com.example.crm_logistico_movil.config.DataSource
import com.example.crm_logistico_movil.database.DatabaseManager
import com.example.crm_logistico_movil.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio unificado que puede usar tanto APIs REST como DatabaseManager
 * para facilitar la migración gradual del sistema.
 *
 * Configurado mediante MigrationConfig.USE_REST_APIS
 */
class UnifiedAuthRepository {

    // Instancias de ambos sistemas
    private val restApiRepository = AuthRepositoryAPI()
    private val databaseManager = DatabaseManager.getInstance()

    // Determinar qué sistema usar basado en configuración
    private val currentDataSource: DataSource = if (MigrationConfig.USE_REST_APIS) {
        DataSource.REST_API
    } else {
        DataSource.LOCAL_DATABASE
    }

    /**
     * Login unificado - usa REST API o DatabaseManager según configuración
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            when (currentDataSource) {
                DataSource.REST_API -> {
                    MigrationLogger.logSystemUsed(DataSource.REST_API, "login")
                    restApiRepository.login(email, password)
                }
                DataSource.LOCAL_DATABASE -> {
                    MigrationLogger.logSystemUsed(DataSource.LOCAL_DATABASE, "login")
                    // Convertir ApiResponse del DatabaseManager a Result
                    val apiResponse = databaseManager.loginUser(email, password)
                    if (apiResponse.status == "OK" && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                }
            }
        } catch (e: Exception) {
            MigrationLogger.logMigrationError("login", e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    /**
     * Registro unificado - usa REST API o DatabaseManager según configuración
     */
    suspend fun register(
        nombre: String,
        apellido: String,
        email: String,
        password: String,
        nombreEmpresa: String,
        rfc: String,
        direccion: String?,
        ciudad: String?,
        pais: String?,
        telefono: String?
    ): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            when (currentDataSource) {
                DataSource.REST_API -> {
                    MigrationLogger.logSystemUsed(DataSource.REST_API, "register")
                    restApiRepository.register(nombre, apellido, email, password, nombreEmpresa, rfc, direccion, ciudad, pais, telefono)
                }
                DataSource.LOCAL_DATABASE -> {
                    MigrationLogger.logSystemUsed(DataSource.LOCAL_DATABASE, "register")
                    // Convertir del nuevo formato al formato del DatabaseManager
                    val registerRequest = RegisterRequest(
                        nombre = nombre,
                        apellido = apellido,
                        email = email,
                        password = password,
                        nombreEmpresa = nombreEmpresa,
                        rfc = rfc,
                        direccion = direccion,
                        ciudad = ciudad,
                        pais = pais,
                        telefono = telefono
                    )

                    val apiResponse = databaseManager.registerClient(registerRequest)
                    if (apiResponse.status == "OK" && apiResponse.data != null) {
                        // Convertir LoginResponse a RegisterResponse
                        val loginResponse = apiResponse.data
                        val registerResponse = RegisterResponse(
                            id_usuario = loginResponse.user.firstOrNull()?.id_usuario,
                            status = loginResponse.status,
                            message = loginResponse.message,
                            usuario = loginResponse.user
                        )
                        Result.success(registerResponse)
                    } else {
                        Result.failure(Exception(apiResponse.message))
                    }
                }
            }
        } catch (e: Exception) {
            MigrationLogger.logMigrationError("register", e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    /**
     * Obtener usuario por ID - usa REST API o DatabaseManager según configuración
     */
    suspend fun getUserById(userId: String): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            when (currentDataSource) {
                DataSource.REST_API -> {
                    MigrationLogger.logSystemUsed(DataSource.REST_API, "getUserById")
                    restApiRepository.getUserById(userId)
                }
                DataSource.LOCAL_DATABASE -> {
                    MigrationLogger.logSystemUsed(DataSource.LOCAL_DATABASE, "getUserById")
                    // DatabaseManager no tiene este método directo, simular respuesta
                    MigrationLogger.logMigrationWarning("getUserById not implemented in DatabaseManager")
                    Result.failure(Exception("Operation not supported in local database mode"))
                }
            }
        } catch (e: Exception) {
            MigrationLogger.logMigrationError("getUserById", e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    /**
     * Método para verificar qué sistema está siendo usado actualmente
     */
    fun getCurrentDataSource(): DataSource = currentDataSource

    /**
     * Método para obtener información sobre el estado de la migración
     */
    fun getMigrationStatus(): String {
        return when (currentDataSource) {
            DataSource.REST_API -> "Usando APIs REST en Render (https://pwa-sistema-logistico-backend.onrender.com/movil/)"
            DataSource.LOCAL_DATABASE -> "Usando conexión directa a base de datos MySQL local"
        }
    }
}