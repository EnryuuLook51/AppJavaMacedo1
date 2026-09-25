
# Universidad Nacional del Pacífico · Sistema de acceso a servicios universitarios

Aplicación Java con interfaz web en español basada en las dos páginas del diagrama de clases y los CU-01 a CU-20.

## Ejecutar en Linux
Requiere Java 21+ (ya disponible en este equipo). Maven queda instalado dentro de `.tools/` y `mvnw` lo descarga y verifica si falta.

```bash
./iniciar.sh
```

En el primer inicio se solicita la contraseña de **admin** sin mostrarla en pantalla. Abra **http://localhost:8080**. Para utilizar el JAR ya compilado: `./iniciar.sh --sin-compilar`. No se crean usuarios de demostración en su base de datos.

```bash
./mvnw verify                       # compilar, probar y generar el JAR
java -jar target/acceso-1.0.0.jar    # con ADMIN_PASSWORD en el primer inicio
```

## Ejecutar en Windows
Requiere JDK 21+ y Maven 3.9+. El script detecta también Maven de NetBeans o IntelliJ.
~~~powershell
.\iniciar.ps1
~~~
En el primer inicio solicita una contraseña para **admin** (8–72 caracteres, mayúscula, minúscula, número y símbolo). Abra **http://localhost:8080**. Ctrl+C detiene el servidor.

Alternativamente:
~~~powershell
$env:ADMIN_PASSWORD = 'UnaClaveInicial1!'
mvn clean verify
java -jar target/acceso-1.0.0.jar
~~~
ADMIN_PASSWORD solo se utiliza cuando la base está vacía. No cambia una contraseña ya registrada. La cuenta inicial recibe ADMINISTRADOR y SEGURIDAD.

Los datos persisten en **data/acceso.mv.db**. DB_URL permite cambiar la ubicación. No elimine data si desea conservar el historial. La instalación está diseñada para una sola instancia y escucha en localhost.

## Funciones
- Registro, búsqueda y edición de usuarios; bloqueo, desbloqueo y baja.
- Sesiones independientes, cierre propio/administrativo y expiración.
- Contraseñas BCrypt e historial privado configurable.
- Recuperación por correo con token aleatorio, temporal y de un uso.
- Roles múltiples con vigencia, restricciones y retiro histórico.
- Auditoría de intentos, intervenciones, cierres y solicitudes de recuperación con filtros.
- Política configurable, permisos comprobados en cada operación y protección CSRF.

Los servicios académicos externos (notas, matrícula, aulas) están fuera del alcance; el panel muestra sus permisos de acceso.

## Recuperación por correo
Se necesita SMTP real o un servidor local de pruebas en el puerto 1025.
~~~powershell
$env:SMTP_HOST = 'smtp.su-institucion.edu.pe'
$env:SMTP_PORT = '587'
$env:SMTP_AUTH = 'true'
$env:SMTP_TLS = 'true'
$env:SMTP_USER = 'cuenta-smtp'
$env:SMTP_PASSWORD = 'secreto-smtp'
$env:MAIL_FROM = 'acceso@su-institucion.edu.pe'
.\iniciar.ps1
~~~
En Linux se usan las mismas variables con `export`:
```bash
export SMTP_HOST='smtp.su-institucion.edu.pe'
export SMTP_PORT=587 SMTP_AUTH=true SMTP_TLS=true
export SMTP_USER='cuenta-smtp' MAIL_FROM='acceso@su-institucion.edu.pe'
read -rs -p 'Contraseña SMTP: ' SMTP_PASSWORD; echo
export SMTP_PASSWORD
./iniciar.sh --sin-compilar
```

Actualice el correo de admin en su ficha antes de recuperar su acceso. Los tokens no se muestran al administrador ni se devuelven por API. Si falla SMTP, se informa el error y no se invalida el token anterior.

## Arquitectura
Las entidades corresponden al diagrama. ClaveHistorica es privada al paquete; RegistroAuditoria es abstracta y tiene cuatro especializaciones: IntentoAcceso, IntervencionAdministrativa, CierreSesion y SolicitudRecuperacion. ServicioAdministracion delega en IControlAcceso. GestorExpiracionSesiones usa ese mismo contrato cada 30 segundos. Las sesiones y los tokens pertenecen a CuentaAcceso; auditoría enlaza usuario objetivo, ejecutor, sesión cerrada y token generado mediante claves foráneas. La API expone resúmenes sin hashes, contraseñas ni tokens.

UnidadTrabajo serializa las transacciones hasta su confirmación en una sola instancia. Los rechazos de autenticación conservan contadores y auditoría; los errores de validación revierten cambios. Para varias instancias harían falta bloqueos de base de datos.

Recuperar contraseña **no desbloquea** una cuenta; la baja cierra sesiones por ADMINISTRACION. Usuarios nuevos nacen sin roles. Se admite cerrar otras sesiones propias según el prototipo.

## Verificar
~~~powershell
mvn test
~~~
Pruebas de integración: bloqueo, auditoría, sesiones, expiración, baja, historial, tokens, roles, HTTP y CSRF. H2 en memoria y SMTP simulado. Se comprueban también relaciones persistidas, solicitudes de recuperación fallidas, conservación del token anterior ante fallos SMTP y edición de vigencias sin solapamientos.

Para preparar una demostración completa con cuentas ficticias y capturas de los 20 casos de uso, siga la [guía de pruebas y capturas](docs/guia-pruebas-y-capturas.md).

Más detalles: [API](docs/api.md), [trazabilidad](docs/implementacion.md), [stack](docs/stack.md).
