package com.example.crm_logistico_movil.models

import kotlinx.datetime.Instant

// Request Models
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterClienteRequest(
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String,
    val nombre_empresa: String,
    val rfc: String,
    val direccion: String? = null,
    val ciudad: String? = null,
    val pais: String? = null,
    val telefono: String? = null
)

data class SolicitudRequest(
    val id_cliente: String,
    val tipo_servicio: String,
    val tipo_carga: String,
    val origen_ciudad: String,
    val origen_pais: String,
    val destino_ciudad: String,
    val destino_pais: String,
    val descripcion_mercancia: String? = null,
    val valor_estimado_mercancia: Double? = null
)

// Response Models
data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null
)

data class LoginResponse(
    val status: String,
    val message: String,
    val user: List<User>
)

data class RegisterResponse(
    val id_usuario: String?,
    val status: String,
    val message: String,
    val usuario: List<User>
)

data class UserResponse(
    val user: List<User>?
)

data class SolicitudResponse(
    val id_solicitud: String?,
    val status: String,
    val message: String,
    val created: List<Solicitud>?
)

data class ProcedureResponse(
    val args: List<Any?>,
    val results: List<List<Map<String, Any>>>,
    val status: String = "",
    val cotizaciones: List<Map<String, Any>>? = null,
    val solicitudes: List<Map<String, Any>>? = null
)

// Responses for dedicated client endpoints
data class OperacionesListResponse(
    val operaciones: List<Map<String, Any>>
)

data class CotizacionesListResponse(
    val cotizaciones: List<Map<String, Any>>
)

data class SolicitudesListResponse(
    val solicitudes: List<Map<String, Any>>
)

// Data Models
data class User(
    val id_usuario: String,
    val nombre: String,
    val apellido: String,
    val email: String,
    val rol: String,
    val activo: Boolean,
    val fecha_creacion: Instant,
    val nombre_empresa: String? = null,
    val rfc: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val ciudad: String? = null,
    val pais: String? = null
)

data class Solicitud(
    val id_solicitud: String,
    val id_cliente: String,
    val tipo_servicio: String,
    val tipo_carga: String,
    val origen_ciudad: String,
    val origen_pais: String,
    val destino_ciudad: String,
    val destino_pais: String,
    val fecha_solicitud: String,
    val descripcion_mercancia: String?,
    val valor_estimado_mercancia: Double?,
    val estatus: String
)

// Backwards-compatible RegisterRequest used by ViewModel
data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String,
    val nombreEmpresa: String,
    val rfc: String,
    val direccion: String? = null,
    val ciudad: String? = null,
    val pais: String? = null,
    val telefono: String? = null
)