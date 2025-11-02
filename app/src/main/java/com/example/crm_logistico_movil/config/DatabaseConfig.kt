package com.example.crm_logistico_movil.config

object DatabaseConfig {
    // IMPORTANTE: Cambia estos valores por los de tu configuración MySQL
    const val DB_HOST = "10.0.2.2" // Cambia por la IP de tu servidor MySQL
    const val DB_PORT = "3306"
    const val DB_NAME = "logistica"
    const val DB_USER = "root" // Cambia por tu usuario MySQL
    const val DB_PASSWORD = "Root" // Cambia por tu contraseña MySQL
    
    // Para desarrollo local, usa localhost o 127.0.0.1
    // Para servidor remoto, usa la IP del servidor
    val DB_URL = "jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME"
    
    // Configuraciones adicionales
    const val CONNECTION_TIMEOUT = 30000 // 30 segundos
    const val SOCKET_TIMEOUT = 60000 // 60 segundos
}
