# API REST
Rutas bajo **/api**, JSON, fechas ISO 8601 con zona. Errores con campo mensaje.
GET /csrf devuelve headerName y token: envíe esa cabecera en las mutaciones y conserve las cookies. ACCESO es HttpOnly; los UUID de sesión visibles no son credenciales.

| Método / ruta | Datos o función |
| --- | --- |
| POST /login | identificacion, contrasena, equipo |
| GET /me | Usuario, sesión y permisos; registra actividad |
| GET /estado | Igual a /me sin renovar actividad |
| POST /logout | Cierra sesión actual |
| POST /contrasena | actual, nueva |
| POST /recuperacion | identificacion; correo si procede |
| POST /restablecer | token, nueva |
| GET /sesiones | Sesiones propias |
| POST /sesiones/{id}/cerrar | Cierre propio |
| GET /admin/usuarios?q= | Búsqueda |
| POST /admin/usuarios | identificacion, nombres, medioContacto, tipoPersona, administrativoAutorizado, contrasena |
| GET /admin/usuarios/{id} | Ficha |
| PUT /admin/usuarios/{id} | nombres, medioContacto |
| POST /admin/usuarios/{id}/desbloquear | Desbloqueo |
| POST /admin/usuarios/{id}/deshabilitar | Baja |
| POST /admin/usuarios/{id}/recuperar | Recuperación administrativa |
| GET /admin/usuarios/{id}/sesiones | Sesiones del usuario |
| POST /admin/sesiones/{id}/cerrar | Cierre administrativo |
| GET /admin/roles | Catálogo |
| POST /admin/usuarios/{id}/roles | rol (UUID), inicio, fin opcional |
| PUT /admin/usuarios/{id}/roles/{asignacion} | inicio, fin opcional |
| DELETE /admin/usuarios/{id}/roles/{asignacion} | Retiro histórico |
| GET /admin/politica | Política |
| PUT /admin/politica | maxIntentos, inactividadMaxima, vigenciaToken, cantidadHistoricas |
| GET /auditoria | Filtros usuario, resultado, contexto, desde, hasta |

Administración exige ADMINISTRAR; política exige CONFIGURAR y auditoría AUDITAR. El actor se obtiene exclusivamente de la sesión.

Tipos de persona: ESTUDIANTE, DOCENTE, ADMINISTRATIVO. Roles: esos tres más ADMINISTRADOR y SEGURIDAD.
Rangos: intentos 1–20; minutos 1–1440; históricas 1–24.

