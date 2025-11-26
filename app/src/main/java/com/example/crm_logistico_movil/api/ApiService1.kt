package com.example.crm_logistico_movil.api

import com.example.crm_logistico_movil.models.*
import com.example.crm_logistico_movil.models.FacturaClienteTmp
import retrofit2.Response
import retrofit2.http.*
import kotlin.jvm.JvmSuppressWildcards

interface ApiService {
    @POST("login-movil")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("usuario/{userId}")
    suspend fun getUsuario(@Path("userId") userId: String): Response<UserResponse>

    @POST("crear_solicitud")
    suspend fun crearSolicitud(@Body request: SolicitudRequest): Response<SolicitudResponse>

    // Client-specific endpoints (backend exposes these under /cliente/{client_id}/...)
    @POST("cliente/{client_id}/crear_solicitud")
    suspend fun crearSolicitudCliente(
        @Path("client_id") clientId: String,
        @Body request: SolicitudRequestBody
    ): Response<SolicitudResponse>

    @GET("cliente/{client_id}/operaciones")
    suspend fun getOperacionesCliente(
        @Path("client_id") clientId: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<OperacionesListResponse>

    @GET("cliente/{client_id}/cotizaciones")
    suspend fun getCotizacionesCliente(
        @Path("client_id") clientId: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<CotizacionesListResponse>

    @GET("cliente/{client_id}/solicitudes")
    suspend fun getSolicitudesCliente(
        @Path("client_id") clientId: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<SolicitudesListResponse>

    @GET("cliente/{client_id}/facturas")
    suspend fun getFacturasCliente(
        @Path("client_id") clientId: String
    ): Response<List<FacturaClienteTmp>>

    @GET("cliente/{client_id}/info")
    suspend fun getClienteInfo(
        @Path("client_id") clientId: String
    ): Response<com.example.crm_logistico_movil.models.ClienteInfoResponse>

    @PUT("cliente/{client_id}")
    suspend fun editarCliente(
        @Path("client_id") clientId: String,
        @Body body: Map<String, @JvmSuppressWildcards Any?>
    ): Response<com.example.crm_logistico_movil.models.EditClientResponse>

    @GET("tracking/{id_operacion}")
    suspend fun getTrackingPorOperacion(
        @Path("id_operacion") idOperacion: String
    ): Response<TrackingResponse>

    // Detail endpoints for individual operations and invoices
    @GET("operacion/{id_operacion}")
    suspend fun getOperacionDetalle(
        @Path("id_operacion") idOperacion: String
    ): Response<Map<String, @JvmSuppressWildcards Any>>

    @GET("factura/{id_factura}")
    suspend fun getFacturaDetalle(
        @Path("id_factura") idFactura: String
    ): Response<FacturaClienteTmp>

    @POST("call/{procName}")
    suspend fun callProcedure(
        @Path("procName") procName: String,
        @Body params: Map<String, @JvmSuppressWildcards Any?>
    ): Response<ProcedureResponse>

    // Reference data endpoints
    @GET("proveedores")
    suspend fun getProveedores(): Response<List<com.example.crm_logistico_movil.models.Proveedor>>

    @GET("agentes")
    suspend fun getAgentes(): Response<List<com.example.crm_logistico_movil.models.Agente>>

    @GET("paises")
    suspend fun getPaises(): Response<List<com.example.crm_logistico_movil.models.Pais>>

    @GET("localizaciones")
    suspend fun getLocalizaciones(): Response<List<com.example.crm_logistico_movil.models.Localizacion>>

    // Register client endpoint
    @POST("register_cliente")
    suspend fun registerCliente(
        @Body body: RegisterClientRequest
    ): Response<RegisterClientResponse>
}