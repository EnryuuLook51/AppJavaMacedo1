# Reglas de negocio

| ID | Regla de negocio |
| --- | --- |
| RN-01 | Un intento exitoso reinicia a cero el contador de fallos consecutivos. |
| RN-02 | Una cuenta bloqueada solo puede ser desbloqueada por un responsable autorizado y no se desbloquea mediante intentos posteriores. |
| RN-03 | Cada equipo o dispositivo genera una sesión propia; las sesiones son independientes entre sí. |
| RN-04 | Una sesión que permanezca inactiva durante el tiempo configurado deja de ser válida. |
| RN-05 | Los registros históricos de intentos son permanentes y no se eliminan cuando el usuario consigue ingresar correctamente. |
| RN-06 | Una nueva contraseña no puede coincidir con la contraseña vigente ni con ninguna de las contraseñas almacenadas en el historial de credenciales, según la cantidad configurada por el sistema. |
| RN-07 | El token de recuperación es de un solo uso y tiene una vigencia limitada. |
| RN-08 | Ningún administrador puede consultar, visualizar ni recuperar el secreto vigente de un usuario. |
| RN-09 | Los roles se asignan independientemente de las credenciales del usuario y pueden tener una vigencia temporal. |
| RN-10 | Solo los usuarios que posean un rol con permisos administrativos podrán ejecutar operaciones administrativas. |
| RN-11 | Cuando el número de intentos fallidos consecutivos alcance el límite configurado, la cuenta quedará bloqueada hasta que un responsable autorizado la desbloquee. |
| RN-12 | Todo intento de autenticación, exitoso o rechazado, deberá registrarse con la información necesaria para su posterior auditoría. |
| RN-13 | La asignación del rol de administración del sistema está restringida a trabajadores administrativos designados como tales. |
| RN-14 | Toda intervención administrativa debe quedar registrada para fines de auditoría. |
| RN-15 | Los valores de configuración de seguridad deben cumplir los rangos válidos definidos por el sistema antes de aplicarse (por ejemplo, valores mayores a cero). |
| RN-16 | Una sesión solo puede finalizar por tres vías excluyentes entre sí: expiración por inactividad, cierre voluntario del propio usuario, o cierre por intervención de un administrador autorizado. |
| RN-17 | Todo usuario debe existir en el sistema mediante un registro previo realizado por un administrador. No se contempla el autorregistro. |
| RN-18 | La búsqueda de un usuario es una operación de solo consulta: no modifica ningún dato del usuario ni de sus sesiones, roles o historial. |
| RN-19 | Dar de baja una cuenta invalida todas sus sesiones activas e impide iniciar sesión nuevamente, pero no elimina su historial de auditoría (RN-05). |

---
[← Volver al índice](00-indice.md)
