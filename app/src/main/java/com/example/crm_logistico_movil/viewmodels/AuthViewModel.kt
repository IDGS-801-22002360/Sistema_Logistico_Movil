package com.example.crm_logistico_movil.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.repository.AuthRepositoryAPI
import com.example.crm_logistico_movil.repository.UnifiedAuthRepository
import com.example.crm_logistico_movil.repository.SessionManager
import com.example.crm_logistico_movil.services.NotificationService

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel : ViewModel() {

    // MIGRACIÓN: Usar UnifiedAuthRepository que puede cambiar entre sistemas según configuración
    private val authRepository = UnifiedAuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Initialize from SessionManager if a user is already stored
        SessionManager.currentUser?.let { user ->
            _uiState.value = _uiState.value.copy(currentUser = user, isLoggedIn = true)
        }

        // Log cual sistema se está usando
        Log.d("AuthViewModel", "Initialized with: ${authRepository.getMigrationStatus()}")
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                Log.d("AuthViewModel", "Attempting login for: $email")
                val result = authRepository.login(email, password)
                Log.d("AuthViewModel", "Login result success: ${result.isSuccess}")

                if (result.isSuccess) {
                    val body = result.getOrNull()
                    Log.d("AuthViewModel", "Login response body: $body")

                    // Check if the API returned success status (1 = success, 0 = failure)
                    if (body?.status == 1) {
                        val user = body.user?.firstOrNull()
                        Log.d("AuthViewModel", "Parsed user: $user")

                        if (user != null) {
                            // persist to global session so other screens can see it
                            SessionManager.currentUser = user
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                currentUser = user,
                                isLoggedIn = true,
                                successMessage = body.message ?: "Login correcto"
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "No se pudo obtener la información del usuario",
                                isLoggedIn = false
                            )
                        }
                    } else {
                        // API returned error status (0)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = body?.message ?: "Credenciales inválidas",
                            isLoggedIn = false
                        )
                    }
                } else {
                    Log.e("AuthViewModel", "Login failed: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al autenticar",
                        isLoggedIn = false
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login exception: ${e.message}", e)
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

                    // Check if the API returned success status (1 = success, 0 = failure)
                    if (body?.status == 1) {
                        val user = body.usuario?.firstOrNull()
                        if (user != null) {
                            SessionManager.currentUser = user

                            // Emitir notificación de bienvenida
                            NotificationService.notifyClienteRegistered(
                                clientId = user.id_usuario,
                                nombreCliente = "${user.nombre} ${user.apellido}"
                            )

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                currentUser = user,
                                isLoggedIn = true,
                                successMessage = body.message ?: "Registro exitoso"
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "No se pudo obtener la información del usuario",
                                isLoggedIn = false
                            )
                        }
                    } else {
                        // API returned error status (0)
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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Funcionalidad Temporal no disponible"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error de conexión: ${e.message}")
            }
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Funcionalidad Temporal no disponible"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Error de conexión: ${e.message}")
            }
        }
    }

    suspend fun checkEmailExists(email: String): Boolean {
        return try {
            false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkRfcExists(rfc: String): Boolean {
        return try {
            false
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