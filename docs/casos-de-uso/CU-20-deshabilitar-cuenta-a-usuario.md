# CU-20: Deshabilitar Cuenta a Usuario

Caso de uso principal

## Actor(es)

Administrador del sistema

## Precondición

El usuario se encuentra registrado en el sistema (CU-01) y su cuenta está activa.

## Flujo principal

1. El administrador incluye el caso de uso CU-02 (Buscar Usuario) para ubicar al usuario a dar de baja.
2. El administrador solicita deshabilitar la cuenta del usuario (RF-34).
3. El sistema cambia el estado de la cuenta a "Deshabilitada".
4. El sistema invalida todas las sesiones activas del usuario, en cualquier equipo (RN-19).
5. El sistema impide que la cuenta pueda iniciar sesión o recuperar el acceso mientras permanezca deshabilitada.
6. El historial de auditoría y de accesos previos del usuario permanece disponible sin cambios (RN-05, RN-19).
7. El sistema incluye el caso de uso CU-18 (Registrar Intervención Administrativa).
8. El sistema confirma al administrador que la cuenta fue deshabilitada.

## Flujos alternativos

### A. Administrador sin rol vigente (RF-28)

1. El sistema determina que el actor no posee el rol de administración.
2. El sistema rechaza la operación solicitada.
3. El sistema informa al actor que no cuenta con los permisos necesarios.

### B. Cuenta ya deshabilitada

1. El sistema determina que la cuenta ya se encuentra en estado "Deshabilitada".
2. El sistema informa al administrador que no es necesario repetir la operación.

## Postcondición

La cuenta del usuario queda deshabilitada y todas sus sesiones activas quedan invalidadas. El usuario no puede iniciar sesión ni recuperar el acceso mientras la cuenta permanezca en ese estado. Su historial de auditoría se conserva íntegro. La intervención queda registrada para auditoría.

## Relaciones

**Incluye:** [CU-02: Buscar Usuario](CU-02-buscar-usuario.md), [CU-18: Registrar Intervención Administrativa](CU-18-registrar-intervencion-administrativa.md)

## Requisitos y reglas que cubre

| Código | Descripción |
| --- | --- |
| RF-34 | El sistema debe permitir que el administrador dé de baja (deshabilite) la cuenta de un usuario, impidiendo cualquier acceso posterior, sin eliminar su historial de auditoría. |
| RN-19 | Dar de baja una cuenta invalida todas sus sesiones activas e impide iniciar sesión nuevamente, pero no elimina su historial de auditoría (RN-05). |

---
[← Volver al índice](../00-indice.md)
