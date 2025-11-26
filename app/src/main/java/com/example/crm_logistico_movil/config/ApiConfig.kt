package com.example.crm_logistico_movil.config

object ApiConfig {
    // URL base de las APIs de Nest.js en Render
    const val BASE_URL = "https://pwa-sistema-logistico-backend.onrender.com/movil/"

    // Timeouts aumentados para manejar la latencia de Render
    const val CONNECTION_TIMEOUT = 45L
    const val READ_TIMEOUT = 45L

    // Headers para CORS
    const val CONTENT_TYPE = "application/json"
    const val ACCEPT = "application/json"
}