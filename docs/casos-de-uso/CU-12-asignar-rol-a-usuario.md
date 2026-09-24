# CU-12: Asignar Rol a Usuario

Caso de uso principal

## Actor(es)

Administrador del sistema

## Precondición

El usuario ya se encuentra registrado en el sistema (CU-01). El Administrador del sistema posee permisos para asignar roles.

## Flujo principal

1. El Administrador incluye el caso de uso CU-02 (Buscar Usuario) para ubicar al usuario.
2. El sistema muestra los roles actualmente asignados al usuario, si los tuviera.
3. El Administrador selecciona el rol a asignar y, cuando corresponda, el período de vigencia (RF-22).
4. El sistema registra la nueva asignación de rol sin modificar la identificación básica del usuario (RF-21).
5. El sistema permite que el usuario mantenga simultáneamente los roles previos junto con el nuevo (RF-22).
6. El sistema incluye el caso de uso CU-18 (Registrar Intervención Administrativa).
7. El sistema confirma la asignación realizada.

## Flujos alternativos

### A. Asignación del rol de administración (RF-23)

1. El Administrador selecciona el rol de administración del sistema para asignarlo a un usuario.
2. El sistema verifica que el usuario destino sea un trabajador administrativo autorizado.
3. Si la verificación es negativa, el sistema rechaza la asignación e informa el motivo.
4. Si la verificación es positiva, el sistema continúa con el flujo principal.

## Postcondición

El usuario queda con el nuevo rol asignado, conservando los roles previos vigentes, sin alterar su identificación básica. Si el rol es de administración, el usuario destino es siempre un trabajador administrativo autorizado.

## Relaciones

**Incluye:** [CU-02: Buscar Usuario](CU-02-buscar-usuario.md), [CU-18: Registrar Intervención Administrativa](CU-18-registrar-intervencion-administrativa.md)

## Requisitos y reglas que cubre

| Código | Descripción |
| --- | --- |
| RF-21 | Permitir asignar, cambiar o retirar roles sin modificar la identificación básica del usuario. |
| RF-22 | Permitir que un usuario tenga varios roles simultáneos que evolucionen en el tiempo. |
| RF-23 | Restringir la asignación del rol de administración del sistema únicamente a trabajadores administrativos autorizados. |

---
[← Volver al índice](../00-indice.md)
