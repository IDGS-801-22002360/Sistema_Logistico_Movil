package com.example.crm_logistico_movil.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.*

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Date = Date(),
    val isRead: Boolean = false,
    val relatedId: String? = null, // ID de la operación, solicitud, etc.
    val clientId: String? = null
) {
    fun getFormattedTime(): String {
        val now = Date()
        val diffInMs = now.time - timestamp.time
        val diffInMinutes = diffInMs / (1000 * 60)
        val diffInHours = diffInMs / (1000 * 60 * 60)
        val diffInDays = diffInMs / (1000 * 60 * 60 * 24)

        return when {
            diffInMinutes < 1 -> "Hace unos segundos"
            diffInMinutes < 60 -> "Hace ${diffInMinutes.toInt()} minuto${if (diffInMinutes.toInt() != 1) "s" else ""}"
            diffInHours < 24 -> "Hace ${diffInHours.toInt()} hora${if (diffInHours.toInt() != 1) "s" else ""}"
            diffInDays < 7 -> "Hace ${diffInDays.toInt()} día${if (diffInDays.toInt() != 1) "s" else ""}"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(timestamp)
        }
    }

    fun getIcon(): ImageVector {
        return when (type) {
            NotificationType.SOLICITUD_CREADA -> Icons.Default.RequestPage
            NotificationType.COTIZACION_GENERADA -> Icons.Default.Assignment
            NotificationType.OPERACION_ASIGNADA -> Icons.Default.LocalShipping
            NotificationType.FACTURA_GENERADA -> Icons.Default.Receipt
            NotificationType.FACTURA_VENCIMIENTO -> Icons.Default.Warning
            NotificationType.OPERACION_ACTUALIZADA -> Icons.Default.Update
            NotificationType.INCIDENCIA_REPORTADA -> Icons.Default.Error
            NotificationType.CLIENTE_REGISTRADO -> Icons.Default.PersonAdd
            NotificationType.SISTEMA -> Icons.Default.Info
        }
    }

    fun getColor(): Color {
        return when (type) {
            NotificationType.SOLICITUD_CREADA -> Color(0xFF2E7D32) // Verde
            NotificationType.COTIZACION_GENERADA -> Color(0xFF1565C0) // Azul
            NotificationType.OPERACION_ASIGNADA -> Color(0xFF1976D2) // Azul más claro
            NotificationType.FACTURA_GENERADA -> Color(0xFF6A1B9A) // Púrpura
            NotificationType.FACTURA_VENCIMIENTO -> Color(0xFFF57C00) // Naranja
            NotificationType.OPERACION_ACTUALIZADA -> Color(0xFF00796B) // Teal
            NotificationType.INCIDENCIA_REPORTADA -> Color(0xFFE91E63) // Rosa/Rojo
            NotificationType.CLIENTE_REGISTRADO -> Color(0xFF6A1B9A) // Púrpura
            NotificationType.SISTEMA -> Color(0xFF424242) // Gris
        }
    }
}

enum class NotificationType {
    SOLICITUD_CREADA,
    COTIZACION_GENERADA,
    OPERACION_ASIGNADA,
    FACTURA_GENERADA,
    FACTURA_VENCIMIENTO,
    OPERACION_ACTUALIZADA,
    INCIDENCIA_REPORTADA,
    CLIENTE_REGISTRADO,
    SISTEMA
}

// Eventos que pueden generar notificaciones
sealed class NotificationEvent {
    data class SolicitudCreated(val solicitudId: String, val clientId: String) : NotificationEvent()
    data class CotizacionGenerated(val cotizacionId: String, val clientId: String) : NotificationEvent()
    data class OperacionAssigned(val operacionId: String, val clientId: String) : NotificationEvent()
    data class FacturaGenerated(val facturaId: String, val clientId: String, val monto: String) : NotificationEvent()
    data class FacturaVencimiento(val facturaId: String, val clientId: String, val diasRestantes: Int) : NotificationEvent()
    data class OperacionUpdated(val operacionId: String, val clientId: String, val nuevoEstatus: String) : NotificationEvent()
    data class IncidenciaReported(val operacionId: String, val clientId: String, val descripcion: String) : NotificationEvent()
    data class ClienteRegistered(val clientId: String, val nombreCliente: String) : NotificationEvent()
}