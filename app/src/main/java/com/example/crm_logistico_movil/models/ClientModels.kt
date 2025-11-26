package com.example.crm_logistico_movil.models

// Modelos de respuesta extendidos para incluir información adicional
data class ClientSummary(
    val operacionesActivas: Int,
    val facturasPendientes: Int,
    val cotizacionesPendientes: Int,
    val solicitudesPendientes: Int
)

data class OperationExtended(
    val id_operacion: String,
    val id_cotizacion: String? = null,
    val id_cliente: String,
    val id_usuario_operativo: String,
    val id_proveedor: String,
    val id_agente: String? = null,
    val tipo_servicio: String,
    val tipo_carga: String,
    val incoterm: String,
    val fecha_inicio_operacion: String,
    val fecha_estimada_arribo: String? = null,
    val fecha_estimada_entrega: String? = null,
    val fecha_arribo_real: String? = null,
    val fecha_entrega_real: String? = null,
    val estatus: String,
    val numero_referencia_proveedor: String? = null,
    val notas_operacion: String? = null,
    val fecha_creacion: String,
    // Campos adicionales de JOIN
    val proveedorNombre: String? = null,
    val operativoNombre: String? = null,
    val operativoApellido: String? = null
)

data class CotizacionExtended(
    val id_cotizacion: String,
    val id_cliente: String,
    val id_usuario_ventas: String,
    val id_usuario_operativo: String? = null,
    val id_origen_localizacion: String,
    val id_destino_localizacion: String,
    val id_proveedor: String,
    val id_agente: String? = null,
    val tipo_servicio: String,
    val tipo_carga: String,
    val incoterm: String,
    val fecha_solicitud: String,
    val fecha_estimada_arribo: String? = null,
    val fecha_estimada_entrega: String? = null,
    val descripcion_mercancia: String? = null,
    val estatus: String,
    val motivo_rechazo: String? = null,
    val fecha_aprobacion_rechazo: String? = null,
    val fecha_creacion: String,
    val id_solicitud_cliente: String? = null,
    // Campos adicionales de JOIN
    val proveedorNombre: String? = null,
    val vendedorNombre: String? = null,
    val vendedorApellido: String? = null,
    val origenCiudad: String? = null,
    val destinoCiudad: String? = null,
    val origenPais: String? = null,
    val destinoPais: String? = null
)

data class FacturaExtended(
    val id_factura_cliente: String,
    val id_cliente: String,
    val id_operacion: String? = null,
    val id_cotizacion: String? = null,
    val numero_factura: String,
    val fecha_emision: String,
    val fecha_vencimiento: String,
    val monto_total: Double,
    val monto_pagado: Double = 0.0,
    val moneda: String,
    val estatus: String,
    val observaciones: String? = null,
    val fecha_pago: String? = null,
    val fecha_creacion: String,
    // Campos adicionales de JOIN
    val operacionReferencia: String? = null,
    val cotizacionReferencia: String? = null
)

data class SolicitudCotizacionExtended(
    val id_solicitud: String,
    val id_cliente: String,
    val tipo_servicio: String,
    val tipo_carga: String,
    val origen_ciudad: String,
    val origen_pais: String,
    val destino_ciudad: String,
    val destino_pais: String,
    val fecha_solicitud: String,
    val descripcion_mercancia: String? = null,
    val valor_estimado_mercancia: Double? = null,
    val estatus: String
)

data class TrackingInfo(
    val id_tracking: String,
    val fecha_hora_actualizacion: String,
    val ubicacion_actual: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val estatus_seguimiento: String,
    val referencia_transportista: String? = null,
    val nombre_transportista: String? = null,
    val notas_tracking: String? = null
)

data class IncidenciaInfo(
    val id_incidencia: String,
    val fecha_hora_incidencia: String,
    val descripcion_incidencia: String,
    val tipo_incidencia: String,
    val estatus: String,
    val fecha_resolucion: String? = null,
    val comentarios_resolucion: String? = null
)

data class DemoraInfo(
    val id_demora: String,
    val fecha_hora_demora: String,
    val descripcion_demora: String? = null,
    val tipo_demora: String,
    val costo_asociado: Double = 0.0,
    val moneda: String? = null
)

data class OperationDetail(
    val operacion: OperationExtended,
    val tracking: List<TrackingInfo>,
    val incidencias: List<IncidenciaInfo>,
    val demoras: List<DemoraInfo>
)

data class NotaCreditoInfo(
    val id_nota_credito: String,
    val numero_nota_credito: String,
    val fecha_emision: String,
    val monto: Double,
    val moneda: String,
    val motivo: String? = null
)

data class FacturaDetail(
    val factura: FacturaExtended,
    val notasCredito: List<NotaCreditoInfo>
)


// Request para nueva solicitud de cotización
data class NuevaSolicitudCotizacion(
    val tipo_servicio: String,
    val tipo_carga: String,
    val origen_ciudad: String,
    val origen_pais: String,
    val destino_ciudad: String,
    val destino_pais: String,
    val descripcion_mercancia: String,
    val valor_estimado_mercancia: Double?
)
