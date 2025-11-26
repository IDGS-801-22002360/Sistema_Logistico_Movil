package com.example.crm_logistico_movil.repository

import com.example.crm_logistico_movil.api.ApiClient
import com.example.crm_logistico_movil.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {
    private val apiService = ApiClient.apiService

    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email, password))
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

            val response = apiService.registerCliente(request)
            if (response.isSuccessful) {
                response.body()?.let { registerClientResponse ->
                    // Convertir RegisterClientResponse a RegisterResponse para compatibilidad
                    val registerResponse = RegisterResponse(
                        id_usuario = registerClientResponse.id_usuario.toString(),
                        status = registerClientResponse.status,
                        message = registerClientResponse.message,
                        usuario = emptyList() // Lista vacía para compatibilidad
                    )
                    Result.success(registerResponse)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                val err = try { response.errorBody()?.string() } catch (_: Exception) { null }
                val msg = buildString {
                    append("Error: ${response.code()} - ${response.message()}")
                    if (!err.isNullOrBlank()) append(": $err")
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<UserResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsuario(userId)
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

    suspend fun callProcedure(procName: String, params: List<Any?>): Result<ProcedureResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.callProcedure(procName, mapOf("params" to params))
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
