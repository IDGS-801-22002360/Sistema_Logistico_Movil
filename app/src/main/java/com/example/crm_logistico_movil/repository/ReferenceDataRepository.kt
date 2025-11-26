package com.example.crm_logistico_movil.repository

import android.util.Log
import com.example.crm_logistico_movil.api.ApiClient
import com.example.crm_logistico_movil.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository para manejar datos de referencia (proveedores, países, localizaciones, agentes)
 * con caché en memoria para evitar llamadas repetitivas a la API
 */
object ReferenceDataRepository {

    private val apiService = ApiClient.apiService

    // Caché en memoria
    private val proveedoresCache: MutableMap<String, Proveedor> = ConcurrentHashMap()
    private val paisesCache: MutableMap<String, Pais> = ConcurrentHashMap()
    private val localizacionesCache: MutableMap<String, Localizacion> = ConcurrentHashMap()
    private val agentesCache: MutableMap<String, Agente> = ConcurrentHashMap()

    // Control de estado de carga
    private var proveedoresLoaded = false
    private var paisesLoaded = false
    private var localizacionesLoaded = false
    private var agentesLoaded = false

    /**
     * Carga todos los datos de referencia
     */
    suspend fun loadAllReferenceData() {
        withContext(Dispatchers.IO) {
            try {
                // Cargar en paralelo para mejor performance
                val paisesJob = async { loadPaises() }
                val localizacionesJob = async { loadLocalizaciones() }
                val proveedoresJob = async { loadProveedores() }
                val agentesJob = async { loadAgentes() }

                // Esperar a que todas las cargas terminen
                paisesJob.await()
                localizacionesJob.await()
                proveedoresJob.await()
                agentesJob.await()

                Log.d("ReferenceDataRepository", "All reference data loaded successfully")
            } catch (e: Exception) {
                Log.e("ReferenceDataRepository", "Error loading reference data: ${e.message}")
            }
        }
    }

    /**
     * Obtiene un proveedor por ID
     */
    suspend fun getProveedorById(id: String): Proveedor? {
        if (!proveedoresLoaded) {
            loadProveedores()
        }
        return proveedoresCache[id]
    }

    /**
     * Obtiene un país por ID
     */
    suspend fun getPaisById(id: String): Pais? {
        if (!paisesLoaded) {
            loadPaises()
        }
        return paisesCache[id]
    }

    /**
     * Obtiene una localización por ID
     */
    suspend fun getLocalizacionById(id: String): Localizacion? {
        if (!localizacionesLoaded) {
            loadLocalizaciones()
        }
        return localizacionesCache[id]
    }

    /**
     * Obtiene un agente por ID
     */
    suspend fun getAgenteById(id: String): Agente? {
        if (!agentesLoaded) {
            loadAgentes()
        }
        return agentesCache[id]
    }

    /**
     * Obtiene el nombre de un proveedor por ID
     */
    suspend fun getProveedorName(id: String): String {
        val proveedor = getProveedorById(id)
        return proveedor?.nombre_empresa ?: "Proveedor #$id"
    }

    /**
     * Obtiene el nombre de un país por ID
     */
    suspend fun getPaisName(id: String): String {
        val pais = getPaisById(id)
        return pais?.nombre_pais ?: "País #$id"
    }

    /**
     * Obtiene el nombre de una ciudad por ID de localización
     */
    suspend fun getCiudadName(id: String): String {
        val localizacion = getLocalizacionById(id)
        return localizacion?.nombre_ciudad ?: "Ciudad #$id"
    }

    /**
     * Obtiene el nombre completo de un agente por ID
     */
    suspend fun getAgenteName(id: String): String {
        val agente = getAgenteById(id)
        return if (agente != null) {
            "${agente.nombre} ${agente.apellido}"
        } else {
            "Agente #$id"
        }
    }

    /**
     * Obtiene información completa de origen (ciudad, país)
     */
    suspend fun getOrigenCompleto(ciudadId: String, paisId: String): String {
        val ciudad = getCiudadName(ciudadId)
        val pais = getPaisName(paisId)
        return "$ciudad, $pais"
    }

    /**
     * Obtiene información completa de destino (ciudad, país)
     */
    suspend fun getDestinoCompleto(ciudadId: String, paisId: String): String {
        val ciudad = getCiudadName(ciudadId)
        val pais = getPaisName(paisId)
        return "$ciudad, $pais"
    }

    // Métodos privados para cargar datos

    private suspend fun loadProveedores() {
        try {
            val response = apiService.getProveedores()
            if (response.isSuccessful) {
                response.body()?.forEach { proveedor: Proveedor ->
                    proveedoresCache[proveedor.id_proveedor] = proveedor
                }
                proveedoresLoaded = true
                Log.d("ReferenceDataRepository", "Proveedores loaded: ${proveedoresCache.size}")
            } else {
                Log.e("ReferenceDataRepository", "Error loading proveedores: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("ReferenceDataRepository", "Exception loading proveedores: ${e.message}")
        }
    }

    private suspend fun loadPaises() {
        try {
            val response = apiService.getPaises()
            if (response.isSuccessful) {
                response.body()?.forEach { pais: Pais ->
                    paisesCache[pais.id_pais.toString()] = pais
                }
                paisesLoaded = true
                Log.d("ReferenceDataRepository", "Países loaded: ${paisesCache.size}")
            } else {
                Log.e("ReferenceDataRepository", "Error loading países: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("ReferenceDataRepository", "Exception loading países: ${e.message}")
        }
    }

    private suspend fun loadLocalizaciones() {
        try {
            val response = apiService.getLocalizaciones()
            if (response.isSuccessful) {
                response.body()?.forEach { localizacion: Localizacion ->
                    localizacionesCache[localizacion.id_localizacion] = localizacion
                }
                localizacionesLoaded = true
                Log.d("ReferenceDataRepository", "Localizaciones loaded: ${localizacionesCache.size}")
            } else {
                Log.e("ReferenceDataRepository", "Error loading localizaciones: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("ReferenceDataRepository", "Exception loading localizaciones: ${e.message}")
        }
    }

    private suspend fun loadAgentes() {
        try {
            val response = apiService.getAgentes()
            if (response.isSuccessful) {
                response.body()?.forEach { agente: Agente ->
                    agentesCache[agente.id_agente] = agente
                }
                agentesLoaded = true
                Log.d("ReferenceDataRepository", "Agentes loaded: ${agentesCache.size}")
            } else {
                Log.e("ReferenceDataRepository", "Error loading agentes: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("ReferenceDataRepository", "Exception loading agentes: ${e.message}")
        }
    }

    /**
     * Limpia todo el caché (útil para refrescar datos)
     */
    fun clearCache() {
        proveedoresCache.clear()
        paisesCache.clear()
        localizacionesCache.clear()
        agentesCache.clear()

        proveedoresLoaded = false
        paisesLoaded = false
        localizacionesLoaded = false
        agentesLoaded = false

        Log.d("ReferenceDataRepository", "Cache cleared")
    }
}