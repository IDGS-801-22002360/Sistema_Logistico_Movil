package com.example.crm_logistico_movil.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.repository.AuthRepository

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
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val response = authRepository.login(email, password)
                
                if (response.status == "OK" && response.data?.user != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = response.data.user,
                        isLoggedIn = true,
                        successMessage = response.message
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.message,
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
                val response = authRepository.register(registerRequest)
                
                if (response.status == "OK" && response.data?.user != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = response.data.user,
                        isLoggedIn = true,
                        successMessage = response.message
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.message,
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
                val response = authRepository.forgotPassword(email)
                
                if (response.status == "OK") {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de conexión: ${e.message}"
                )
            }
        }
    }
    
    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val response = authRepository.resetPassword(token, newPassword)
                
                if (response.status == "OK") {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = response.message
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de conexión: ${e.message}"
                )
            }
        }
    }
    
    suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val response = authRepository.checkEmailExists(email)
            response.data == true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun checkRfcExists(rfc: String): Boolean {
        return try {
            val response = authRepository.checkRfcExists(rfc)
            response.data == true
        } catch (e: Exception) {
            false
        }
    }
    
    fun logout() {
        _uiState.value = AuthUiState()
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
