# Trazabilidad

| Casos de uso | Implementación / pantalla |
| --- | --- |
| CU-01, 02, 03 | ServicioAdministracion / registro, búsqueda y ficha |
| CU-04, 05, 06 | ServicioAutenticacion, temporizador / login y sesiones |
| CU-07, 10, 11, 20 | ServicioAdministracion → IControlAcceso / acciones de ficha |
| CU-08 | Credencial y ClaveHistorica / cambiar contraseña |
| CU-09, 19 | TokenRecuperacion / solicitud y restablecimiento |
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

