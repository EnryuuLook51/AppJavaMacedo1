# CU-06: Cerrar Sesión por Inactividad

Caso de uso principal

## Actor(es)

Sistema (Temporizador)

## Precondición

Existe una sesión activa que ha superado el tiempo de inactividad configurado (RF-06, RF-30).

## Flujo principal

1. El temporizador del sistema detecta que una sesión específica ha superado el tiempo límite de inactividad.
2. El sistema identifica la sesión que ha excedido dicho tiempo.
3. El sistema invalida únicamente la sesión identificada.
4. Las demás sesiones activas del usuario permanecen vigentes (RN-03, RN-16).
5. El sistema registra el evento de cierre de sesión por inactividad.

## Postcondición

La sesión inactiva queda invalidada automáticamente. Las demás sesiones activas del usuario no se ven afectadas.

## Requisitos y reglas que cubre

| Código | Descripción |
| --- | --- |
| RF-06 | Cada sesión se cierra de forma independiente por inactividad (no afecta las demás). |
| RN-04 | Una sesión que permanezca inactiva durante el tiempo configurado deja de ser válida. |
| RN-16 | Una sesión solo puede finalizar por tres vías excluyentes entre sí: expiración por inactividad, cierre voluntario del propio usuario, o cierre por intervención de un administrador autorizado. |

---
[← Volver al índice](../00-indice.md)
