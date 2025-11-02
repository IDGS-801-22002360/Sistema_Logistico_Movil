package com.example.crm_logistico_movil.api

import com.example.crm_logistico_movil.config.ApiConfig
import com.google.firebase.appdistribution.gradle.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}