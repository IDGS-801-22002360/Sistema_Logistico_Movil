package com.example.crm_logistico_movil.models

// Facturas Models - Archivo temporal para resolver problemas de compilación
data class FacturaClienteTmp(
    val id_factura_cliente: String,
    val id_cliente: String,
    val id_operacion: String? = null,
    val id_cotizacion: String? = null,
    val numero_factura: String,
    val fecha_emision: String, // ISO format string for datetime
    val fecha_vencimiento: String, // ISO format string for date
    val monto_total: Double,
    val monto_pagado: Double,
    val moneda: String,
    val estatus: String,
    val observaciones: String? = null,
    val fecha_creacion: String, // ISO format string for datetime

    // Información adicional útil de las tablas unidas
    val cotizacion_tipo_servicio: String? = null,
    val cotizacion_tipo_carga: String? = null,
    val cotizacion_incoterm: String? = null,
    val descripcion_mercancia: String? = null,
    val operacion_tipo_servicio: String? = null,
    val fecha_inicio_operacion: String? = null,
    val fecha_estimada_entrega: String? = null
)

data class FacturasListResponseTmp(
    val facturas: List<FacturaClienteTmp>? = null
)