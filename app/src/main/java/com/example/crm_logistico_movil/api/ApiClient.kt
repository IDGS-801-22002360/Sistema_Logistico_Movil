package com.example.crm_logistico_movil.api

import com.example.crm_logistico_movil.config.ApiConfig
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object ApiClient {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(ApiConfig.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
        .build()

    // Create a Gson instance that tolerates boolean fields represented as numbers (0/1) or strings
    private val gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, JsonDeserializer { json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?
            ->
            if (json == null || json.isJsonNull) {
                null
            } else {
                try {
                    when {
                        json.isJsonPrimitive && json.asJsonPrimitive.isBoolean -> json.asBoolean
                        json.isJsonPrimitive && json.asJsonPrimitive.isNumber -> json.asInt != 0
                        json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                            val s = json.asString.lowercase().trim()
                            s == "true" || s == "1" || s == "yes"
                        }
                        else -> false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        })
        // register for primitive boolean as well
        .registerTypeAdapter(Boolean::class.javaPrimitiveType, JsonDeserializer { json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?
            ->
            if (json == null || json.isJsonNull) {
                false
            } else {
                try {
                    when {
                        json.isJsonPrimitive && json.asJsonPrimitive.isBoolean -> json.asBoolean
                        json.isJsonPrimitive && json.asJsonPrimitive.isNumber -> json.asInt != 0
                        json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                            val s = json.asString.lowercase().trim()
                            s == "true" || s == "1" || s == "yes"
                        }
                        else -> false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        })
        // support parsing Instant fields returned as ISO string or as epoch milliseconds
        .registerTypeAdapter(Instant::class.java, JsonDeserializer { json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?
            ->
            if (json == null || json.isJsonNull) {
                null
            } else {
                try {
                    if (json.isJsonPrimitive && json.asJsonPrimitive.isNumber) {
                        // epoch millis
                        return@JsonDeserializer Instant.fromEpochMilliseconds(json.asLong)
                    }

                    if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
                        val s = json.asString.trim()
                        // try strict parse first
                        try {
                            return@JsonDeserializer Instant.parse(s)
                        } catch (_: Exception) {
                        }
                        // try appending Z (assume UTC) if missing timezone
                        try {
                            return@JsonDeserializer Instant.parse(s + "Z")
                        } catch (_: Exception) {
                        }
                        // try parse as LocalDateTime and assume UTC
                        try {
                            val ldt = LocalDateTime.parse(s)
                            return@JsonDeserializer ldt.toInstant(TimeZone.UTC)
                        } catch (e: Exception) {
                            throw JsonParseException("Failed to parse Instant from string '$s': ${e.message}", e)
                        }
                    }

                    throw JsonParseException("Cannot parse Instant from: $json")
                } catch (e: Exception) {
                    throw JsonParseException("Failed to parse Instant: ${e.message}", e)
                }
            }
        })
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // create the service from the local ApiService interface
    val apiService: ApiService = retrofit.create(ApiService::class.java)

}