USE Logistica;

DELIMITER $$

-- =====================================================
-- STORED PROCEDURES PARA FUNCIONALIDAD DEL CLIENTE
-- =====================================================

-- Obtener resumen del dashboard del cliente
DROP PROCEDURE IF EXISTS sp_obtener_resumen_cliente $$
CREATE PROCEDURE sp_obtener_resumen_cliente(
    IN p_id_cliente VARCHAR(50)
)
BEGIN
    SELECT 
        (SELECT COUNT(*) FROM OPERACIONES WHERE id_cliente = p_id_cliente AND estatus IN ('en_transito', 'en_aduana', 'pendiente_documentos')) as operaciones_activas,
        (SELECT COUNT(*) FROM FACTURAS_CLIENTE WHERE id_cliente = p_id_cliente AND estatus = 'pendiente') as facturas_pendientes,
        (SELECT COUNT(*) FROM COTIZACIONES WHERE id_cliente = p_id_cliente AND estatus = 'pendiente') as cotizaciones_pendientes,
        (SELECT COUNT(*) FROM SOLICITUDES_COTIZACION_CLIENTE WHERE id_cliente = p_id_cliente AND estatus IN ('nueva', 'en_proceso')) as solicitudes_pendientes;
END $$

-- Obtener operaciones del cliente con paginación
DROP PROCEDURE IF EXISTS sp_obtener_operaciones_cliente $$
CREATE PROCEDURE sp_obtener_operaciones_cliente(
    IN p_id_cliente VARCHAR(50),
    IN p_limit INT DEFAULT 10,
    IN p_offset INT DEFAULT 0
)
BEGIN
    SELECT 
        o.id_operacion,
        o.id_cotizacion,
        o.id_cliente,
        o.id_usuario_operativo,
        o.id_proveedor,
        o.id_agente,
        o.tipo_servicio,
        o.tipo_carga,
        o.incoterm,
        o.fecha_inicio_operacion,
        o.fecha_estimada_arribo,
        o.fecha_estimada_entrega,
        o.fecha_arribo_real,
        o.fecha_entrega_real,
        o.estatus,
        o.numero_referencia_proveedor,
        o.notas_operacion,
        o.fecha_creacion,
        p.nombre_empresa as proveedor_nombre,
        u.nombre as operativo_nombre,
        u.apellido as operativo_apellido
    FROM OPERACIONES o
    LEFT JOIN PROVEEDORES p ON o.id_proveedor = p.id_proveedor
    LEFT JOIN USUARIOS u ON o.id_usuario_operativo = u.id_usuario
    WHERE o.id_cliente = p_id_cliente
    ORDER BY o.fecha_creacion DESC
    LIMIT p_limit OFFSET p_offset;
END $$

-- Obtener cotizaciones del cliente
DROP PROCEDURE IF EXISTS sp_obtener_cotizaciones_cliente $$
CREATE PROCEDURE sp_obtener_cotizaciones_cliente(
    IN p_id_cliente VARCHAR(50),
    IN p_limit INT DEFAULT 10,
    IN p_offset INT DEFAULT 0
)
BEGIN
    SELECT 
        c.id_cotizacion,
        c.id_cliente,
        c.id_usuario_ventas,
        c.id_usuario_operativo,
        c.id_origen_localizacion,
        c.id_destino_localizacion,
        c.id_proveedor,
        c.id_agente,
        c.tipo_servicio,
        c.tipo_carga,
        c.incoterm,
        c.fecha_solicitud,
        c.fecha_estimada_arribo,
        c.fecha_estimada_entrega,
        c.descripcion_mercancia,
        c.estatus,
        c.motivo_rechazo,
        c.fecha_aprobacion_rechazo,
        c.fecha_creacion,
        c.id_solicitud_cliente,
        p.nombre_empresa as proveedor_nombre,
        uv.nombre as vendedor_nombre,
        uv.apellido as vendedor_apellido,
        lo.nombre_ciudad as origen_ciudad,
        ld.nombre_ciudad as destino_ciudad,
        po.nombre_pais as origen_pais,
        pd.nombre_pais as destino_pais
    FROM COTIZACIONES c
    LEFT JOIN PROVEEDORES p ON c.id_proveedor = p.id_proveedor
    LEFT JOIN USUARIOS uv ON c.id_usuario_ventas = uv.id_usuario
    LEFT JOIN LOCALIZACIONES lo ON c.id_origen_localizacion = lo.id_localizacion
    LEFT JOIN LOCALIZACIONES ld ON c.id_destino_localizacion = ld.id_localizacion
    LEFT JOIN PAISES po ON lo.id_pais = po.id_pais
    LEFT JOIN PAISES pd ON ld.id_pais = pd.id_pais
    WHERE c.id_cliente = p_id_cliente
    ORDER BY c.fecha_creacion DESC
    LIMIT p_limit OFFSET p_offset;
END $$

