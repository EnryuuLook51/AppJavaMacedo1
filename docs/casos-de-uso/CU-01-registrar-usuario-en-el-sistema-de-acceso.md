# CU-01: Registrar Usuario en el Sistema de Acceso

Caso de uso principal

## Actor(es)

Administrador del sistema

## Precondición

Existe una persona (estudiante, docente o trabajador administrativo) que debe utilizar los servicios institucionales y aún no posee una cuenta en el sistema de acceso (RN-17).

## Flujo principal

1. El Administrador del sistema ingresa la identificación básica de la nueva persona (datos de identidad y tipo de persona: estudiante, docente o trabajador administrativo).
2. El Administrador ingresa el medio de contacto que se utilizará posteriormente para la recuperación de acceso (RF-31).
3. El sistema valida que no exista previamente una cuenta con la misma identificación.
4. El sistema establece una contraseña inicial, sujeta a las mismas reglas de seguridad que un cambio de contraseña (RNF-02).
5. El sistema registra la nueva cuenta con el contador de intentos fallidos en cero y sin roles asignados.
6. El sistema incluye el caso de uso CU-18 (Registrar Intervención Administrativa).
7. El sistema confirma al Administrador que la cuenta fue creada.

## Flujos alternativos

### A. Identificación ya existente

1. El sistema determina que ya existe una cuenta con la misma identificación básica.
2. El sistema rechaza el registro.
3. El sistema informa al Administrador que la persona ya se encuentra registrada.

## Postcondición

Queda creada la identificación básica de la persona en el sistema, sin roles asignados. La cuenta queda lista para que, mediante CU-12 (Asignar Rol a Usuario), se le otorguen las funciones correspondientes.

## Relaciones

**Incluye:** [CU-18: Registrar Intervención Administrativa](CU-18-registrar-intervencion-administrativa.md)

## Requisitos y reglas que cubre

| Código | Descripción |
| --- | --- |
| RF-31 | El sistema debe permitir que el administrador registre a un nuevo usuario, estableciendo su identificación básica, su medio de contacto (necesario para la recuperación de acceso) y una contraseña inicial. |
| RN-17 | Todo usuario debe existir en el sistema mediante un registro previo realizado por un administrador. No se contempla el autorregistro. |

---
[← Volver al índice](../00-indice.md)
