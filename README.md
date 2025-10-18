# Sistema Logístico Móvil - editado

Aplicación móvil desarrollada en **Kotlin con Jetpack Compose**, diseñada para optimizar la gestión logística tanto para **operativos internos** como para **clientes finales**.  
El sistema permite administrar operaciones, incidencias, cotizaciones, facturas y comunicación interna, todo desde una interfaz móvil centralizada.

---

## Características Generales

### Inicio de Sesión y Seguridad
- Registro / Login mediante **correo y contraseña**.
- Recuperación de contraseña vía **email**.
- **Cierre automático por inactividad**.
- Visualización de **datos básicos del perfil** del usuario.

### Notificaciones
Bandeja de notificaciones centralizada con eventos como:
- Cambios de estatus en operaciones.
- Recepción de nuevas facturas.
- Recordatorios de pago.
- Alertas de incidencias o demoras.
- Nuevas cotizaciones recibidas.

### Soporte
- Envío de comentarios o reportes directamente desde la app.

---

## Funcionalidades para el Operativo

### Gestión de Operaciones
- **Lista de operaciones asignadas**, con:
  - Filtros por estatus, cliente, tipo de servicio o rango de fechas.
  - Búsqueda por número de operación, referencia o cliente.
  - Ordenamiento por fecha, prioridad o estatus.
- **Detalle completo de cada operación**, incluyendo:
  - Información general: cliente, proveedor, agentes, tipo de carga, fechas estimadas y reales.
  - Edición de campos clave como referencias o notas internas.
  - Gestión de fechas reales de arribo y entrega.
  - El estatus del proceso se actualiza automáticamente según acciones realizadas.

### Gestión de Incidencias
- Registro de **nueva incidencia** con tipo, descripción, fecha/hora y costo.
- **Historial de incidencias** por operación.
- Edición de estatus o comentarios de resolución.

### Gestión de Demoras
- Registro de demoras con tipo, inicio y costo asociado.
- Historial de demoras por operación.
- Edición de información existente si es necesario.

### Comunicación Interna
- **Chat integrado** entre operativos o área de ventas vinculado a cada operación.

---

## Funcionalidades para el Cliente

### Gestión de Operaciones
- Lista de todas sus operaciones con filtros básicos.
- Vista **solo lectura** del detalle de cada operación.
- **Tracking en vivo mediante mapa**, con ruta estimada e historial de eventos.

### Solicitudes de Cotización
- Formulario para solicitar una nueva cotización con datos básicos del envío.
- Historial con estatus de solicitudes enviadas.

### Gestión de Cotizaciones Recibidas
- Lista de cotizaciones tanto solicitadas como enviadas por ventas.
- Filtros por estatus: nuevas, aceptadas, rechazadas o pendientes.
- Detalle con opción para **Aceptar** o **Rechazar**, incluyendo motivo opcional.

### Gestión de Facturas y Pagos
- Lista de facturas con filtros por estatus: pendiente, pagada o vencida.
- Detalle de factura con estado de pago y recordatorios.
- Historial de **notas de crédito** con monto, motivo y fecha.

---

## Arquitectura y Tecnologías

| Componente | Tecnología |
|------------|------------|
| Lenguaje principal | **Kotlin** |
| UI | **Jetpack Compose** |
| Navegación | Navigation Compose |
| Gestión de estado | ViewModel + StateFlow / LiveData |
| Persistencia local (opcional) | Room / DataStore |
| Consumo de API | Retrofit + Kotlin Coroutines |
| Autenticación | Token-based / JWT (según backend) |
| Notificaciones push (opcional) | Firebase Cloud Messaging |

---

## Flujo General de Usuario

### Operativo
1. Inicia sesión y accede a **su panel de operaciones**.
2. Gestiona incidencias, demoras y fechas conforme avanza el servicio.
3. Se comunica con otros usuarios mediante el chat interno.
4. Recibe notificaciones sobre cambios relevantes.

### Cliente
1. Consulta el **estado de sus envíos**.
2. Solicita y acepta/rechaza cotizaciones.
3. Revisa facturas y realiza seguimiento de pagos.
4. Visualiza el tracking en tiempo real de cada operación.

