# CRM Logístico Móvil - Configuración de Base de Datos

## Configuración Inicial

### 1. Configurar MySQL

1. **Crear la base de datos**: Ejecuta el script SQL que tienes para crear las tablas
2. **Ejecutar stored procedures**: Ejecuta el archivo `db/auth_procedures.sql` en tu MySQL Workbench

### 2. Configurar la aplicación

1. **Editar configuración de base de datos**:
   - Abre el archivo `app/src/main/java/com/example/crm_logistico_movil/config/DatabaseConfig.kt`
   - Cambia los siguientes valores por los de tu configuración:
     ```kotlin
     const val DB_HOST = "tu_ip_servidor" // Por ejemplo: "192.168.1.100"
     const val DB_USER = "tu_usuario_mysql"
     const val DB_PASSWORD = "tu_contraseña_mysql"
     ```

2. **Para desarrollo local**: Usa `"localhost"` o `"127.0.0.1"` como DB_HOST
3. **Para servidor remoto**: Usa la IP del servidor donde está MySQL

### 3. Stored Procedures Incluidos

Los siguientes stored procedures se han creado para la autenticación:

- `sp_login_user`: Autenticación de usuarios
- `sp_registrar_cliente`: Registro de nuevos clientes
- `sp_request_password_reset`: Solicitar recuperación de contraseña
- `sp_reset_password`: Restablecer contraseña
- `sp_verificar_email`: Verificar si un email existe
- `sp_verificar_rfc`: Verificar si un RFC existe
- `sp_obtener_usuario_por_id`: Obtener datos de usuario

### 4. Funcionalidades Implementadas

#### Login
- Validación de credenciales contra la base de datos
- Autenticación con hash SHA-256
- Navegación automática según el rol del usuario

#### Registro
- Formulario completo para registro de clientes
- Validación de email y RFC únicos
- Creación automática de usuario y cliente

#### Recuperación de Contraseña
- Solicitud de reset por email
- Sistema de tokens temporales
- Validación de tokens con expiración

### 5. Estructura de la Base de Datos

La aplicación utiliza las siguientes tablas principales:
- `USUARIOS`: Datos de autenticación y roles
- `CLIENTES`: Información de empresas cliente
- `PASSWORD_RESET_TOKENS`: Tokens para recuperación de contraseña

### 6. Seguridad

- Las contraseñas se almacenan con hash SHA-256
- Tokens de recuperación tienen expiración de 1 hora
- Validación de entrada en todos los formularios
- Conexión segura a la base de datos

### 7. Uso de la Aplicación

1. **Primera vez**: Los usuarios deben registrarse usando el enlace en la pantalla de login
2. **Login**: Usar email y contraseña para acceder
3. **Olvido de contraseña**: Usar el enlace correspondiente para recuperarla

### 8. Resolución de Problemas

#### Error de conexión a la base de datos:
- Verificar que MySQL esté ejecutándose
- Confirmar que las credenciales en `DatabaseConfig.kt` sean correctas
- Verificar que la IP y puerto sean accesibles desde el dispositivo

#### Error de stored procedures:
- Asegurarse de haber ejecutado `db/auth_procedures.sql`
- Verificar que no haya errores de sintaxis en los procedimientos

#### Error de dependencias:
- Ejecutar `./gradlew clean build` para limpiar y reconstruir
- Verificar que todas las dependencias estén descargadas correctamente

### 9. Próximos Pasos

Una vez que tengas funcionando la autenticación, podrás expandir la aplicación con:
- Gestión de cotizaciones
- Seguimiento de operaciones
- Módulo de facturación
- Sistema de notificaciones

## Notas Importantes

- **Cambiar credenciales**: No olvides cambiar las credenciales por defecto en `DatabaseConfig.kt`
- **Red**: Asegúrate de que el dispositivo Android tenga acceso a la red donde está el servidor MySQL
- **Firewall**: Verificar que el puerto 3306 (MySQL) esté abierto en el servidor
- **Permisos**: La aplicación ya incluye los permisos de internet necesarios
- **IP fija**: El servidor MySQL debe tener una IP estática para evitar pérdida de conexión
- **Usuario MySQL dedicado**: Crear un usuario exclusivo para la app con permisos limitados
- **Seguridad**: No almacenar credenciales en texto plano dentro de la app
- **API recomendada**: Para producción, se recomienda usar un backend en lugar de conectar directo a MySQL
- **Timeout**: Configurar tiempos de espera adecuados en la conexión
