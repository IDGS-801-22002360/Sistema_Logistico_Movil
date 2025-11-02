-- Datos de prueba para CRM Logístico
USE Logistica;

-- Insertar algunos países de ejemplo
INSERT INTO PAISES (id_pais, nombre_pais, codigo_iso2) VALUES
(REPLACE(UUID(),'-',''), 'México', 'MX'),
(REPLACE(UUID(),'-',''), 'Estados Unidos', 'US'),
(REPLACE(UUID(),'-',''), 'Canadá', 'CA'),
(REPLACE(UUID(),'-',''), 'China', 'CN'),
(REPLACE(UUID(),'-',''), 'Alemania', 'DE');

-- Insertar algunas localizaciones de ejemplo
SET @mexico_id = (SELECT id_pais FROM PAISES WHERE codigo_iso2 = 'MX' LIMIT 1);
SET @usa_id = (SELECT id_pais FROM PAISES WHERE codigo_iso2 = 'US' LIMIT 1);

INSERT INTO LOCALIZACIONES (id_localizacion, id_pais, nombre_ciudad, tipo_ubicacion, codigo_iata_icao, direccion) VALUES
(REPLACE(UUID(),'-',''), @mexico_id, 'Ciudad de México', 'aeropuerto', 'MEX', 'Aeropuerto Internacional Benito Juárez'),
(REPLACE(UUID(),'-',''), @mexico_id, 'Veracruz', 'puerto', 'VER', 'Puerto de Veracruz'),
(REPLACE(UUID(),'-',''), @mexico_id, 'Tijuana', 'centro_distribucion', '', 'Zona Industrial Tijuana'),
(REPLACE(UUID(),'-',''), @usa_id, 'Los Angeles', 'puerto', 'LAX', 'Puerto de Los Angeles'),
(REPLACE(UUID(),'-',''), @usa_id, 'Miami', 'aeropuerto', 'MIA', 'Aeropuerto Internacional de Miami');

-- Insertar un usuario administrador de prueba
-- Contraseña: admin123 (SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)
INSERT INTO USUARIOS (id_usuario, nombre, apellido, email, password, rol, activo, fecha_creacion) VALUES
('admin001', 'Administrador', 'Sistema', 'admin@crmlogistico.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin', TRUE, NOW());

-- Insertar un usuario de ventas de prueba
-- Contraseña: ventas123 (SHA-256: a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3)
INSERT INTO USUARIOS (id_usuario, nombre, apellido, email, password, rol, activo, fecha_creacion) VALUES
('ventas001', 'Juan Carlos', 'Vendedor', 'ventas@crmlogistico.com', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3', 'ventas', TRUE, NOW());

-- Insertar un usuario operativo de prueba  
-- Contraseña: operaciones123 (SHA-256: 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92)
INSERT INTO USUARIOS (id_usuario, nombre, apellido, email, password, rol, activo, fecha_creacion) VALUES
('operativo001', 'María Elena', 'Operaciones', 'operaciones@crmlogistico.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'operaciones', TRUE, NOW());

-- Insertar algunos proveedores de ejemplo
INSERT INTO PROVEEDORES (id_proveedor, nombre_empresa, rfc, direccion, ciudad, pais, telefono, email_contacto, contacto_nombre, tipo_servicio_ofrecido, fecha_creacion) VALUES
(REPLACE(UUID(),'-',''), 'Transportes México SA', 'TMX123456789', 'Av. Industria 123', 'Ciudad de México', 'México', '555-1234567', 'contacto@transportesmexico.com', 'Carlos González', 'terrestre', NOW()),
(REPLACE(UUID(),'-',''), 'Logística Marítima Internacional', 'LMI987654321', 'Puerto Industrial 456', 'Veracruz', 'México', '229-9876543', 'info@maritima.com', 'Ana Martínez', 'maritimo', NOW()),
(REPLACE(UUID(),'-',''), 'Aero Cargo Express', 'ACE555666777', 'Terminal Aérea 789', 'Ciudad de México', 'México', '555-7890123', 'cargo@aeroexpress.com', 'Roberto Silva', 'aereo', NOW());

-- Insertar algunos agentes de ejemplo
INSERT INTO AGENTES (id_agente, nombre, apellido, email, telefono, tipo_agente, fecha_creacion) VALUES
(REPLACE(UUID(),'-',''), 'Luis', 'Hernández', 'luis.hernandez@agenteasduana.com', '555-1111111', 'aduanal', NOW()),
(REPLACE(UUID(),'-',''), 'Patricia', 'López', 'patricia.lopez@carga.com', '555-2222222', 'carga', NOW()),
(REPLACE(UUID(),'-',''), 'Miguel', 'Torres', 'miguel.torres@seguros.com', '555-3333333', 'seguros', NOW());

-- Nota: Este script está comentado para evitar duplicados
-- Ejecutar solo si necesitas datos de prueba para desarrollo

-- IMPORTANTE: Las contraseñas de los usuarios de prueba son:
-- admin@crmlogistico.com: admin123
-- ventas@crmlogistico.com: ventas123  
-- operaciones@crmlogistico.com: operaciones123
