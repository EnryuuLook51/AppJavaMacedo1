# CU-09: Solicitar Recuperación de Acceso

Caso de uso principal

## Actor(es)

Estudiante, Docente, Trabajador administrativo

## Precondición

El usuario no recuerda su contraseña o se encuentra bloqueado.

## Flujo principal

1. El usuario solicita recuperar el acceso a su cuenta.
2. El sistema registra la solicitud de recuperación.
3. El sistema incluye el caso de uso CU-19 (Generar Token de Recuperación).
4. El sistema envía el token temporal al medio de contacto correspondiente del usuario (registrado en CU-01 y CU-03).
5. El usuario proporciona el token temporal recibido.
6. El sistema verifica que el token temporal sea válido y se encuentre dentro de su período de vigencia.
7. El sistema permite al usuario establecer una nueva contraseña, aplicando las reglas de seguridad correspondientes.
8. El sistema invalida el token temporal para impedir su reutilización (RF-17).
9. El sistema confirma la recuperación del acceso.

## Flujos alternativos

### A. Credencial temporal inválida o expirada

1. El usuario proporciona un token temporal.
2. El sistema determina que el token no es válido o que su período de vigencia ha finalizado.
3. El sistema rechaza la solicitud de recuperación.
4. El sistema informa al usuario que debe realizar una nueva solicitud de recuperación.

## Postcondición

El usuario recupera el acceso mediante una nueva contraseña. El token temporal utilizado queda invalidado y no puede volver a utilizarse.

## Relaciones

**Incluye:** [CU-19: Generar Token de Recuperación](CU-19-generar-token-de-recuperacion.md)

## Requisitos y reglas que cubre

| Código | Descripción |
| --- | --- |
| RF-15 | Permitir solicitar recuperación de acceso mediante contacto con un administrador o de forma autogestionada. |
| RF-17 | El token será de un único uso. |

---
[← Volver al índice](../00-indice.md)
