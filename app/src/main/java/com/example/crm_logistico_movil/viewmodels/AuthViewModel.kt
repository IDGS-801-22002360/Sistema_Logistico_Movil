package com.example.crm_logistico_movil.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.repository.AuthRepository
import com.example.crm_logistico_movil.repository.SessionManager

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel : ViewModel() {
    
    private val authRepository = AuthRepository()
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Initialize from SessionManager if a user is already stored
        SessionManager.currentUser?.let { user ->
            _uiState.value = _uiState.value.copy(currentUser = user, isLoggedIn = true)
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = authRepository.login(email, password)
                if (result.isSuccess) {
                    val body = result.getOrNull()
                    val user = body?.user?.firstOrNull()
                    if (user != null) {
                        // persist to global session so other screens can see it
                        SessionManager.currentUser = user
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentUser = user,
                            isLoggedIn = true,
                            successMessage = body?.message ?: "Login correcto"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "Credenciales inválidas",
                            isLoggedIn = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al autenticar",
                        isLoggedIn = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de conexión: ${e.message}",
                    isLoggedIn = false
                )
            }
        }
    }
    
    fun register(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = authRepository.register(
                    nombre = registerRequest.nombre,
                    apellido = registerRequest.apellido,
                    email = registerRequest.email,
                    password = registerRequest.password,
                    nombreEmpresa = registerRequest.nombreEmpresa,
                    rfc = registerRequest.rfc,
                    direccion = registerRequest.direccion,
                    ciudad = registerRequest.ciudad,
                    pais = registerRequest.pais,
                    telefono = registerRequest.telefono
                )

                if (result.isSuccess) {
                    val body = result.getOrNull()
                    val user = body?.usuario?.firstOrNull()
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentUser = user,
                            isLoggedIn = true,
                            successMessage = body?.message ?: "Registro exitoso"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "No se pudo registrar",
                            isLoggedIn = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar",
                        isLoggedIn = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de conexión: ${e.message}",
                    isLoggedIn = false
                )
            }
        }
    }
    
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = authRepository.callProcedure("sp_request_password_reset", listOf(email))
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Token generado (ver consola o correo)"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al solicitar reset"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error de conexión: ${e.message}")
            }
        }
    }
    
    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = authRepository.callProcedure("sp_reset_password", listOf(token, newPassword))
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Contraseña actualizada")
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Error al resetear contraseña")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error de conexión: ${e.message}")
            }
        }
    }
    
    suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val res = authRepository.callProcedure("sp_verificar_email", listOf(email))
            if (res.isSuccess) {
                val proc = res.getOrNull()
                val rows = proc?.results?.getOrNull(0) ?: emptyList()
                // procedure returns OUT status/message but we treat success if no exception
                rows.isNotEmpty().not() // best-effort: return false if any row? fallback
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkRfcExists(rfc: String): Boolean {
        return try {
            val res = authRepository.callProcedure("sp_verificar_rfc", listOf(rfc))
            res.isSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    fun logout() {
        _uiState.value = AuthUiState()
        SessionManager.currentUser = null
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
