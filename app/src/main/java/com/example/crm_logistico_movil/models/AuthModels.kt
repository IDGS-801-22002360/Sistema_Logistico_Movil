package com.example.crm_logistico_movil.models

// User class has been moved to User.kt

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String,
    val nombreEmpresa: String,
    val rfc: String,
    val direccion: String,
    val ciudad: String,
    val pais: String,
    val telefono: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null
)

data class LoginResponse(
    val user: User?,
    val token: String? = null
)
