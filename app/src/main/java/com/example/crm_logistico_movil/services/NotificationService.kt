package com.example.crm_logistico_movil.services

import android.util.Log
import com.example.crm_logistico_movil.models.NotificationEvent
import com.example.crm_logistico_movil.viewmodels.NotificationViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationService {
    private var notificationViewModel: NotificationViewModel? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    // Cache para evitar notificaciones duplicadas
    private val generatedNotifications = mutableSetOf<String>()

    private val _notificationEvents = MutableSharedFlow<NotificationEvent>()
    val notificationEvents: SharedFlow<NotificationEvent> = _notificationEvents.asSharedFlow()

    fun initialize(viewModel: NotificationViewModel) {
        Log.d("NotificationService", "Initializing NotificationService with ViewModel")
        notificationViewModel = viewModel
        Log.d("NotificationService", "NotificationViewModel is now: ${notificationViewModel != null}")
    }

    fun emitNotification(event: NotificationEvent) {
        Log.d("NotificationService", "=== EMIT NOTIFICATION START ===")
        Log.d("NotificationService", "Event: $event")
        Log.d("NotificationService", "NotificationViewModel available: ${notificationViewModel != null}")

        // Crear una clave única para evitar duplicados
        val notificationKey = createNotificationKey(event)
        Log.d("NotificationService", "Generated key: $notificationKey")

        if (generatedNotifications.contains(notificationKey)) {
            Log.d("NotificationService", "Notification already exists, skipping: $notificationKey")
            return
        }

        Log.d("NotificationService", "Adding key to cache and proceeding...")
        generatedNotifications.add(notificationKey)

        serviceScope.launch {
            Log.d("NotificationService", "Inside coroutine, calling ViewModel...")
            notificationViewModel?.addNotification(event)
                ?: Log.w("NotificationService", "NotificationViewModel is null!")
            Log.d("NotificationService", "ViewModel called, emitting event...")
            _notificationEvents.emit(event)
            Log.d("NotificationService", "=== EMIT NOTIFICATION END ===")
        }
    }

    private fun createNotificationKey(event: NotificationEvent): String {
        return when (event) {
            is NotificationEvent.SolicitudCreated -> "solicitud_${event.solicitudId}_${event.clientId}"
            is NotificationEvent.CotizacionGenerated -> "cotizacion_${event.cotizacionId}_${event.clientId}"
            is NotificationEvent.OperacionAssigned -> "operacion_assigned_${event.operacionId}_${event.clientId}"
            is NotificationEvent.FacturaGenerated -> "factura_${event.facturaId}_${event.clientId}"
            is NotificationEvent.FacturaVencimiento -> "vencimiento_${event.facturaId}_${event.clientId}"
            is NotificationEvent.OperacionUpdated -> "operacion_updated_${event.operacionId}${event.nuevoEstatus}${event.clientId}"
            is NotificationEvent.IncidenciaReported -> "incidencia_${event.operacionId}_${event.clientId}"
            is NotificationEvent.ClienteRegistered -> "cliente_${event.clientId}"
        }
    }

    fun clearNotificationCache() {
        generatedNotifications.clear()
        Log.d("NotificationService", "Notification cache cleared")
    }

    // Métodos de conveniencia para emitir notificaciones específicas (ahora sincrónicos)
    fun notifySolicitudCreated(solicitudId: String, clientId: String) {
        emitNotification(NotificationEvent.SolicitudCreated(solicitudId, clientId))
    }

    fun notifyCotizacionGenerated(cotizacionId: String, clientId: String) {
        emitNotification(NotificationEvent.CotizacionGenerated(cotizacionId, clientId))
    }

    fun notifyOperacionAssigned(operacionId: String, clientId: String) {
        emitNotification(NotificationEvent.OperacionAssigned(operacionId, clientId))
    }

    fun notifyFacturaGenerated(facturaId: String, clientId: String, monto: String) {
        emitNotification(NotificationEvent.FacturaGenerated(facturaId, clientId, monto))
    }

    fun notifyFacturaVencimiento(facturaId: String, clientId: String, diasRestantes: Int) {
        emitNotification(NotificationEvent.FacturaVencimiento(facturaId, clientId, diasRestantes))
    }

    fun notifyOperacionUpdated(operacionId: String, clientId: String, nuevoEstatus: String) {
        emitNotification(NotificationEvent.OperacionUpdated(operacionId, clientId, nuevoEstatus))
    }

    fun notifyIncidenciaReported(operacionId: String, clientId: String, descripcion: String) {
        emitNotification(NotificationEvent.IncidenciaReported(operacionId, clientId, descripcion))
    }

    fun notifyClienteRegistered(clientId: String, nombreCliente: String) {
        emitNotification(NotificationEvent.ClienteRegistered(clientId, nombreCliente))
    }
}