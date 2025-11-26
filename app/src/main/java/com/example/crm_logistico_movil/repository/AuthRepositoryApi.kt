package com.example.crm_logistico_movil.repository

import com.example.crm_logistico_movil.api.ApiClient
import com.example.crm_logistico_movil.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Nueva implementación del AuthRepository que usa las APIs REST de Render
 * en lugar de la conexión directa a base de datos MySQL.
 *
 * URL Base: https://pwa-sistema-logistico-backend.onrender.com/movil/
 *
 * Endpoints implementados:
 * - POST /login-movil
 * - POST /register_cliente
 * - GET /usuario/{userId}
 * - PUT /cliente/{clientId}
 */
class AuthRepositoryAPI {
    private val apiService = ApiClient.apiService

    /**
     * Login usando la API REST de Nest.js en Render
     * Endpoint: POST /movil/login-movil
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // La API de Nest devuelve status: number (1 = success, 0 = error)
                    // Convertir User de la respuesta para agregar id_cliente si viene
                    val updatedUsers = loginResponse.user.map { user ->
                        // Si el usuario tiene un id_cliente, agregarlo al User
                        val idCliente = if (user.rol == "cliente") user.id_usuario else null
                        user.copy(id_cliente = idCliente)
                    }

                    Result.success(loginResponse.copy(user = updatedUsers))
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
     * Registro de cliente usando la API REST de Nest.js en Render
     * Endpoint: POST /movil/register_cliente
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
            println("🔄 Registrando cliente: $email")
            val request = RegisterClientRequest(
                nombre = nombre,
                apellido = apellido,
                email = email,
                password = password,
                nombre_empresa = nombreEmpresa,
                rfc = rfc,
                direccion = direccion ?: "",
                ciudad = ciudad ?: "",
                pais = pais ?: "",
                telefono = telefono ?: ""
            )
            println("📤 Request enviado: $request")

            val response = apiService.registerCliente(request)
            println("🌐 Response code: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { registerClientResponse ->
                    println("✅ Registro exitoso: $registerClientResponse")
                    // Convertir RegisterClientResponse a RegisterResponse para mantener compatibilidad
                    val registerResponse = RegisterResponse(
                        id_usuario = registerClientResponse.id_usuario.toString(), // Convertir Int a String
                        status = registerClientResponse.status,
                        message = registerClientResponse.message,
                        usuario = emptyList() // Lista vacía de usuarios para compatibilidad
                    )
                    Result.success(registerResponse)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (e: Exception) { "Unknown error" }
                val errorMsg = "Error ${response.code()}: ${response.message()} - $errorBody"
                println("❌ Error en registro: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            println("💥 Exception en register: ${e.message}")
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    /**
     * Obtener información del usuario por ID
     * Endpoint: GET /movil/usuario/{userId}
     */
    suspend fun getUserById(userId: String): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsuario(userId)
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