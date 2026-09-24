# CU-19: Generar Token de Recuperación

Caso de uso «include» (subrutina compartida)

## Actor(es)

Ninguno (caso de uso incluido; se ejecuta como parte de CU-09 o CU-10).

## Precondición

Se ha registrado una solicitud de recuperación de acceso para un usuario.

## Flujo principal

1. Si existía un token de recuperación previo vigente para el usuario, el sistema lo invalida (RF-19).
2. El sistema genera un nuevo token temporal que cumple las condiciones de seguridad establecidas: unicidad e imposibilidad de predecirse (RF-16).
3. El sistema establece el tiempo de vigencia del token, según el valor configurado (RF-18, RF-30).
4. El sistema marca el token como de un solo uso (RF-17).

## Postcondición

Existe un único token de recuperación vigente para el usuario, con tiempo de expiración definido y listo para ser entregado.

## Relaciones

**Incluido por:** [CU-09: Solicitar Recuperación de Acceso](CU-09-solicitar-recuperacion-de-acceso.md), [CU-10: Recuperar Acceso de Usuario](CU-10-recuperar-acceso-de-usuario.md)

## Requisitos y reglas que cubre

| Código | Descripción |
| --- | --- |
| RF-16 | El sistema deberá generar un token temporal de recuperación que cumpla las condiciones de seguridad establecidas. |
| RF-17 | El token será de un único uso. |
| RF-18 | El token tendrá un tiempo de expiración definido. |
| RF-19 | Al generarse un nuevo token de recuperación, cualquier token anterior no utilizado del mismo usuario debe invalidarse automáticamente. |

---
[← Volver al índice](../00-indice.md)
