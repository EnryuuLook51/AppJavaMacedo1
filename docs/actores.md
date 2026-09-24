# Actores y criterio de descomposición

## Actores

- **Estudiante:** usuario que ingresa a los servicios estudiantiles.
- **Docente:** usuario que puede utilizar varios equipos de forma simultánea.
- **Trabajador administrativo:** usuario con funciones administrativas.
- **Administrador del sistema:** trabajador administrativo con rol de administración, que registra, busca y administra usuarios, y puede intervenir en la gestión de roles, cuentas y sesiones.
- **Área de seguridad:** responsable de supervisar los eventos relacionados con el acceso, revisar el historial de autenticación y definir los parámetros de seguridad del sistema.
- **Sistema (Temporizador):** actor secundario automatizado encargado de ejecutar tareas programadas basadas en tiempo (expiración de tokens y cierres por inactividad).

## Criterio de descomposición de casos de uso

Se evita el uso de verbos genéricos ("Gestionar", "Administrar", "Manejar") como nombre de caso de uso, porque ocultan actores, disparadores o postcondiciones distintas. Cada caso de uso principal representa un único objetivo verificable de un único actor. Cuando un mismo paso se repite igual dentro de varios casos de uso (registrar en bitácora, registrar intervención administrativa, generar token, o —a partir de esta versión— buscar/ubicar a un usuario), se extrae como caso de uso «include» en vez de redactarlo varias veces. No se usa «extend» para relaciones donde el paso incluido es obligatorio: «extend» solo aplica a comportamiento verdaderamente opcional que se agrega a un caso de uso base ya completo por sí mismo.

---
[← Volver al índice](00-indice.md)
