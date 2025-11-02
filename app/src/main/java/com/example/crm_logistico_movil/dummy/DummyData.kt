package com.example.crm_logistico_movil.dummy

import com.example.crm_logistico_movil.models.*
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.days

object DummyData {
    val dummyUser = User(
        id_usuario = "USR001",
        nombre = "Juan",
        apellido = "Pérez",
        email = "juan.perez@example.com",
        rol = "cliente",
        activo = true,
        fecha_creacion = Clock.System.now() - 30.days
    )

    val dummyClient = Client(
        id_cliente = "CLI001",
        nombre_empresa = "Transportes Rápidos S.A. de C.V.",
        rfc = "TRA901231ABC",
        direccion = "Av. Siempre Viva 123",
        ciudad = "Ciudad de México",
        pais = "México",
        telefono = "5512345678",
        email_contacto = "contacto@transportesrapidos.com",
        contacto_nombre = "Ana García",
        contacto_puesto = "Gerente de Logística",
        fecha_creacion = Clock.System.now() - 60.days
    )

    val dummyOperation = Operation(
        id_operacion = "OP001",
        id_cliente = dummyClient.id_cliente,
        id_usuario_operativo = "USR002",
        id_proveedor = "PROV001",
        tipo_servicio = TipoServicio.MARITIMO,
        tipo_carga = TipoCarga.FCL,
        incoterm = Incoterm.FOB,
        fecha_inicio_operacion = Clock.System.now() - 10.days,
        fecha_estimada_arribo = (Clock.System.now() + 15.days).toLocalDateTime(TimeZone.currentSystemDefault()).date,
        fecha_estimada_entrega = (Clock.System.now() + 17.days).toLocalDateTime(TimeZone.currentSystemDefault()).date,
        estatus = EstatusOperacion.EN_TRANSITO,
        numero_referencia_proveedor = "REF456789",
        notas_operacion = "Carga delicada, requiere monitoreo constante."
    )

    val dummyOperationsList = listOf(
        dummyOperation,
        dummyOperation.copy(
            id_operacion = "OP002",
            estatus = EstatusOperacion.ENTREGADO,
            fecha_entrega_real = Clock.System.now() - 5.days,
            tipo_servicio = TipoServicio.AEREO
        ),
        dummyOperation.copy(
            id_operacion = "OP003",
            estatus = EstatusOperacion.PENDIENTE_DOCUMENTOS,
            tipo_servicio = TipoServicio.TERRESTRE
        )
    )
    // Agrega más datos de ejemplo para otras entidades
}