package com.example.crm_logistico_movil.config

/**
 * Configuración para la migración entre sistema local y APIs en la nube
 *
 * MIGRACIÓN DE ARQUITECTURA:
 *
 * ANTES: App móvil -> Conexión directa MySQL -> Base de datos local
 * AHORA: App móvil -> APIs REST (Nest.js) -> Base de datos en Render
 *
 * URLs y Endpoints:
 * Base URL: https://pwa-sistema-logistico-backend.onrender.com/movil/
 *
 * Endpoints migrados:
 * - POST /login-movil (reemplaza sp_login_user)
 * - POST /register_cliente (reemplaza sp_registrar_cliente)
 * - GET /usuario/{id} (reemplaza sp_obtener_usuario_por_id)
 * - GET /cliente/{id}/operaciones (reemplaza sp_obtener_operaciones_cliente)
 * - GET /cliente/{id}/cotizaciones (reemplaza sp_obtener_cotizaciones_cliente)
 * - GET /cliente/{id}/solicitudes (reemplaza sp_obtener_solicitudes_cotizacion_cliente)
 * - GET /cliente/{id}/facturas (reemplaza sp_obtener_facturas_cliente)
 * - PUT /cliente/{id} (reemplaza sp_editar_cliente)
 * - GET /cliente/{id}/info (reemplaza sp_obtener_cliente_usuario_info)
 * - GET /tracking/{id_operacion} (reemplaza sp_obtener_tracking_operacion)
 *
 * CORS configurado en el backend de Nest.js para permitir requests desde la app móvil
 */
object MigrationConfig {

    /**
     * Flag para controlar si usar APIs REST (true) o DatabaseManager local (false)
     *
     * true  = Usar APIs de Render (Recomendado para producción)
     * false = Usar conexión directa a MySQL local (Solo para desarrollo)
     */
    const val USE_REST_APIS = true

    /**
     * Flag para logging detallado durante la migración
     */
    const val ENABLE_MIGRATION_LOGGING = true

    /**
     * Configuración de timeouts para las APIs REST
     * Aumentados para manejar la latencia de Render
     */
    const val REST_API_CONNECT_TIMEOUT = 45L // segundos
    const val REST_API_READ_TIMEOUT = 45L     // segundos

    /**
     * Configuración de reintentos para APIs REST
     */
    const val MAX_RETRY_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 1000L

    /**
     * Headers adicionales para CORS
     */
    val CORS_HEADERS = mapOf(
        "Access-Control-Allow-Origin" to "*",
        "Access-Control-Allow-Methods" to "GET,POST,PUT,DELETE,OPTIONS",
        "Access-Control-Allow-Headers" to "Content-Type,Authorization,Accept"
    )
}

/**
 * Enum para identificar qué sistema está siendo usado
 */
enum class DataSource {
    LOCAL_DATABASE,  // DatabaseManager con conexión directa a MySQL
    REST_API        // APIs REST en Render con Nest.js
}

/**
 * Clase helper para logging de migración
 */
object MigrationLogger {
    private const val TAG = "MIGRATION"

    fun logSystemUsed(source: DataSource, operation: String) {
        if (MigrationConfig.ENABLE_MIGRATION_LOGGING) {
            val system = when (source) {
                DataSource.LOCAL_DATABASE -> "LOCAL_DB"
                DataSource.REST_API -> "REST_API"
            }
            android.util.Log.d(TAG, "[$system] $operation")
        }
    }

    fun logMigrationWarning(message: String) {
        if (MigrationConfig.ENABLE_MIGRATION_LOGGING) {
            android.util.Log.w(TAG, "MIGRATION WARNING: $message")
        }
    }

    fun logMigrationError(operation: String, error: String) {
        android.util.Log.e(TAG, "MIGRATION ERROR in $operation: $error")
    }
}