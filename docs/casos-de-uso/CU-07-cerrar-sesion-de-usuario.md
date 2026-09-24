# CU-07: Cerrar Sesión de Usuario

Caso de uso principal

## Actor(es)

Administrador del sistema

## Precondición

El usuario objetivo tiene al menos una sesión activa registrada.

## Flujo principal

1. El administrador incluye el caso de uso CU-02 (Buscar Usuario) para ubicar al usuario.
2. El sistema muestra la lista de sesiones activas del usuario, identificando el equipo o contexto de cada una (RF-08).
3. El administrador selecciona la sesión sobre la cual desea intervenir.
4. El sistema invalida únicamente la sesión seleccionada (RF-09).
5. Las demás sesiones activas del usuario permanecen vigentes (RN-03, RN-16).
6. El sistema incluye el caso de uso CU-18 (Registrar Intervención Administrativa).
7. El sistema informa al administrador que la sesión fue cerrada.

## Flujos alternativos

### A. Administrador sin rol vigente (RF-28)

1. El sistema determina que el actor no posee el rol de administración.
2. El sistema rechaza la operación solicitada.
3. El sistema informa al actor que no cuenta con los permisos necesarios.
4. Finaliza el caso de uso.

## Postcondición

La sesión seleccionada queda invalidada. Las demás sesiones del usuario permanecen sin modificaciones. La intervención queda registrada para auditoría.

## Relaciones

**Incluye:** [CU-02: Buscar Usuario](CU-02-buscar-usuario.md), [CU-18: Registrar Intervención Administrativa](CU-18-registrar-intervencion-administrativa.md)

## Requisitos y reglas que cubre

| Código | Descripción |
| --- | --- |
| RF-08 | Permitir que el administrador consulte las sesiones activas de un usuario, identificando el equipo o contexto de cada una. |
| RF-09 | El administrador puede cerrar una sesión específica sin afectar las otras. |
| RF-27 | Permitir actuar sobre sesiones activas (por ejemplo, cerrarlas). |
| RF-28 | Verificar que el actor posea el rol de administración antes de ejecutar cualquier operación administrativa. |

---
[← Volver al índice](../00-indice.md)
