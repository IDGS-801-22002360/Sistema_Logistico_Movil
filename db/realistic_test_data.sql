-- Datos de prueba realistas para el CRM Logístico
USE Logistica;

-- Insertar datos de prueba solo si no existen ya
-- Verificar si ya hay datos
SET @data_exists = (SELECT COUNT(*) FROM USUARIOS WHERE email = 'cliente.prueba@logistica.com');

-- Solo insertar si no hay datos de prueba
IF @data_exists = 0 THEN

-- =====================================================
-- USUARIOS DE PRUEBA
-- =====================================================

-- Cliente de prueba
INSERT INTO USUARIOS (id_usuario, nombre, apellido, email, password, rol, activo, fecha_creacion) VALUES
('CLI001USER', 'Carlos Eduardo', 'Hernández López', 'cliente.prueba@logistica.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'cliente', TRUE, DATE_SUB(NOW(), INTERVAL 90 DAY));

-- Usuario de ventas  
INSERT INTO USUARIOS (id_usuario, nombre, apellido, email, password, rol, activo, fecha_creacion) VALUES
('VTA001USER', 'Ana María', 'Rodríguez Sánchez', 'ventas.ana@logistica.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ventas', TRUE, DATE_SUB(NOW(), INTERVAL 180 DAY));

-- Usuario operativo
INSERT INTO USUARIOS (id_usuario, nombre, apellido, email, password, rol, activo, fecha_creacion) VALUES
('OPE001USER', 'Miguel Ángel', 'Torres Vargas', 'operaciones.miguel@logistica.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'operaciones', TRUE, DATE_SUB(NOW(), INTERVAL 150 DAY));

-- =====================================================
-- CLIENTE DE PRUEBA
-- =====================================================

INSERT INTO CLIENTES (id_cliente, nombre_empresa, rfc, direccion, ciudad, pais, telefono, email_contacto, contacto_nombre, contacto_puesto, fecha_creacion) VALUES
('CLI001USER', 'Comercializadora Internacional Azteca SA de CV', 'CIA950621ABC', 'Av. Insurgentes Sur 1234, Col. Del Valle', 'Ciudad de México', 'México', '+52-55-1234-5678', 'cliente.prueba@logistica.com', 'Carlos Eduardo Hernández López', 'Director General', DATE_SUB(NOW(), INTERVAL 90 DAY));

-- =====================================================
-- PROVEEDORES DE PRUEBA
-- =====================================================

INSERT INTO PROVEEDORES (id_proveedor, nombre_empresa, rfc, direccion, ciudad, pais, telefono, email_contacto, contacto_nombre, tipo_servicio_ofrecido, fecha_creacion) VALUES
('PROV001', 'Naviera del Pacífico SA', 'NDP850315XYZ', 'Puerto de Manzanillo, Muelle 7', 'Manzanillo', 'México', '+52-314-987-6543', 'operaciones@naviera.com', 'Roberto Martínez', 'maritimo', DATE_SUB(NOW(), INTERVAL 200 DAY)),
('PROV002', 'Transportes Terrestres Unidos', 'TTU901205ABC', 'Carretera Federal 57, Km 45', 'Querétaro', 'México', '+52-442-123-4567', 'logistics@ttu.com', 'Elena González', 'terrestre', DATE_SUB(NOW(), INTERVAL 180 DAY)),
('PROV003', 'AeroLogística Express', 'ALE880910DEF', 'Aeropuerto Internacional CDMX, Terminal Carga', 'Ciudad de México', 'México', '+52-55-9876-5432', 'cargo@aeroex.com', 'Luis Fernando Silva', 'aereo', DATE_SUB(NOW(), INTERVAL 220 DAY));

-- =====================================================
-- AGENTES DE PRUEBA
-- =====================================================

INSERT INTO AGENTES (id_agente, nombre, apellido, email, telefono, tipo_agente, fecha_creacion) VALUES
('AGE001', 'Patricia Elena', 'López Morales', 'patricia.lopez@aduanas.com', '+52-55-2345-6789', 'aduanal', DATE_SUB(NOW(), INTERVAL 160 DAY)),
('AGE002', 'Fernando', 'Castillo Ruiz', 'fernando.castillo@carga.com', '+52-55-3456-7890', 'carga', DATE_SUB(NOW(), INTERVAL 140 DAY)),
('AGE003', 'Gabriela', 'Mendoza Torres', 'gabriela.mendoza@seguros.com', '+52-55-4567-8901', 'seguros', DATE_SUB(NOW(), INTERVAL 170 DAY));

-- =====================================================
-- OBTENER IDs DE PAÍSES Y LOCALIZACIONES
-- =====================================================

SET @mexico_id = (SELECT id_pais FROM PAISES WHERE codigo_iso2 = 'MX' LIMIT 1);
SET @usa_id = (SELECT id_pais FROM PAISES WHERE codigo_iso2 = 'US' LIMIT 1);

-- Si no existen los países, los creamos
IF @mexico_id IS NULL THEN
    INSERT INTO PAISES (id_pais, nombre_pais, codigo_iso2) VALUES
    ('MEX001', 'México', 'MX');
    SET @mexico_id = 'MEX001';
END IF;

IF @usa_id IS NULL THEN
    INSERT INTO PAISES (id_pais, nombre_pais, codigo_iso2) VALUES
    ('USA001', 'Estados Unidos', 'US');
    SET @usa_id = 'USA001';
END IF;

-- Localizaciones si no existen
SET @existing_locations = (SELECT COUNT(*) FROM LOCALIZACIONES);
IF @existing_locations = 0 THEN
    INSERT INTO LOCALIZACIONES (id_localizacion, id_pais, nombre_ciudad, tipo_ubicacion, codigo_iata_icao, direccion) VALUES
    ('LOC001', @mexico_id, 'Ciudad de México', 'aeropuerto', 'MEX', 'Aeropuerto Internacional Benito Juárez'),
    ('LOC002', @mexico_id, 'Veracruz', 'puerto', 'VER', 'Puerto de Veracruz'),
    ('LOC003', @mexico_id, 'Manzanillo', 'puerto', 'ZLO', 'Puerto de Manzanillo'),
    ('LOC004', @usa_id, 'Los Angeles', 'puerto', 'LAX', 'Puerto de Los Angeles'),
    ('LOC005', @usa_id, 'Miami', 'aeropuerto', 'MIA', 'Aeropuerto Internacional de Miami'),
    ('LOC006', @mexico_id, 'Tijuana', 'centro_distribucion', 'TIJ', 'Centro de Distribución Tijuana');
END IF;

-- =====================================================
-- COTIZACIONES DE PRUEBA
-- =====================================================

INSERT INTO COTIZACIONES (id_cotizacion, id_cliente, id_usuario_ventas, id_usuario_operativo, id_origen_localizacion, id_destino_localizacion, id_proveedor, id_agente, tipo_servicio, tipo_carga, incoterm, fecha_solicitud, fecha_estimada_arribo, fecha_estimada_entrega, descripcion_mercancia, estatus, fecha_creacion) VALUES
('COT001', 'CLI001USER', 'VTA001USER', 'OPE001USER', 'LOC002', 'LOC004', 'PROV001', 'AGE001', 'maritimo', 'FCL', 'FOB', DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 'Electrónicos - 20 contenedores 40ft con productos electrónicos diversos', 'aprobada', DATE_SUB(NOW(), INTERVAL 45 DAY)),

('COT002', 'CLI001USER', 'VTA001USER', 'OPE001USER', 'LOC001', 'LOC005', 'PROV003', 'AGE002', 'aereo', 'LTL', 'DAP', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 'Textiles urgentes - 500kg de ropa para temporada navideña', 'aprobada', DATE_SUB(NOW(), INTERVAL 30 DAY)),

('COT003', 'CLI001USER', 'VTA001USER', NULL, 'LOC003', 'LOC004', 'PROV001', 'AGE001', 'maritimo', 'LCL', 'CIF', DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, NULL, 'Autopartes - Componentes automotrices varios, 15 pallets', 'pendiente', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- =====================================================
-- OPERACIONES DE PRUEBA
-- =====================================================

INSERT INTO OPERACIONES (id_operacion, id_cotizacion, id_cliente, id_usuario_operativo, id_proveedor, id_agente, tipo_servicio, tipo_carga, incoterm, fecha_inicio_operacion, fecha_estimada_arribo, fecha_estimada_entrega, estatus, numero_referencia_proveedor, notas_operacion, fecha_creacion) VALUES
('OP001', 'COT001', 'CLI001USER', 'OPE001USER', 'PROV001', 'AGE001', 'maritimo', 'FCL', 'FOB', DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 'en_transito', 'NAV-2024-001234', 'Embarque en ruta. ETA confirmado. Mercancía asegurada por $2,500,000 USD', DATE_SUB(NOW(), INTERVAL 25 DAY)),

('OP002', 'COT002', 'CLI001USER', 'OPE001USER', 'PROV003', 'AGE002', 'aereo', 'LTL', 'DAP', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'entregado', 'AERO-2024-005678', 'Entrega completada sin incidencias. Cliente satisfecho.', DATE_SUB(NOW(), INTERVAL 15 DAY)),

('OP003', NULL, 'CLI001USER', 'OPE001USER', 'PROV002', NULL, 'terrestre', 'FTL', 'EXW', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 'en_aduana', 'TER-2024-009012', 'Documentación en revisión aduanal. Esperando liberación.', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- =====================================================
-- TRACKING DE PRUEBA
-- =====================================================

INSERT INTO TRACKING (id_tracking, id_operacion, fecha_hora_actualizacion, ubicacion_actual, estatus_seguimiento, referencia_transportista, nombre_transportista, notas_tracking, fecha_registro) VALUES
-- Tracking para OP001 (en_transito)
('TRK001', 'OP001', DATE_SUB(NOW(), INTERVAL 2 HOUR), 'Océano Pacífico - 800 millas náuticas de LA', 'en_transito', 'MSC-CALIFORNIA-2024', 'MSC California', 'Navegando en condiciones normales. Viento favorable.', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('TRK002', 'OP001', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Puerto de Manzanillo - Terminal especializada', 'en_origen', 'MSC-CALIFORNIA-2024', 'MSC California', 'Carga completada. Zarpe programado.', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- Tracking para OP002 (entregado)
('TRK003', 'OP002', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Centro de distribución Miami', 'entregado', 'AE-4521', 'American Eagle Flight 4521', 'Entrega completada. Documentos firmados por receptor.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TRK004', 'OP002', DATE_SUB(NOW(), INTERVAL 2 DAY), 'Aeropuerto Internacional Miami', 'en_destino', 'AE-4521', 'American Eagle Flight 4521', 'Descarga en curso. Preparando documentación.', DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- Tracking para OP003 (en_aduana)
('TRK005', 'OP003', DATE_SUB(NOW(), INTERVAL 6 HOUR), 'Aduana Nuevo Laredo', 'aduana', 'TRU-567890', 'Transportes Unidos 890', 'En proceso de revisión documental. Módulo rojo.', DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- =====================================================
-- FACTURAS DE PRUEBA
-- =====================================================

INSERT INTO FACTURAS_CLIENTE (id_factura_cliente, id_cliente, id_operacion, numero_factura, fecha_emision, fecha_vencimiento, monto_total, monto_pagado, moneda, estatus, observaciones, fecha_creacion) VALUES
('FAC001', 'CLI001USER', 'OP002', 'FAC-2024-001', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), 15750.00, 15750.00, 'USD', 'pagada', 'Pago recibido vía transferencia bancaria. Servicio aéreo express completado.', DATE_SUB(NOW(), INTERVAL 10 DAY)),

('FAC002', 'CLI001USER', 'OP001', 'FAC-2024-002', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY), 45500.00, 0.00, 'USD', 'pendiente', 'Servicio marítimo FCL. Pendiente de arribo para facturación final.', DATE_SUB(NOW(), INTERVAL 5 DAY)),

('FAC003', 'CLI001USER', 'OP003', 'FAC-2024-003', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 28 DAY), 8250.00, 0.00, 'USD', 'pendiente', 'Servicio terrestre. Facturación anticipada 80%. Saldo al finalizar.', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- =====================================================
-- SOLICITUDES DE COTIZACIÓN PENDIENTES
-- =====================================================

INSERT INTO SOLICITUDES_COTIZACION_CLIENTE (id_solicitud, id_cliente, tipo_servicio, tipo_carga, origen_ciudad, origen_pais, destino_ciudad, destino_pais, fecha_solicitud, descripcion_mercancia, valor_estimado_mercancia, estatus) VALUES
('SOL001', 'CLI001USER', 'maritimo', 'FCL', 'Guangzhou', 'China', 'Veracruz', 'México', DATE_SUB(NOW(), INTERVAL 3 DAY), 'Maquinaria industrial - 15 contenedores 40ft con equipos para manufactura textil, peso aproximado 280 toneladas', 850000.00, 'nueva'),

('SOL002', 'CLI001USER', 'aereo', 'LTL', 'Frankfurt', 'Alemania', 'Ciudad de México', 'México', DATE_SUB(NOW(), INTERVAL 1 DAY), 'Componentes electrónicos de alta tecnología - Requiere manejo especializado y temperatura controlada, 1,200 kg', 125000.00, 'en_proceso'),

('SOL003', 'CLI001USER', 'terrestre', 'FTL', 'Ciudad de México', 'México', 'Monterrey', 'México', NOW(), 'Productos farmacéuticos - Medicamentos que requieren cadena de frío y documentación especial sanitaria', 95000.00, 'nueva');

-- =====================================================
-- INCIDENCIAS DE PRUEBA
-- =====================================================

INSERT INTO INCIDENCIAS (id_incidencia, id_operacion, fecha_hora_incidencia, descripcion_incidencia, tipo_incidencia, estatus, fecha_resolucion, comentarios_resolucion, fecha_registro) VALUES
('INC001', 'OP003', DATE_SUB(NOW(), INTERVAL 12 HOUR), 'Documentación de origen presenta inconsistencias en códigos arancelarios. Aduana requiere certificación adicional del fabricante.', 'error_documentacion', 'en_revision', NULL, NULL, DATE_SUB(NOW(), INTERVAL 12 HOUR));

-- =====================================================
-- DEMORAS DE PRUEBA
-- =====================================================

INSERT INTO DEMORAS (id_demora, id_operacion, fecha_hora_demora, descripcion_demora, tipo_demora, costo_asociado, moneda, fecha_registro) VALUES
('DEM001', 'OP001', DATE_SUB(NOW(), INTERVAL 3 DAY), 'Retraso por condiciones climáticas adversas en el Pacífico. Tormenta tropical causó desvío de ruta por 18 horas.', 'climatica', 2500.00, 'USD', DATE_SUB(NOW(), INTERVAL 3 DAY));

END IF;

-- Confirmar datos insertados
SELECT 'Datos de prueba insertados correctamente' as mensaje;
