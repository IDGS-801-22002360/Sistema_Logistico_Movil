package com.example.crm_logistico_movil.utils

import com.example.crm_logistico_movil.repository.ReferenceDataRepository

/**
 * Utilidades para mostrar información legible en lugar de IDs numéricos
 */
object DisplayUtils {

    /**
     * Convierte ID de proveedor a nombre legible
     */
    suspend fun getProveedorDisplay(proveedorId: String?): String {
        return if (!proveedorId.isNullOrBlank()) {
            ReferenceDataRepository.getProveedorName(proveedorId)
        } else {
            "No especificado"
        }
    }

    /**
     * Convierte ID de país a nombre legible
     */
    suspend fun getPaisDisplay(paisId: String?): String {
        return if (!paisId.isNullOrBlank()) {
            ReferenceDataRepository.getPaisName(paisId)
        } else {
            "No especificado"
        }
    }

    /**
     * Convierte ID de localización a nombre de ciudad legible
     */
    suspend fun getCiudadDisplay(localizacionId: String?): String {
        return if (!localizacionId.isNullOrBlank()) {
            ReferenceDataRepository.getCiudadName(localizacionId)
        } else {
            "No especificado"
        }
    }

    /**
     * Convierte ID de agente a nombre completo legible
     */
    suspend fun getAgenteDisplay(agenteId: String?): String {
        return if (!agenteId.isNullOrBlank()) {
            ReferenceDataRepository.getAgenteName(agenteId)
        } else {
            "No asignado"
        }
    }

    /**
     * Convierte origen (ciudad + país) a formato legible
     */
    suspend fun getOrigenDisplay(ciudadId: String?, paisId: String?): String {
        return if (!ciudadId.isNullOrBlank() && !paisId.isNullOrBlank()) {
            ReferenceDataRepository.getOrigenCompleto(ciudadId, paisId)
        } else if (!ciudadId.isNullOrBlank()) {
            getCiudadDisplay(ciudadId)
        } else if (!paisId.isNullOrBlank()) {
            getPaisDisplay(paisId)
        } else {
            "No especificado"
        }
    }

    /**
     * Convierte destino (ciudad + país) a formato legible
     */
    suspend fun getDestinoDisplay(ciudadId: String?, paisId: String?): String {
        return if (!ciudadId.isNullOrBlank() && !paisId.isNullOrBlank()) {
            ReferenceDataRepository.getDestinoCompleto(ciudadId, paisId)
        } else if (!ciudadId.isNullOrBlank()) {
            getCiudadDisplay(ciudadId)
        } else if (!paisId.isNullOrBlank()) {
            getPaisDisplay(paisId)
        } else {
            "No especificado"
        }
    }

    /**
     * Convierte cualquier valor a String ID (útil para procesar datos del backend)
     */
    fun extractStringId(value: Any?): String? {
        return when (value) {
            is String -> if (value.isBlank() || value == "null") null else value
            is Int -> value.toString()
            is Long -> value.toString()
            is Number -> value.toString()
            else -> null
        }
    }

    /**
     * Formatea información de ruta (origen -> destino)
     */
    suspend fun formatRoute(
        origenCiudadId: String?,
        origenPaisId: String?,
        destinoCiudadId: String?,
        destinoPaisId: String?
    ): String {
        val origen = getOrigenDisplay(origenCiudadId, origenPaisId)
        val destino = getDestinoDisplay(destinoCiudadId, destinoPaisId)
        return "$origen → $destino"
    }

    /**
     * Convierte campo de origen_ciudad o destino_ciudad (que puede venir como string)
     */
    fun parseLocationString(location: String?): String {
        return if (location.isNullOrBlank() || location == "null") {
            "No especificado"
        } else {
            location
        }
    }

    /**
     * Convierte campo de origen_pais o destino_pais (que puede venir como string)
     */
    fun parseCountryString(country: String?): String {
        return if (country.isNullOrBlank() || country == "null") {
            "No especificado"
        } else {
            country
        }
    }
}