# CU-17: Registrar Intento de Acceso

Caso de uso «include» (subrutina compartida)

## Actor(es)

Ninguno (caso de uso incluido; se ejecuta como parte de CU-04).

## Precondición

Se ha producido un intento de inicio de sesión, exitoso o rechazado.

## Flujo principal

1. El sistema registra el usuario, la fecha y hora del intento.
2. El sistema registra el resultado del intento (exitoso o rechazado) y el contexto de origen (equipo, dirección de red).
3. Si el intento fue rechazado, el sistema registra el motivo del rechazo.
4. El sistema almacena el registro de forma permanente, sin posibilidad de eliminación (RNF-01, RN-05).

## Postcondición

El intento queda registrado de forma permanente en la bitácora, disponible para su consulta posterior mediante CU-15.

## Relaciones

**Incluido por:** [CU-04: Iniciar Sesión](CU-04-iniciar-sesion.md)

## Requisitos y reglas que cubre

| Código | Descripción |
| --- | --- |
| RF-04 | Registrar cada intento de inicio de sesión para su análisis posterior. |
| RF-10 | Registrar cada intento de acceso: usuario, fecha/hora, resultado, contexto y motivo de rechazo. |
| RNF-01 | La información histórica debe persistir, incluso si el usuario luego ingresa correctamente. |

---
[← Volver al índice](../00-indice.md)
