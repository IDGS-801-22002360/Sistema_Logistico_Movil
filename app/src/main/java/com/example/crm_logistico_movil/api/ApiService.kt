package com.example.crm_logistico_movil.api

import com.example.crm_logistico_movil.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("register_cliente")
    suspend fun registerCliente(@Body request: RegisterClienteRequest): Response<RegisterResponse>
    
    @GET("usuario/{userId}")
    suspend fun getUsuario(@Path("userId") userId: String): Response<UserResponse>
    
    @POST("crear_solicitud")
    suspend fun crearSolicitud(@Body request: SolicitudRequest): Response<SolicitudResponse>
    
    @POST("call/{procName}")
    suspend fun callProcedure(
        @Path("procName") procName: String,
        @Body params: Map<String, Any>
    ): Response<ProcedureResponse>
}