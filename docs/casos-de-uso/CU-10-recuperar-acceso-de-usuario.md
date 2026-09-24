# CU-10: Recuperar Acceso de Usuario

Caso de uso principal

## Actor(es)

Administrador del sistema

## Precondición

Un usuario ha contactado al administrador porque no recuerda su contraseña o se encuentra bloqueado, y no puede autogestionar su recuperación (Caso 5).

## Flujo principal

1. El administrador incluye el caso de uso CU-02 (Buscar Usuario) para ubicar al usuario.
2. El sistema incluye el caso de uso CU-19 (Generar Token de Recuperación).
3. El sistema entrega el token al usuario mediante el medio establecido.
4. El Administrador del sistema no obtiene acceso a la contraseña del usuario en ningún momento (RN-08).
5. El sistema incluye el caso de uso CU-18 (Registrar Intervención Administrativa).

## Flujos alternativos

### A. Administrador sin rol vigente (RF-28)

1. El sistema determina que el actor no posee el rol de administración.
2. El sistema rechaza la operación solicitada.
3. El sistema informa al actor que no cuenta con los permisos necesarios.

## Postcondición

El usuario recibe un token temporal para completar su propia recuperación de acceso (continúa en CU-09 desde el paso de verificación del token). La intervención queda registrada para auditoría.

## Relaciones

**Incluye:** [CU-02: Buscar Usuario](CU-02-buscar-usuario.md), [CU-19: Generar Token de Recuperación](CU-19-generar-token-de-recuperacion.md), [CU-18: Registrar Intervención Administrativa](CU-18-registrar-intervencion-administrativa.md)

## Requisitos y reglas que cubre

| Código | Descripción |
| --- | --- |
| RF-15 | Permitir solicitar recuperación de acceso mediante contacto con un administrador o de forma autogestionada. |
| RF-25 | Permitir que personal autorizado intervenga ante incidentes de acceso. |
| RF-28 | Verificar que el actor posea el rol de administración antes de ejecutar cualquier operación administrativa. |
| RNF-04 | El proceso no debe exponer al administrador la credencial del usuario. |

## Diseño

### Diagrama de casos de uso

![Diagrama 3: Administración de sesiones y recuperación de acceso](../assets/casos-de-uso/diagrama-3-sesiones-y-recuperacion-de-acceso.jpeg)

*Diagrama 3: Administración de sesiones y recuperación de acceso*

### Diagrama de robustez

*El Word aún no incluye el diagrama de robustez de este caso de uso.*

---
[← Volver al índice](../00-indice.md)