-- Obtener facturas del cliente
DROP PROCEDURE IF EXISTS sp_obtener_facturas_cliente $$
CREATE PROCEDURE sp_obtener_facturas_cliente(
    IN p_id_cliente VARCHAR(50),
    IN p_limit INT DEFAULT 10,
    IN p_offset INT DEFAULT 0
)
BEGIN
    SELECT 
        f.id_factura_cliente,
        f.id_cliente,
        f.id_operacion,
        f.id_cotizacion,
        f.numero_factura,
        f.fecha_emision,
        f.fecha_vencimiento,
        f.monto_total,
        f.monto_pagado,
        f.moneda,
        f.estatus,
        f.observaciones,
        f.fecha_pago,
        f.fecha_creacion,
        o.id_operacion as operacion_referencia,
        c.id_cotizacion as cotizacion_referencia
    FROM FACTURAS_CLIENTE f
    LEFT JOIN OPERACIONES o ON f.id_operacion = o.id_operacion
    LEFT JOIN COTIZACIONES c ON f.id_cotizacion = c.id_cotizacion
    WHERE f.id_cliente = p_id_cliente
    ORDER BY f.fecha_creacion DESC
    LIMIT p_limit OFFSET p_offset;
END $$

-- Obtener solicitudes de cotización del cliente
DROP PROCEDURE IF EXISTS sp_obtener_solicitudes_cotizacion_cliente $$
CREATE PROCEDURE sp_obtener_solicitudes_cotizacion_cliente(
    IN p_id_cliente VARCHAR(50),
    IN p_limit INT DEFAULT 10,
    IN p_offset INT DEFAULT 0
)
BEGIN
    SELECT 
        id_solicitud,
        id_cliente,
        tipo_servicio,
        tipo_carga,
        origen_ciudad,
        origen_pais,
        destino_ciudad,
        destino_pais,
        fecha_solicitud,
        descripcion_mercancia,
        valor_estimado_mercancia,
        estatus
    FROM SOLICITUDES_COTIZACION_CLIENTE
    WHERE id_cliente = p_id_cliente
    ORDER BY fecha_solicitud DESC
    LIMIT p_limit OFFSET p_offset;
END $$

