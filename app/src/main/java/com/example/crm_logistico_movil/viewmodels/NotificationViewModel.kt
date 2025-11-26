package com.example.crm_logistico_movil.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.services.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false
)

class NotificationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _notifications = mutableListOf<AppNotification>()

    init {
        // Inicializar con notificaciones de bienvenida si es necesario
        loadNotifications()
    }

    fun addNotification(event: NotificationEvent) {
        viewModelScope.launch {
            Log.d("NotificationViewModel", "=== ADD NOTIFICATION START ===")
            Log.d("NotificationViewModel", "Received event: $event")

            val notification = createNotificationFromEvent(event)
            Log.d("NotificationViewModel", "Created notification: ${notification.title} for client: ${notification.clientId}")

            _notifications.add(0, notification) // Agregar al inicio (más recientes primero)
            Log.d("NotificationViewModel", "Added to list, current size: ${_notifications.size}")

            // Mantener solo las últimas 50 notificaciones para evitar memoria excesiva
            if (_notifications.size > 50) {
                _notifications.removeAt(_notifications.size - 1)
            }

            Log.d("NotificationViewModel", "Calling updateUiState...")
            updateUiState()
            Log.d("NotificationViewModel", "=== ADD NOTIFICATION END ===")
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val index = _notifications.indexOfFirst { it.id == notificationId }
            if (index != -1) {
                _notifications[index] = _notifications[index].copy(isRead = true)
                updateUiState()
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            for (i in _notifications.indices) {
                _notifications[i] = _notifications[i].copy(isRead = true)
            }
            updateUiState()
        }
    }

    fun clearNotification(notificationId: String) {
        viewModelScope.launch {
            _notifications.removeAll { it.id == notificationId }
            updateUiState()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            _notifications.clear()
            NotificationService.clearNotificationCache()
            updateUiState()
        }
    }

    fun getNotificationsForClient(clientId: String): List<AppNotification> {
        return _notifications.filter { it.clientId == clientId || it.clientId == null }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Aquí puedes cargar notificaciones guardadas si implementas persistencia
            // Por ahora, empezamos sin notificaciones

            updateUiState()
        }
    }

    private fun updateUiState() {
        val unreadCount = _notifications.count { !it.isRead }

        // Debug log
        Log.d("NotificationViewModel", "Updating UI State - Total: ${_notifications.size}, Unread: $unreadCount")

        _uiState.value = NotificationsUiState(
            notifications = _notifications.toList(),
            unreadCount = unreadCount,
            isLoading = false
        )
    }

    private fun createNotificationFromEvent(event: NotificationEvent): AppNotification {
        return when (event) {
            is NotificationEvent.SolicitudCreated -> AppNotification(
                title = "Solicitud de cotización creada",
                message = "Tu solicitud ha sido registrada exitosamente. Pronto recibirás una respuesta.",
                type = NotificationType.SOLICITUD_CREADA,
                relatedId = event.solicitudId,
                clientId = event.clientId
            )

            is NotificationEvent.CotizacionGenerated -> AppNotification(
                title = "Nueva cotización disponible",
                message = "Se ha generado una cotización para tu solicitud. Revísala en la sección correspondiente.",
                type = NotificationType.COTIZACION_GENERADA,
                relatedId = event.cotizacionId,
                clientId = event.clientId
            )

            is NotificationEvent.OperacionAssigned -> AppNotification(
                title = "Nueva operación asignada",
                message = "Se ha creado una nueva operación logística. Puedes seguir su progreso en tiempo real.",
                type = NotificationType.OPERACION_ASIGNADA,
                relatedId = event.operacionId,
                clientId = event.clientId
            )

            is NotificationEvent.FacturaGenerated -> AppNotification(
                title = "Factura generada",
                message = "Se ha generado una factura por $${event.monto}. Revisa los detalles de facturación.",
                type = NotificationType.FACTURA_GENERADA,
                relatedId = event.facturaId,
                clientId = event.clientId
            )

            is NotificationEvent.FacturaVencimiento -> AppNotification(
                title = "Factura próxima a vencer",
                message = "Tu factura vence en ${event.diasRestantes} día${if (event.diasRestantes != 1) "s" else ""}. Procesa el pago a tiempo.",
                type = NotificationType.FACTURA_VENCIMIENTO,
                relatedId = event.facturaId,
                clientId = event.clientId
            )

            is NotificationEvent.OperacionUpdated -> AppNotification(
                title = "Operación actualizada",
                message = "El estatus de tu operación ha cambiado a: ${event.nuevoEstatus}",
                type = NotificationType.OPERACION_ACTUALIZADA,
                relatedId = event.operacionId,
                clientId = event.clientId
            )

            is NotificationEvent.IncidenciaReported -> AppNotification(
                title = "Incidencia reportada",
                message = "Se ha reportado una incidencia en tu operación: ${event.descripcion}",
                type = NotificationType.INCIDENCIA_REPORTADA,
                relatedId = event.operacionId,
                clientId = event.clientId
            )

            is NotificationEvent.ClienteRegistered -> AppNotification(
                title = "¡Bienvenido a LogiCorp!",
                message = "Hola ${event.nombreCliente}, tu cuenta ha sido activada exitosamente. ¡Comienza a gestionar tus operaciones logísticas!",
                type = NotificationType.CLIENTE_REGISTRADO,
                relatedId = event.clientId,
                clientId = event.clientId
            )
        }
    }
}