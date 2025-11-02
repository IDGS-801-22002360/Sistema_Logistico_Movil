-- Database: Logistica
USE Logistica;

-- Table for password reset tokens
CREATE TABLE IF NOT EXISTS PASSWORD_RESETS (
    id_reset VARCHAR(50) PRIMARY KEY,
    id_usuario VARCHAR(50) NOT NULL,
    token VARCHAR(100) UNIQUE NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_resets_user FOREIGN KEY (id_usuario) REFERENCES USUARIOS(id_usuario)
        ON DELETE CASCADE ON UPDATE CASCADE
);

DELIMITER $$

-- Create user (expects password already hashed)
DROP PROCEDURE IF EXISTS sp_create_user $$
CREATE PROCEDURE sp_create_user (
    IN p_nombre VARCHAR(100),
    IN p_apellido VARCHAR(100),
    IN p_email VARCHAR(255),
    IN p_password_hash VARCHAR(255),
    IN p_rol ENUM('admin','ventas','operaciones','cliente'),
    OUT o_id_usuario VARCHAR(50),
    OUT o_status VARCHAR(32),
    OUT o_message VARCHAR(255)
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    SELECT COUNT(1) INTO v_exists FROM USUARIOS WHERE email = p_email;

    IF v_exists > 0 THEN
        SET o_id_usuario = NULL;
        SET o_status = 'EMAIL_EXISTS';
        SET o_message = 'El correo ya está registrado.';
    ELSE
        SET o_id_usuario = REPLACE(UUID(),'-','');
        INSERT INTO USUARIOS (id_usuario, nombre, apellido, email, password, rol, activo)
        VALUES (o_id_usuario, p_nombre, p_apellido, p_email, p_password_hash, IFNULL(p_rol,'cliente'), TRUE);
        SET o_status = 'OK';
        SET o_message = 'Usuario creado correctamente';
    END IF;
END $$

-- Login (expects password already hashed). Returns user row when OK
DROP PROCEDURE IF EXISTS sp_login_user $$
CREATE PROCEDURE sp_login_user (
    IN p_email VARCHAR(255),
    IN p_password_hash VARCHAR(255),
    OUT o_status VARCHAR(32),
    OUT o_message VARCHAR(255)
)
BEGIN
    DECLARE v_id VARCHAR(50);
    DECLARE v_activo BOOLEAN;

    SELECT id_usuario, activo INTO v_id, v_activo
    FROM USUARIOS WHERE email = p_email AND password = p_password_hash LIMIT 1;

    IF v_id IS NULL THEN
        SET o_status = 'INVALID_CREDENTIALS';
        SET o_message = 'Usuario o contraseña inválidos';
    ELSEIF v_activo = 0 THEN
        SET o_status = 'INACTIVE';
        SET o_message = 'La cuenta está inactiva';
    ELSE
        SET o_status = 'OK';
        SET o_message = 'Login correcto';
        -- Devolver datos del usuario como resultset
        SELECT id_usuario, nombre, apellido, email, rol, activo, fecha_creacion
        FROM USUARIOS WHERE id_usuario = v_id;
    END IF;
END $$

-- Request password reset: creates a token and returns it
DROP PROCEDURE IF EXISTS sp_request_password_reset $$
CREATE PROCEDURE sp_request_password_reset (
    IN p_email VARCHAR(255),
    OUT o_status VARCHAR(32),
    OUT o_message VARCHAR(255),
    OUT o_token VARCHAR(100)
)
BEGIN
    DECLARE v_id VARCHAR(50);
    DECLARE v_activo BOOLEAN;

    SELECT id_usuario, activo INTO v_id, v_activo FROM USUARIOS WHERE email = p_email LIMIT 1;

    IF v_id IS NULL THEN
        SET o_status = 'NOT_FOUND';
        SET o_message = 'No existe una cuenta con ese correo';
        SET o_token = NULL;
    ELSEIF v_activo = 0 THEN
        SET o_status = 'INACTIVE';
        SET o_message = 'La cuenta está inactiva';
        SET o_token = NULL;
    ELSE
        SET o_token = REPLACE(UUID(),'-','');
        INSERT INTO PASSWORD_RESETS (id_reset, id_usuario, token, expires_at)
        VALUES (REPLACE(UUID(),'-',''), v_id, o_token, DATE_ADD(NOW(), INTERVAL 1 HOUR));
        SET o_status = 'OK';
        SET o_message = 'Token generado';
        -- Opcionalmente devolver también el id_usuario
        SELECT v_id AS id_usuario, o_token AS token, DATE_ADD(NOW(), INTERVAL 1 HOUR) AS expires_at;
    END IF;
END $$

-- Reset password with token
DROP PROCEDURE IF EXISTS sp_reset_password $$
CREATE PROCEDURE sp_reset_password (
    IN p_token VARCHAR(100),
    IN p_new_password_hash VARCHAR(255),
    OUT o_status VARCHAR(32),
    OUT o_message VARCHAR(255)
)
BEGIN
    DECLARE v_id_usuario VARCHAR(50);
    DECLARE v_expires DATETIME;
    DECLARE v_used DATETIME;

    SELECT pr.id_usuario, pr.expires_at, pr.used_at
      INTO v_id_usuario, v_expires, v_used
      FROM PASSWORD_RESETS pr
     WHERE pr.token = p_token
     LIMIT 1;

    IF v_id_usuario IS NULL THEN
        SET o_status = 'INVALID_TOKEN';
        SET o_message = 'Token inválido';
    ELSEIF v_used IS NOT NULL THEN
        SET o_status = 'TOKEN_USED';
        SET o_message = 'El token ya fue usado';
    ELSEIF v_expires < NOW() THEN
        SET o_status = 'TOKEN_EXPIRED';
        SET o_message = 'El token ha expirado';
    ELSE
        UPDATE USUARIOS SET password = p_new_password_hash WHERE id_usuario = v_id_usuario;
        UPDATE PASSWORD_RESETS SET used_at = NOW() WHERE token = p_token;
        SET o_status = 'OK';
        SET o_message = 'Contraseña actualizada';
    END IF;
END $$

-- Procedimiento para registrar un nuevo usuario cliente
DROP PROCEDURE IF EXISTS sp_registrar_cliente $$
CREATE PROCEDURE sp_registrar_cliente (
    IN p_nombre VARCHAR(100),
    IN p_apellido VARCHAR(100),
    IN p_email VARCHAR(255),
    IN p_password_hash VARCHAR(255),
    IN p_nombre_empresa VARCHAR(255),
    IN p_rfc VARCHAR(20),
    IN p_direccion VARCHAR(255),
    IN p_ciudad VARCHAR(100),
    IN p_pais VARCHAR(100),
    IN p_telefono VARCHAR(50),
    OUT o_id_usuario VARCHAR(50),
    OUT o_status VARCHAR(32),
    OUT o_message VARCHAR(255)
)
BEGIN
    DECLARE v_email_exists INT DEFAULT 0;
    DECLARE v_rfc_exists INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET o_status = 'ERROR';
        SET o_message = 'Error al registrar usuario. Inténtelo de nuevo.';
        SET o_id_usuario = NULL;
    END;
    
    START TRANSACTION;
    
    -- Verificar si el email ya existe
    SELECT COUNT(*) INTO v_email_exists FROM USUARIOS WHERE email = p_email;
    
    -- Verificar si el RFC ya existe
    SELECT COUNT(*) INTO v_rfc_exists FROM CLIENTES WHERE rfc = p_rfc;
    
    IF v_email_exists > 0 THEN
        SET o_status = 'EMAIL_EXISTS';
        SET o_message = 'El email ya está registrado';
        SET o_id_usuario = NULL;
        ROLLBACK;
    ELSEIF v_rfc_exists > 0 THEN
        SET o_status = 'RFC_EXISTS';
        SET o_message = 'El RFC ya está registrado';
        SET o_id_usuario = NULL;
        ROLLBACK;
    ELSE
        -- Generar ID único
        SET o_id_usuario = REPLACE(UUID(),'-','');
        
        -- Insertar usuario
        INSERT INTO USUARIOS (id_usuario, nombre, apellido, email, password, rol, activo, fecha_creacion)
        VALUES (o_id_usuario, p_nombre, p_apellido, p_email, p_password_hash, 'cliente', TRUE, NOW());
        
        -- Insertar cliente
        INSERT INTO CLIENTES (id_cliente, nombre_empresa, rfc, direccion, ciudad, pais, telefono, email_contacto, contacto_nombre, fecha_creacion)
        VALUES (o_id_usuario, p_nombre_empresa, p_rfc, p_direccion, p_ciudad, p_pais, p_telefono, p_email, CONCAT(p_nombre, ' ', p_apellido), NOW());
        
        COMMIT;
        
        SET o_status = 'OK';
        SET o_message = 'Usuario registrado exitosamente';
        
        -- Devolver datos del usuario registrado
        SELECT 
            id_usuario,
            nombre,
            apellido,
            email,
            rol,
            activo,
            fecha_creacion
        FROM USUARIOS 
        WHERE id_usuario = o_id_usuario;
    END IF;
END $$

-- Procedimiento para verificar si un email existe
DROP PROCEDURE IF EXISTS sp_verificar_email $$
CREATE PROCEDURE sp_verificar_email (
    IN p_email VARCHAR(255),
    OUT o_status VARCHAR(32),
    OUT o_message VARCHAR(255)
)
BEGIN
    DECLARE v_email_exists INT DEFAULT 0;
    
    SELECT COUNT(*) INTO v_email_exists FROM USUARIOS WHERE email = p_email;
    
    IF v_email_exists > 0 THEN
        SET o_status = 'EXISTS';
        SET o_message = 'El email ya está registrado';
    ELSE
        SET o_status = 'AVAILABLE';
        SET o_message = 'Email disponible';
    END IF;
END $$

-- Procedimiento para verificar si un RFC existe
DROP PROCEDURE IF EXISTS sp_verificar_rfc $$
CREATE PROCEDURE sp_verificar_rfc (
    IN p_rfc VARCHAR(20),
    OUT o_status VARCHAR(32),
    OUT o_message VARCHAR(255)
)
BEGIN
    DECLARE v_rfc_exists INT DEFAULT 0;
    
    SELECT COUNT(*) INTO v_rfc_exists FROM CLIENTES WHERE rfc = p_rfc;
    
    IF v_rfc_exists > 0 THEN
        SET o_status = 'EXISTS';
        SET o_message = 'El RFC ya está registrado';
    ELSE
        SET o_status = 'AVAILABLE';
        SET o_message = 'RFC disponible';
    END IF;
END $$

-- Procedimiento para obtener datos del usuario por ID
DROP PROCEDURE IF EXISTS sp_obtener_usuario_por_id $$
CREATE PROCEDURE sp_obtener_usuario_por_id (
    IN p_user_id VARCHAR(50)
)
BEGIN
    SELECT 
        u.id_usuario,
        u.nombre,
        u.apellido,
        u.email,
        u.rol,
        u.activo,
        u.fecha_creacion,
        CASE 
            WHEN u.rol = 'cliente' THEN c.nombre_empresa
            ELSE NULL
        END as nombre_empresa,
        CASE 
            WHEN u.rol = 'cliente' THEN c.rfc
            ELSE NULL
        END as rfc,
        CASE 
            WHEN u.rol = 'cliente' THEN c.telefono
            ELSE NULL
        END as telefono,
        CASE 
            WHEN u.rol = 'cliente' THEN c.direccion
            ELSE NULL
        END as direccion,
        CASE 
            WHEN u.rol = 'cliente' THEN c.ciudad
            ELSE NULL
        END as ciudad,
        CASE 
            WHEN u.rol = 'cliente' THEN c.pais
            ELSE NULL
        END as pais
    FROM USUARIOS u
    LEFT JOIN CLIENTES c ON u.id_usuario = c.id_cliente
    WHERE u.id_usuario = p_user_id AND u.activo = 1;
END $$

DELIMITER ;