-- Crear nueva solicitud de cotización
DROP PROCEDURE IF EXISTS sp_crear_solicitud_cotizacion $$
CREATE PROCEDURE sp_crear_solicitud_cotizacion(
    IN p_id_cliente VARCHAR(50),
    IN p_tipo_servicio ENUM('terrestre', 'maritimo', 'aereo', 'ultima_milla', 'domestico'),
    IN p_tipo_carga ENUM('FCL', 'LCL', 'FTL', 'LTL'),
    IN p_origen_ciudad VARCHAR(100),
    IN p_origen_pais VARCHAR(100),
    IN p_destino_ciudad VARCHAR(100),
    IN p_destino_pais VARCHAR(100),
    IN p_descripcion_mercancia TEXT,
    IN p_valor_estimado_mercancia DECIMAL(12, 2),
    OUT o_id_solicitud VARCHAR(50),
    OUT o_status VARCHAR(32),
    OUT o_message VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET o_status = 'ERROR';
        SET o_message = 'Error al crear la solicitud de cotización';
        SET o_id_solicitud = NULL;
    END;
    
    START TRANSACTION;
    
    -- Generar ID único
    SET o_id_solicitud = REPLACE(UUID(),'-','');
    
    -- Insertar solicitud
    INSERT INTO SOLICITUDES_COTIZACION_CLIENTE (
        id_solicitud,
        id_cliente,
        tipo_servicio,
        tipo_carga,
        origen_ciudad,
        origen_pais,
        destino_ciudad,
        destino_pais,
        fecha_solicitud,
        descripcion_mercancia,
        valor_estimado_mercancia,
        estatus
    ) VALUES (
        o_id_solicitud,
        p_id_cliente,
        p_tipo_servicio,
        p_tipo_carga,
        p_origen_ciudad,
        p_origen_pais,
        p_destino_ciudad,
        p_destino_pais,
        NOW(),
        p_descripcion_mercancia,
        p_valor_estimado_mercancia,
        'nueva'
    );
    
    COMMIT;
    
    SET o_status = 'OK';
    SET o_message = 'Solicitud de cotización creada exitosamente';
    
    -- Devolver la solicitud creada
    SELECT 
        id_solicitud,
        id_cliente,
        tipo_servicio,
        tipo_carga,
        origen_ciudad,
        origen_pais,
        destino_ciudad,
        destino_pais,
        fecha_solicitud,
        descripcion_mercancia,
        valor_estimado_mercancia,
        estatus
    FROM SOLICITUDES_COTIZACION_CLIENTE
    WHERE id_solicitud = o_id_solicitud;
END $$

-- Obtener detalle de operación
DROP PROCEDURE IF EXISTS sp_obtener_detalle_operacion $$
CREATE PROCEDURE sp_obtener_detalle_operacion(
    IN p_id_operacion VARCHAR(50)
)
BEGIN
    -- Datos básicos de la operación
    SELECT 
        o.id_operacion,
        o.id_cotizacion,
        o.id_cliente,
        o.id_usuario_operativo,
        o.id_proveedor,
        o.id_agente,
        o.tipo_servicio,
        o.tipo_carga,
        o.incoterm,
        o.fecha_inicio_operacion,
        o.fecha_estimada_arribo,
        o.fecha_estimada_entrega,
        o.fecha_arribo_real,
        o.fecha_entrega_real,
        o.estatus,
        o.numero_referencia_proveedor,
        o.notas_operacion,
        o.fecha_creacion,
        p.nombre_empresa as proveedor_nombre,
        p.telefono as proveedor_telefono,
        p.email_contacto as proveedor_email,
        u.nombre as operativo_nombre,
        u.apellido as operativo_apellido,
        u.email as operativo_email,
        a.nombre as agente_nombre,
        a.apellido as agente_apellido,
        a.email as agente_email,
        a.tipo_agente
    FROM OPERACIONES o
    LEFT JOIN PROVEEDORES p ON o.id_proveedor = p.id_proveedor
    LEFT JOIN USUARIOS u ON o.id_usuario_operativo = u.id_usuario
    LEFT JOIN AGENTES a ON o.id_agente = a.id_agente
    WHERE o.id_operacion = p_id_operacion;
    
    -- Tracking de la operación
    SELECT 
        id_tracking,
        fecha_hora_actualizacion,
        ubicacion_actual,
        estatus_seguimiento,
        referencia_transportista,
        nombre_transportista,
        notas_tracking
    FROM TRACKING
    WHERE id_operacion = p_id_operacion
    ORDER BY fecha_hora_actualizacion DESC;
    
    -- Incidencias de la operación
    SELECT 
        id_incidencia,
        fecha_hora_incidencia,
        descripcion_incidencia,
        tipo_incidencia,
        estatus,
        fecha_resolucion,
        comentarios_resolucion
    FROM INCIDENCIAS
    WHERE id_operacion = p_id_operacion
    ORDER BY fecha_hora_incidencia DESC;
    
    -- Demoras de la operación
    SELECT 
        id_demora,
        fecha_hora_demora,
        descripcion_demora,
        tipo_demora,
        costo_asociado,
        moneda
    FROM DEMORAS
    WHERE id_operacion = p_id_operacion
    ORDER BY fecha_hora_demora DESC;
END $$

-- Obtener detalle de factura
DROP PROCEDURE IF EXISTS sp_obtener_detalle_factura $$
CREATE PROCEDURE sp_obtener_detalle_factura(
    IN p_id_factura VARCHAR(50)
)
BEGIN
    -- Datos básicos de la factura
    SELECT 
        f.id_factura_cliente,
        f.id_cliente,
        f.id_operacion,
        f.id_cotizacion,
        f.numero_factura,
        f.fecha_emision,
        f.fecha_vencimiento,
        f.monto_total,
        f.monto_pagado,
        f.moneda,
        f.estatus,
        f.observaciones,
        f.fecha_pago,
        f.fecha_creacion,
        o.numero_referencia_proveedor as operacion_referencia,
        c.id_cotizacion as cotizacion_referencia
    FROM FACTURAS_CLIENTE f
    LEFT JOIN OPERACIONES o ON f.id_operacion = o.id_operacion
    LEFT JOIN COTIZACIONES c ON f.id_cotizacion = c.id_cotizacion
    WHERE f.id_factura_cliente = p_id_factura;
    
    -- Notas de crédito asociadas
    SELECT 
        id_nota_credito,
        numero_nota_credito,
        fecha_emision,
        monto,
        moneda,
        motivo
    FROM NOTAS_CREDITO
    WHERE id_factura_cliente = p_id_factura
    ORDER BY fecha_emision DESC;
END $$

-- Obtener países disponibles
DROP PROCEDURE IF EXISTS sp_obtener_paises $$
CREATE PROCEDURE sp_obtener_paises()
BEGIN
    SELECT 
        id_pais,
        nombre_pais,
        codigo_iso2
    FROM PAISES
    ORDER BY nombre_pais;
END $$

-- Obtener ciudades por país
DROP PROCEDURE IF EXISTS sp_obtener_ciudades_por_pais $$
CREATE PROCEDURE sp_obtener_ciudades_por_pais(
    IN p_id_pais VARCHAR(50)
)
BEGIN
    SELECT 
        id_localizacion,
        nombre_ciudad,
        tipo_ubicacion,
        codigo_iata_icao,
        direccion
    FROM LOCALIZACIONES
    WHERE id_pais = p_id_pais
    ORDER BY nombre_ciudad;
END $$

DELIMITER ;
