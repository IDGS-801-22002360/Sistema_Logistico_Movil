package com.example.crm_logistico_movil.models

import kotlinx.datetime.*

data class Operation(
    val id_operacion: String,
    val id_cotizacion: String? = null,
    val id_cliente: String,
    val id_usuario_operativo: String,
    val id_proveedor: String,
    val id_agente: String? = null,
    val tipo_servicio: TipoServicio,
    val tipo_carga: TipoCarga,
    val incoterm: Incoterm,
    val fecha_inicio_operacion: Instant = Clock.System.now(),
    val fecha_estimada_arribo: LocalDate? = null,
    val fecha_estimada_entrega: LocalDate? = null,
    val fecha_arribo_real: Instant? = null,
    val fecha_entrega_real: Instant? = null,
    val estatus: EstatusOperacion,
    val numero_referencia_proveedor: String? = null,
    val notas_operacion: String? = null,
    val fecha_creacion: Instant = Clock.System.now()
)

data class Cotizacion(
    val id_cotizacion: String,
    val id_cliente: String,
    val id_usuario_ventas: String,
    val id_usuario_operativo: String? = null,
    val id_origen_localizacion: String,
    val id_destino_localizacion: String,
    val id_proveedor: String,
    val id_agente: String? = null,
    val tipo_servicio: TipoServicio,
    val tipo_carga: TipoCarga,
    val incoterm: Incoterm,
    val fecha_solicitud: Instant = Clock.System.now(),
    val fecha_estimada_arribo: LocalDate? = null,
    val fecha_estimada_entrega: LocalDate? = null,
    val descripcion_mercancia: String? = null,
    val estatus: EstatusCotizacion = EstatusCotizacion.PENDIENTE,
    val motivo_rechazo: String? = null,
    val fecha_aprobacion_rechazo: Instant? = null,
    val fecha_creacion: Instant = Clock.System.now(),
    val id_solicitud_cliente: String? = null
)

data class SolicitudCotizacionCliente(
    val id_solicitud: String,
    val id_cliente: String,
    val tipo_servicio: TipoServicio,
    val tipo_carga: TipoCarga,
    val origen_ciudad: String,
    val origen_pais: String,
    val destino_ciudad: String,
    val destino_pais: String,
    val fecha_solicitud: Instant = Clock.System.now(),
    val descripcion_mercancia: String? = null,
    val valor_estimado_mercancia: Double? = null,
    val estatus: EstatusSolicitudCotizacion = EstatusSolicitudCotizacion.NUEVA
)

data class CotizacionCargo(
    val id_cargo_cotizacion: String,
    val id_cotizacion: String,
    val id_tarifa: String? = null,
    val descripcion_cargo: String,
    val monto: Double,
    val moneda: String,
    val aplica_proveedor: Boolean = false,
    val aplica_cliente: Boolean = true
)

data class Incidencia(
    val id_incidencia: String,
    val id_operacion: String,
    val fecha_hora_incidencia: Instant,
    val descripcion_incidencia: String,
    val tipo_incidencia: TipoIncidencia,
    val estatus: EstatusIncidencia = EstatusIncidencia.REPORTADA,
    val fecha_resolucion: Instant? = null,
    val comentarios_resolucion: String? = null,
    val fecha_registro: Instant = Clock.System.now()
)

data class Demora(
    val id_demora: String,
    val id_operacion: String,
    val fecha_hora_demora: Instant,
    val descripcion_demora: String? = null,
    val tipo_demora: TipoDemora,
    val costo_asociado: Double = 0.0,
    val moneda: String? = null,
    val fecha_registro: Instant = Clock.System.now()
)

data class Tracking(
    val id_tracking: String,
    val id_operacion: String,
    val fecha_hora_actualizacion: Instant = Clock.System.now(),
    val ubicacion_actual: String? = null,
    val estatus_seguimiento: EstatusSeguimiento,
    val referencia_transportista: String? = null,
    val nombre_transportista: String? = null,
    val notas_tracking: String? = null,
    val fecha_registro: Instant = Clock.System.now()
)


data class NotaCredito(
    val id_nota_credito: String,
    val id_factura_cliente: String,
    val numero_nota_credito: String,
    val fecha_emision: Instant = Clock.System.now(),
    val monto: Double,
    val moneda: String,
    val motivo: String? = null,
    val fecha_creacion: Instant = Clock.System.now()
)

data class DocumentoRelacionado(
    val id_documento: String,
    val id_entidad_relacionada: String,
    val tipo_entidad: TipoEntidadDocumento,
    val nombre_documento: String,
    val tipo_documento: TipoDocumento,
    val url_archivo: String,
    val fecha_carga: Instant = Clock.System.now(),
    val id_usuario_carga: String
)

// Enums
enum class TipoServicio {
    TERRESTRE, MARITIMO, AEREO, MULTIMODAL, ULTIMA_MILLA, DOMESTICO
}

enum class TipoCarga {
    FCL, LCL, FTL, LTL
}

enum class Incoterm {
    EXW, FCA, CPT, CIP, DAP, DPU, DDP, FAS, FOB, CFR, CIF
}

enum class EstatusOperacion {
    EN_TRANSITO, EN_ADUANA, ENTREGADO, CANCELADO, PENDIENTE_DOCUMENTOS
}

enum class EstatusCotizacion {
    PENDIENTE, APROBADA, RECHAZADA, CADUCADA, ENVIADA
}

enum class EstatusSolicitudCotizacion {
    NUEVA, EN_PROCESO, COTIZADA, RECHAZADA
}

enum class TipoIncidencia {
    DANO_MERCANCIA, EXTRAVIO_PARCIAL, EXTRAVIO_TOTAL, ROBO, ERROR_DOCUMENTACION, OTRO
}

enum class EstatusIncidencia {
    REPORTADA, EN_REVISION, RESUELTA, ESCALADA
}

enum class TipoDemora {
    CLIMATICA, ADUANA, MECANICA, DOCUMENTAL, TRAFICO, OTRO
}

enum class EstatusSeguimiento {
    EN_ORIGEN, EN_TRANSITO, EN_DESTINO, ENTREGADO, ADUANA
}

enum class EstatusFactura {
    PENDIENTE, PAGADA, VENCIDA, CANCELADA
}

enum class TipoEntidadDocumento {
    COTIZACION, OPERACION
}

enum class TipoDocumento {
    BL, AWB, CARTA_PORTE, PACKING_LIST, FACTURA_COMERCIAL, CERTIFICADO_ORIGEN, PEDIMENTO, OTRO
}
