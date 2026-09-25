# Trazabilidad

| Casos de uso | Implementación / pantalla |
| --- | --- |
| CU-01, 02, 03 | ServicioAdministracion / registro, búsqueda y ficha |
| CU-04, 05, 06 | ServicioAutenticacion, GestorExpiracionSesiones / login y sesiones |
| CU-07, 10, 11, 20 | ServicioAdministracion → IControlAcceso / acciones de ficha |
| CU-08 | Credencial y ClaveHistorica / cambiar contraseña |
| CU-09, 19 | TokenRecuperacion, SolicitudRecuperacion / solicitud, restablecimiento y bitácora |
| CU-12, 13, 14 | AsignacionRol, ServicioAutorizacion / roles y vigencia |
| CU-15, 17, 18 | ServicioAuditoria y especializaciones / bitácora |
| CU-16 | PoliticaSeguridad / parámetros |

Se mantienen clases y responsabilidades de ambas páginas del diagrama.
Las duraciones se representan en minutos y las fechas como Instant UTC.
Las sesiones y tokens se consultan por repositorio, evitando colecciones grandes en CuentaAcceso.

ADMINISTRADOR tiene ADMINISTRAR y AUDITAR. SEGURIDAD tiene AUDITAR y CONFIGURAR.
La cuenta inicial obtiene ambos roles. Los roles sensibles exigen trabajador administrativo autorizado.

El temporizador corre cada 30 segundos y las peticiones comprueban la expiración inmediatamente. El sondeo de estado no extiende la sesión. Las consultas no modifican al usuario objetivo; sí registran actividad del actor.

Se priorizó el diagrama: recuperación no desbloquea; baja cierra sesiones como ADMINISTRACION; nuevo usuario sin roles. Se permite cerrar otras sesiones propias según el prototipo. Se impide deshabilitar la propia cuenta.

SMTP necesita configuración externa. Los módulos académicos y un despliegue público/distribuido no forman parte de esta implementación.


## Correspondencia con las relaciones del diagrama corregido

Se priorizan las dos páginas de `DiagramClass/` sobre las imágenes orientativas del prototipo.

| Relación | Persistencia |
| --- | --- |
| Usuario → CuentaAcceso → Credencial | Relaciones uno a uno; composición mediante cascada |
| Credencial → ClaveHistorica | Colección interna ordenada, sin acceso público a hashes |
| Usuario → AsignacionRol → Rol | Colección de asignaciones; cada asignación referencia un rol |
| Rol ↔ Permiso | Muchos a muchos |
| CuentaAcceso → Sesion / TokenRecuperacion | Clave foránea obligatoria a la cuenta; consulta por repositorio |
| RegistroAuditoria → Usuario | Objetivo opcional y copia de la identificación ingresada |
| IntervencionAdministrativa → Usuario | Ejecutor relacionado y copia de su identificación |
| CierreSesion → Sesion | Uno a uno; una sesión tiene como máximo un cierre |
| SolicitudRecuperacion → TokenRecuperacion | Uno a uno opcional; sin token para solicitudes rechazadas o envíos fallidos |

`RegistroAuditoria` tiene cuatro especializaciones. Las solicitudes distinguen los canales AUTOGESTION y ADMINISTRACION y los resultados ENVIADA, RECHAZADA y ERROR_ENVIO. Un fallo SMTP conserva su evento de auditoría y no invalida el token anterior. La respuesta pública para cuentas desconocidas o deshabilitadas es genérica.

La bitácora presenta los cuatro tipos de eventos y permite filtrar los resultados de recuperación. La ficha enlaza al historial del usuario. La vista personal muestra sesiones activas y permite incluir las cerradas. Tanto asignar como modificar vigencias rechaza períodos superpuestos del mismo rol.

Esta versión modifica el esquema anterior de sesiones, tokens y auditoría. La instalación de este repositorio estaba vacía; si se utiliza una base creada por una versión anterior, se necesita una migración de claves foráneas antes de usarla. No basta con `ddl-auto=update` para migrar esas relaciones.
