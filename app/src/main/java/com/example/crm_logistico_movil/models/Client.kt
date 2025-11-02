package com.example.crm_logistico_movil.models

import java.time.LocalDateTime
import kotlinx.datetime.*


data class Client(
    val id_cliente: String,
    val nombre_empresa: String,
    val rfc: String,
    val direccion: String?,
    val ciudad: String?,
    val pais: String?,
    val telefono: String?,
    val email_contacto: String?,
    val contacto_nombre: String?,
    val contacto_puesto: String?,
    val fecha_creacion: Instant = Clock.System.now()
)