package com.example.crm_logistico_movil.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.time.LocalDateTime

data class User(
    val id_usuario: String,
    val nombre: String,
    val apellido: String,
    val email: String,
    val rol: String, // Considera un enum si se va a usar funcionalmente
    val activo: Boolean = true,
    val fecha_creacion: Instant = Clock.System.now()
)