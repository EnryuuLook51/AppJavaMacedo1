# Requisitos por caso

## Caso 0: Registro y administración de usuarios (fundamento de todos los demás casos)

Los Casos 1 a 7 del enunciado asumen que la persona "se encuentra autorizada" y que ya existe una "identificación básica" (Caso 6) separada de sus roles. El enunciado no describe autorregistro; por eso la creación de la cuenta es un acto administrativo. Del mismo modo, dado que varios casos de uso administrativos necesitan primero ubicar al usuario sobre el cual van a actuar, se separa esa búsqueda en un caso de uso propio, reutilizado mediante <<include>> en vez de repetirlo.

- **RF-31:** El sistema debe permitir que el administrador registre a un nuevo usuario, estableciendo su identificación básica, su medio de contacto (necesario para la recuperación de acceso) y una contraseña inicial.
- **RF-32:** El sistema debe permitir que el administrador busque y consulte la información de un usuario: identificación, roles vigentes, estado de la cuenta y sesiones activas. *(nuevo v2.0)*
- **RF-33:** El sistema debe permitir que el administrador modifique los datos básicos de identificación de un usuario (por ejemplo, su medio de contacto), sin afectar su contraseña ni sus roles asignados. *(nuevo v2.0)*
- **RF-34:** El sistema debe permitir que el administrador dé de baja (deshabilite) la cuenta de un usuario, impidiendo cualquier acceso posterior, sin eliminar su historial de auditoría. *(nuevo v2.0)*

## Caso 1: Intentos de acceso

- **RF-01:** Limitar los intentos de autenticación fallidos y bloquear el acceso al superar el límite configurado.
- **RF-02:** El personal administrativo puede restablecer el acceso de una cuenta bloqueada.
- **RF-03:** Un inicio de sesión exitoso reinicia el contador de intentos fallidos.
- **RF-04:** Registrar cada intento de inicio de sesión para su análisis posterior.

## Caso 2: Varios equipos

- **RF-05:** Permitir sesiones activas simultáneas del mismo usuario en distintos equipos, cada una independiente de las demás.
- **RF-06:** Cada sesión se cierra de forma independiente por inactividad (no afecta las demás).
- **RF-07:** Permitir que el propio usuario cierre manualmente la sesión del equipo que está utilizando, sin afectar sus otras sesiones activas.
- **RF-08:** Permitir que el administrador consulte las sesiones activas de un usuario, identificando el equipo o contexto de cada una.
- **RF-09:** El administrador puede cerrar una sesión específica sin afectar las otras.

## Caso 3: Información histórica

- **RF-10:** Registrar cada intento de acceso: usuario, fecha/hora, resultado, contexto y motivo de rechazo.
- **RF-11:** Permitir consultar y filtrar el historial de intentos de acceso por usuario, fecha, resultado y contexto.
- **RNF-01:** La información histórica debe persistir, incluso si el usuario luego ingresa correctamente.

## Caso 4: Cambio de credenciales

- **RF-12:** Permitir modificar la contraseña de autenticación.
- **RNF-02:** Aplicar reglas de seguridad al definir una nueva contraseña.
- **RF-13:** No permitir reutilizar las contraseñas anteriormente utilizadas.
- **RF-14:** La cantidad de contraseñas históricas consideradas para el rechazo por reutilización debe ser configurable por el área de seguridad.
- **RNF-03:** El historial de credenciales debe estar encapsulado y no visible a otros componentes.

## Caso 5: Pérdida de acceso

- **RF-15:** Permitir solicitar recuperación de acceso mediante contacto con un administrador o de forma autogestionada.
- **RNF-04:** El proceso no debe exponer al administrador la credencial del usuario.
- **RF-16:** El sistema deberá generar un token temporal de recuperación que cumpla las condiciones de seguridad establecidas.
- **RF-17:** El token será de un único uso.
- **RF-18:** El token tendrá un tiempo de expiración definido.
- **RF-19:** Al generarse un nuevo token de recuperación, cualquier token anterior no utilizado del mismo usuario debe invalidarse automáticamente.

## Caso 6: Diferentes responsabilidades

- **RF-20:** Otorgar permisos distintos según el tipo de usuario (estudiante, docente, administrativo).
- **RF-21:** Permitir asignar, cambiar o retirar roles sin modificar la identificación básica del usuario.
- **RF-22:** Permitir que un usuario tenga varios roles simultáneos que evolucionen en el tiempo.
- **RF-23:** Restringir la asignación del rol de administración del sistema únicamente a trabajadores administrativos autorizados.
- **RF-24:** Determinar las opciones disponibles para el usuario en cada acceso según sus roles vigentes en ese momento.

## Caso 7: Intervención administrativa

- **RF-25:** Permitir que personal autorizado intervenga ante incidentes de acceso.
- **RF-26:** Permitir revisar el historial de accesos y actividades de un usuario en el contexto de un incidente.
- **RF-27:** Permitir actuar sobre sesiones activas (por ejemplo, cerrarlas).
- **RF-28:** Verificar que el actor posea el rol de administración antes de ejecutar cualquier operación administrativa.
- **RF-29:** Registrar toda intervención administrativa (quién, cuándo, sobre qué usuario y qué acción) para fines de auditoría.
- **RNF-05:** Las operaciones administrativas deben estar desacopladas del mecanismo interno de acceso.

## Requisitos transversales de configuración

Los siguientes parámetros se declaran "configurables" en los Casos 1, 2, 4 y 5 (RF-01, RF-06, RF-14, RF-18). El siguiente requisito y el CU-16 establecen quién y cómo se configuran:

- **RF-30:** Permitir que el área de seguridad configure el número máximo de intentos fallidos, el tiempo de inactividad para expiración de sesión, el tiempo de vigencia del token de recuperación y la cantidad de contraseñas históricas a validar, sin necesidad de modificar el código del sistema.

---
[← Volver al índice](00-indice.md)
