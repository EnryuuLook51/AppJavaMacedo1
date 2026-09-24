# CU-13: Modificar Vigencia de Rol

Caso de uso principal

## Actor(es)

Administrador del sistema

## Precondición

El usuario cuenta con al menos un rol asignado (CU-12).

## Flujo principal

1. El Administrador incluye el caso de uso CU-02 (Buscar Usuario) para ubicar al usuario.
2. El sistema muestra los roles asignados al usuario.
3. El Administrador selecciona el rol cuya vigencia desea modificar.
4. El Administrador establece la nueva fecha de inicio o fin de vigencia.
5. El sistema actualiza la vigencia de la asignación correspondiente.
6. Los demás roles vigentes del usuario permanecen sin modificaciones.
7. El sistema incluye el caso de uso CU-18 (Registrar Intervención Administrativa).
8. El sistema confirma la actualización realizada.

## Postcondición

La vigencia del rol seleccionado queda actualizada. Los demás roles del usuario permanecen sin cambios.

## Relaciones

**Incluye:** [CU-02: Buscar Usuario](CU-02-buscar-usuario.md), [CU-18: Registrar Intervención Administrativa](CU-18-registrar-intervencion-administrativa.md)

## Requisitos y reglas que cubre

| Código | Descripción |
| --- | --- |
| RF-21 | Permitir asignar, cambiar o retirar roles sin modificar la identificación básica del usuario. |
| RF-22 | Permitir que un usuario tenga varios roles simultáneos que evolucionen en el tiempo. |

---
[← Volver al índice](../00-indice.md)
