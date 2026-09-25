# Manual paso a paso de pruebas y capturas

**Universidad Nacional del Pacífico — Sistema de acceso a servicios universitarios**

Este manual indica qué cuenta utilizar, qué escribir, qué botón pulsar, qué resultado comprobar y qué capturar. Sigue el orden: algunas pruebas cambian contraseñas, retiran roles o deshabilitan cuentas.

**Las cuentas de este documento son datos propuestos: debes crearlas siguiendo los pasos.** Escribir el manual no las crea en el servidor.

## 1. Elige dónde hacer la demostración

### Opción A: demostración completa en tu computadora (recomendada)

Permite usar exactamente las credenciales siguientes y probar los correos sin contratar SMTP. Los datos quedan separados de la aplicación publicada.

Desde la carpeta del proyecto, abre una terminal:

```bash
mkdir -p docs/evidencias
export DB_URL='jdbc:h2:file:./data/demo-capturas-01'
export ADMIN_PASSWORD='AdminCapturas2026!'
export SMTP_HOST=localhost
export SMTP_PORT=1025
export SMTP_AUTH=false
export SMTP_TLS=false
./iniciar.sh
```

Mantén esa terminal abierta. En otra terminal, en la misma carpeta:

```bash
python3 scripts/buzon_smtp_pruebas.py
```

Abre:

- Aplicación: **http://localhost:8080**.
- Buzón de pruebas: **http://localhost:8025**.
- Administrador: **`admin` / `AdminCapturas2026!`**.

La contraseña anterior funciona si `demo-capturas-01` es una base nueva. Si ya la utilizaste, conserva la contraseña que tenga o cambia el nombre a `demo-capturas-02` para empezar otra demostración. `ADMIN_PASSWORD` no modifica una cuenta existente.

Si tienes otra instancia en el puerto 8080, añade `export PORT=8081` antes de iniciar y usa `http://localhost:8081` en todos los pasos.

### Opción B: aplicación publicada

Abre **https://sys-ac.soyfranco.org.pe**. El usuario es `admin`; su contraseña inicial generada se consulta desde tu terminal:

```bash
ssh asus "grep '^ADMIN_PASSWORD=' /opt/apps/services/sys-ac/.env"
```

Si ya cambiaste esa contraseña dentro de la aplicación, utiliza la nueva. No cambies el administrador publicado por la clave de ejemplo de la opción A.

Puedes seguir las mismas pantallas y cuentas del manual. **En la web pública reemplaza las contraseñas de ejemplo por claves privadas y anótalas aparte.** No son credenciales preexistentes. Las políticas de seguridad afectan a todas las cuentas: realiza las pruebas de límites e inactividad cuando no haya otros usuarios, anota los valores originales y restáuralos al terminar.

**Recuperación por correo:** en el despliegue inicial no se configuró SMTP. Los pasos de recuperación exitosa requieren configurarlo con correos que puedas recibir, o realizar esos pasos en la opción A. Un fallo de envío no cuenta como prueba exitosa de recuperación. El buzón local de tu computadora no recibe automáticamente mensajes del contenedor del servidor.

## 2. Organiza navegadores y capturas

Utiliza tres sesiones de navegador independientes:

| Ventana o perfil | Uso | Nombre del equipo al iniciar sesión |
| --- | --- | --- |
| A: navegador habitual | Administrador | `Admin-Capturas` |
| B: otro perfil o navegador | Cuenta que estás probando | `Equipo-Principal` |
| C: tercer perfil o navegador | Segunda sesión de esa cuenta | `Equipo-Secundario` |

Dos pestañas de un mismo perfil comparten la cookie: no sirven para demostrar dos sesiones independientes. Dos ventanas incógnitas del mismo navegador también pueden compartirla.

Guarda las capturas en `docs/evidencias/`. Usa los nombres de archivo de este manual. Captura la ventana o región donde se lean el título, los datos y el resultado. Para una ficha larga, toma varias imágenes desplazándote hacia abajo.

Los avisos de éxito/error duran aproximadamente **6,5 segundos**: prepara la herramienta antes de pulsar el botón. No muestres contraseñas ni tokens en las imágenes. En el formulario de token, bórralo después de obtener la confirmación antes de capturar; en el buzón vuelve a ocultar el contenido.

Para cada prueba, registra: **fecha, cuenta, resultado esperado, resultado obtenido, cumple/no cumple y captura**. Los resultados de este manual son esperados; debes verificarlos al ejecutar las pruebas.

## 3. Credenciales de la demostración local

| Identificación | Nombres y apellidos | Correo | Tipo de persona | Casilla administrativo autorizado | Contraseña inicial |
| --- | --- | --- | --- | --- | --- |
| `demo.estudiante` | Ana Torres Demo | `ana@example.com` | ESTUDIANTE | Desmarcada | `Estudiante2026!` |
| `demo.docente` | Diego Ramos Demo | `diego@example.com` | DOCENTE | Desmarcada | `Docente2026!` |
| `demo.administrativo` | Sofia Perez Demo | `sofia@example.com` | ADMINISTRATIVO | Marcada | `Administrativo2026!` |
| `demo.seguridad` | Alex Vega Demo | `alex@example.com` | ADMINISTRATIVO | Marcada | `Seguridad2026!` |
| `demo.bloqueo` | Bruno Diaz Demo | `bruno@example.com` | ESTUDIANTE | Desmarcada | `Bloqueo2026!` |
| `demo.baja` | Carla Ruiz Demo | `carla@example.com` | ESTUDIANTE | Desmarcada | `Baja2026!` |

Los correos `example.com` sirven con el buzón local de pruebas; no son buzones reales para SMTP público. Para repetir la guía sobre la misma base utiliza otro prefijo, por ejemplo `demo2.`, y sustitúyelo en todos los pasos.

## 4. Acceso y panel del administrador

### Prueba 01 — Pantalla inicial

1. En A abre la aplicación sin sesión iniciada.
2. Comprueba el nombre de la universidad y los campos **Identificación institucional**, **Contraseña** y **Nombre de este equipo**.
3. Comprueba los enlaces **Olvidé mi contraseña** y **Tengo un token**.
4. Captura **`01-pantalla-inicio.png`**.

### Prueba 02 — Inicio de sesión administrativo (CU-04)

1. Escribe `admin`, su contraseña y `Admin-Capturas`.
2. Pulsa **Iniciar sesión →**.
3. Debe mostrarse el panel y los roles `ADMINISTRADOR` y `SEGURIDAD`.
4. Comprueba los menús **Buscar usuarios**, **Registrar usuario**, **Bitácora de auditoría** y **Parámetros de seguridad**.
5. Captura **`02-panel-administrador.png`**.

Antes de continuar entra en **Parámetros de seguridad** y anota los cuatro valores originales. Para la demostración local, establece `5` intentos, `20` minutos de inactividad, `15` minutos de token y `5` contraseñas históricas. Pulsa **Guardar parámetros**.

## 5. Registrar, buscar y editar usuarios

### Prueba 03 — Registrar una cuenta sin roles (CU-01)

1. En A pulsa **Registrar usuario**.
2. Completa los datos de `demo.estudiante` según la tabla.
3. Deja desmarcada la autorización administrativa.
4. Captura **`03a-formulario-registro.png`**, con la contraseña oculta.
5. Pulsa **Crear cuenta**.
6. La ficha debe mostrar `ACTIVA`, cero fallos y la sección **Roles y vigencia** vacía.
7. Captura **`03b-cuenta-creada-sin-roles.png`**.
8. Repite el registro para las otras cinco cuentas. Todavía no asignes roles.

### Prueba 04 — Validaciones del registro

1. Intenta registrar otra cuenta con identificación `demo.estudiante`, datos válidos y una contraseña válida.
2. Debe rechazar la identificación duplicada. Captura **`04a-identificacion-duplicada.png`**.
3. En un nuevo formulario utiliza identificación `demo.invalida`, datos válidos y contraseña `sololetras`.
4. Pulsa **Crear cuenta**. Debe rechazar la contraseña por incumplir la política. Captura **`04b-clave-invalida.png`**.
5. Busca `demo.invalida` y verifica que no se creó.

### Prueba 05 — Búsqueda (CU-02)

1. Abre **Buscar usuarios**.
2. Busca `demo.estudiante` y pulsa **Buscar**.
3. Debe aparecer Ana, su tipo y estado. Captura **`05a-busqueda-identificacion.png`**.
4. Busca `Torres`: debe encontrar la misma cuenta. Captura **`05b-busqueda-nombre.png`**.
5. Busca `sincoincidencias999`: debe indicar que no se encontraron registros. Captura **`05c-busqueda-vacia.png`**.

### Prueba 06 — Modificar datos (CU-03)

1. Busca `demo.estudiante` y pulsa **Ver ficha →**.
2. En **Datos básicos**, cambia el nombre a `Ana Torres Actualizada` y el correo a `ana.actualizada@example.com`.
3. Pulsa **Guardar datos**.
4. Comprueba que se muestran los nuevos datos. Captura **`06-datos-actualizados.png`**.
5. Su identificación sigue siendo `demo.estudiante`.

## 6. Roles y paneles según permisos

### Prueba 07 — Acceso sin roles

1. En B inicia sesión con `demo.estudiante` / `Estudiante2026!`, equipo `Equipo-Principal`.
2. Debe entrar, mostrar cero roles y el aviso de que aún no tiene roles vigentes.
3. Debe poder usar **Mis sesiones** y **Cambiar contraseña**; no debe tener menús administrativos.
4. Captura **`07-panel-sin-roles.png`**.

### Prueba 08 — Asignar roles (CU-12)

1. En A abre la ficha de `demo.estudiante`.
2. En **Roles y vigencia → Nuevo rol**, selecciona `ESTUDIANTE`.
3. Pon el **Inicio** un minuto antes de la hora actual, para que ya esté vigente. Deja **Fin (opcional)** vacío.
4. Pulsa **Asignar rol**. Debe aparecer una fila `Vigente`.
5. Captura **`08a-rol-asignado.png`**.
6. Abre el selector de roles de Ana: no debe ofrecer `ADMINISTRADOR` ni `SEGURIDAD`, porque no está autorizada administrativamente. Captura **`08b-restriccion-roles.png`**.
7. Asigna a las demás cuentas:

| Cuenta | Rol a asignar |
| --- | --- |
| `demo.docente` | DOCENTE |
| `demo.administrativo` | ADMINISTRATIVO |
| `demo.seguridad` | SEGURIDAD |
| `demo.baja` | ESTUDIANTE |

Deja `demo.bloqueo` sin roles por ahora. Una cuenta de tipo ADMINISTRATIVO no obtiene automáticamente el rol ADMINISTRADOR.

### Prueba 09 — Paneles de cada rol

1. En B actualiza **Panel principal** como estudiante: debe mostrar `ESTUDIANTE` y **Servicios estudiantiles**. Captura **`09a-panel-estudiante.png`**.
2. Pulsa **Cerrar sesión** e inicia como `demo.docente` / `Docente2026!`: debe mostrar **Servicios docentes**. Captura **`09b-panel-docente.png`**.
3. Cierra sesión e inicia como `demo.administrativo` / `Administrativo2026!`: debe mostrar **Servicios administrativos**, sin gestión de usuarios. Captura **`09c-panel-administrativo.png`**.
4. Cierra sesión e inicia como `demo.seguridad` / `Seguridad2026!`: debe mostrar **Bitácora de auditoría** y **Parámetros de seguridad**, sin registro ni búsqueda administrativa de usuarios. Captura **`09d-panel-seguridad.png`**.
5. Cierra sesión en B.

Las tarjetas de servicios representan permisos vigentes. Esta aplicación no implementa matrícula, notas ni aulas externas.

### Prueba 10 — Roles múltiples y retiro (CU-14)

1. En A asigna también `DOCENTE` a `demo.estudiante`, con inicio un minuto antes de ahora.
2. En B inicia como Ana con su clave inicial.
3. Deben aparecer dos roles y las tarjetas estudiantil y docente. Captura **`10a-roles-multiples.png`**.
4. En A, en la fila `DOCENTE` de Ana, pulsa **Retirar** y luego **Confirmar**.
5. La fila debe conservarse con estado `Retirado`. Captura **`10b-rol-retirado.png`**.
6. En B recarga el panel: debe quedar únicamente el servicio estudiantil. Captura **`10c-permisos-tras-retiro.png`**.

### Prueba 11 — Vigencia y fechas inválidas (CU-13)

1. En A edita la fila vigente `ESTUDIANTE` de Ana pulsando **Editar**.
2. Conserva el inicio y coloca el fin para mañana a la misma hora.
3. Pulsa **Guardar vigencia**. Captura **`11a-vigencia-actualizada.png`**.
4. Vuelve a editar y coloca un fin anterior al inicio. Debe rechazarlo. Captura **`11b-vigencia-invalida.png`**.
5. Cierra el diálogo y comprueba que la fecha válida se conservó.
6. Edita otra vez: inicio mañana, fin pasado mañana. Guarda y captura el estado `Programado` en **`11c-rol-programado.png`**.
7. En B recarga: al no tener roles vigentes, Ana pierde temporalmente la tarjeta estudiantil.
8. **Restablece** el inicio a un minuto antes de ahora y deja el fin vacío; guarda. Así las siguientes pruebas vuelven a tener el rol vigente.
9. Opcional: intenta asignar de nuevo ESTUDIANTE con una vigencia que se solape. Debe rechazarla; captura **`11d-solapamiento-rechazado.png`**.

## 7. Sesiones y cierres

### Prueba 12 — Sesiones independientes y cierre propio (CU-05)

1. Mantén a Ana conectada en B con equipo `Equipo-Principal`.
2. En C inicia con la misma identificación y contraseña, equipo `Equipo-Secundario`.
3. En B abre **Mis sesiones**: deben aparecer ambas activas y la marca `Actual` junto a la primera.
4. Captura **`12a-dos-sesiones.png`**.
5. En B cierra la fila de `Equipo-Secundario` y confirma.
6. Marca **Mostrar también las sesiones cerradas**: una sesión debe estar cerrada y la actual activa. Captura **`12b-cierre-sesion-secundaria.png`**.
7. En C recarga: debe volver al acceso.
8. En B pulsa **Cerrar sesión** en el menú: debe volver al acceso. Captura **`12c-cierre-sesion-actual.png`**.

### Prueba 13 — Cierre administrativo (CU-07)

1. Inicia como `demo.docente` / `Docente2026!` en B y C, con equipos `Docente-A` y `Docente-B`.
2. En A abre la ficha del docente y baja a **Sesiones del usuario**.
3. Captura **`13a-sesiones-desde-administracion.png`**.
4. Cierra solo `Docente-B` y confirma. Captura **`13b-cierre-administrativo.png`**.
5. En C recarga: acceso denegado a esa sesión. En B abre el panel: debe continuar activo.
6. Cierra la sesión del docente en B.

### Prueba 14 — Expiración por inactividad (CU-06)

1. En A cambia **Inactividad máxima (minutos)** a `1`, conservando los otros valores, y guarda.
2. En B inicia como docente con equipo `Prueba-Inactividad`.
3. Espera aproximadamente **90 segundos** sin pulsar, escribir ni recargar en B. El cierre automático se revisa cada 30 segundos.
4. Debe volver al acceso; si no cambió, recarga después de la espera.
5. Captura **`14a-sesion-expirada.png`**.
6. A también puede expirar mientras esperas: vuelve a iniciar como admin si hace falta.
7. **Restablece inmediatamente** la inactividad a `20` minutos, o a su valor original si usas la web pública.
8. En auditoría filtra por `demo.docente`. Debe aparecer un **Cierre de sesión** con motivo `INACTIVIDAD`. Captura **`14b-auditoria-inactividad.png`**.

## 8. Contraseñas y bloqueo

### Prueba 15 — Cambiar contraseña y comprobar historial (CU-08)

1. En B inicia como `demo.estudiante` / `Estudiante2026!`.
2. Abre **Cambiar contraseña**.
3. Escribe actual `Estudiante2026!`, nueva `EstudianteNuevo2026!` y la misma confirmación.
4. Pulsa **Guardar contraseña**. Captura **`15a-contrasena-actualizada.png`**.
5. Intenta cambiar de `EstudianteNuevo2026!` a `Estudiante2026!`: debe rechazar la reutilización. Captura **`15b-historial-rechaza-clave.png`**.
6. Prueba nueva `EstudianteOtra2026!` y confirmación `NoCoincide2026!`: debe indicar que no coinciden. Captura **`15c-confirmacion-diferente.png`**.
7. Cierra sesión. Intenta entrar una sola vez con la antigua `Estudiante2026!`: debe rechazarla.
8. Entra con `EstudianteNuevo2026!`: debe funcionar. Esta es la contraseña de Ana hasta la recuperación posterior.
9. Cierra sesión en B.

### Prueba 16 — Bloqueo y desbloqueo (CU-11, CU-17)

1. En A configura **Máximo de intentos fallidos** en `3` y guarda.
2. En B intenta iniciar como `demo.bloqueo`, equipo `Prueba-Bloqueo`, con `Incorrecta2026!`.
3. Repite hasta completar **tres intentos incorrectos**. Captura el rechazo en **`16a-acceso-rechazado.png`**.
4. En A busca esa cuenta y abre su ficha. Debe mostrar `BLOQUEADA` y tres fallos. Captura **`16b-cuenta-bloqueada.png`**.
5. En B prueba su clave correcta `Bloqueo2026!`: debe seguir rechazando por bloqueo.
6. En A pulsa **Desbloquear → Confirmar**. Debe mostrar `ACTIVA` y cero fallos. Captura **`16c-cuenta-desbloqueada.png`**.
7. En B inicia con la clave correcta: ahora funciona. Cierra sesión.
8. Restablece en A el máximo de intentos a `5`, o a su valor original.

No ejecutes los intentos erróneos contra `admin`.

## 9. Recuperación y tokens

**Requisito:** buzón local encendido, o SMTP real configurado y correos accesibles. En el servidor publicado sin SMTP, registra el caso como pendiente por configuración; no lo marques como aprobado.

### Prueba 17 — Solicitar y restablecer contraseña (CU-09, CU-19)

1. Sin sesión en B, pulsa **Solicitar recuperación** o **Olvidé mi contraseña**.
2. Escribe `demo.estudiante` y pulsa **Solicitar recuperación**.
3. Captura la confirmación en **`17a-solicitud-recuperacion.png`**.
4. Abre el buzón `http://localhost:8025`. Busca el mensaje para `ana.actualizada@example.com`.
5. Captura la lista con el contenido oculto: **`17b-correo-recibido.png`**.
6. Pulsa **Mostrar contenido para copiar el token** y copia el token. No lo captures.
7. En la aplicación abre **Restablecer con token** y pega el token.
8. Escribe nueva contraseña y confirmación `EstudianteRecuperado2026!`.
9. Pulsa **Guardar contraseña**. Captura el mensaje de éxito en **`17c-restablecimiento-exitoso.png`**; el formulario se limpia al tener éxito.
10. Vuelve a introducir ese mismo token e intenta otra clave, `EstudianteOtro2026!`.
11. Debe rechazar el token usado. Borra el token visible antes de capturar **`17d-token-usado-rechazado.png`**.
12. Comprueba que puedes entrar como Ana con `EstudianteRecuperado2026!` y cierra sesión.

### Prueba 18 — Recuperación iniciada por administrador (CU-10)

1. En A abre la ficha de `demo.docente`.
2. Pulsa **Enviar recuperación → Confirmar**.
3. Captura **`18a-recuperacion-administrativa.png`**.
4. En el buzón busca el mensaje para `diego@example.com` y copia el token.
5. En B, sin sesión, restablece usando `DocenteRecuperado2026!` como nueva clave y confirmación.
6. Inicia como docente con esa contraseña para comprobar el resultado y cierra sesión.
7. En A entra a **Consultar historial →** desde su ficha: captura la intervención y la solicitud en **`18b-auditoria-recuperacion-admin.png`**.

### Prueba 19 — El token nuevo invalida el anterior (CU-19)

1. Solicita recuperación de `demo.estudiante` dos veces seguidas.
2. Identifica por su orden los correos A y B. Guarda los tokens temporalmente fuera del documento.
3. Intenta restablecer con el token A y clave nueva `EstudianteFinal2026!`: debe rechazarlo.
4. Borra el token del campo y captura **`19a-token-anterior-invalidado.png`**.
5. Usa el token B con `EstudianteFinal2026!` en nueva clave y confirmación: debe funcionar.
6. Captura **`19b-token-nuevo-aceptado.png`**.
7. Comprueba el login con `EstudianteFinal2026!` y cierra sesión.

### Prueba 20 — Token vencido

1. En A cambia **Vigencia del token (minutos)** a `1` y guarda.
2. En B solicita recuperación de `demo.estudiante` y copia el nuevo token.
3. Espera al menos **70 segundos** desde la recepción del mensaje.
4. Intenta restablecer con ese token y `EstudianteVencido2026!`.
5. Debe rechazarlo. Borra el token visible y captura **`20-token-vencido.png`**.
6. La contraseña sigue siendo `EstudianteFinal2026!` porque el intento falló.
7. En A restablece la vigencia a `15` minutos, o a su valor original.

## 10. Auditoría y configuración

### Prueba 21 — Consultar la bitácora (CU-15, CU-17, CU-18)

1. En A abre **Bitácora de auditoría** y pulsa **Limpiar campos** para ver los eventos.
2. Captura **`21a-bitacora-general.png`**.
3. En **Identificación del usuario**, escribe `demo.bloqueo`; en **Resultado**, selecciona **Acceso rechazado**. Pulsa **Aplicar filtros**.
4. Comprueba fecha, usuario, equipo/contexto y motivo. Captura **`21b-intentos-rechazados.png`**.
5. Limpia y filtra por `demo.estudiante`, dejando **Todos los eventos**.
6. Busca intervenciones de modificación, asignación y retiro. Comprueba ejecutor `admin` y detalle. Captura **`21c-intervenciones.png`**.
7. Si completaste recuperación, filtra por **Recuperación enviada** y captura **`21d-solicitudes-enviadas.png`**.
8. Limpia los campos. Filtra por contexto `Prueba-Bloqueo`, **Desde** hoy a las 00:00 y **Hasta** mañana a las 00:00. Captura **`21e-filtros-contexto-fechas.png`**.
9. Pulsa **Limpiar campos**: debe volver la lista sin filtros.

CU-17 y CU-18 se generan al iniciar sesión o ejecutar intervenciones: no son botones separados.

### Prueba 22 — Guardar política y validar límites (CU-16)

1. En B inicia como `demo.seguridad` / `Seguridad2026!`.
2. Abre **Parámetros de seguridad**.
3. Para la demo local guarda `5`, `20`, `15`, `5`, en el orden de los campos. En la web pública restaura los valores originales anotados.
4. Captura **`22a-politica-guardada.png`**.
5. Escribe `0` en máximo de intentos y pulsa **Guardar parámetros**.
6. El navegador debe impedir el envío por el mínimo `1`. Captura **`22b-validacion-minimo.png`**.
7. Recarga: debe conservarse la política válida, no el cero. Esta prueba evidencia validación del formulario, no una petición inválida al servidor.
8. Abre la bitácora y busca la intervención de configuración hecha por `demo.seguridad`. Captura **`22c-auditoria-configuracion.png`**.
9. Cierra sesión en B.

## 11. Deshabilitar cuenta — dejar para el final

### Prueba 23 — Baja con conservación de historial (CU-20)

1. En B inicia como `demo.baja` / `Baja2026!`, equipo `Equipo-Baja`.
2. En A busca esa cuenta y abre su ficha.
3. Comprueba que tiene una sesión activa y pulsa **Deshabilitar**.
4. Captura el diálogo en **`23a-confirmar-baja.png`** y pulsa **Confirmar**.
5. La cuenta debe mostrar `DESHABILITADA` y sus sesiones cerradas. Captura **`23b-cuenta-deshabilitada.png`**.
6. En B recarga: la sesión deja de servir.
7. Intenta entrar otra vez con la contraseña correcta: debe rechazarlo. Captura **`23c-login-baja-rechazado.png`**.
8. En A pulsa **Consultar historial →**: deben conservarse el inicio anterior, la intervención y el cierre. Captura **`23d-historial-conservado.png`**.

La interfaz no permite reactivar esta cuenta. Por eso se utiliza una cuenta dedicada y se prueba al final.

## 12. Comprobación final y credenciales resultantes

Si seguiste todos los pasos locales, las contraseñas finales son:

| Cuenta | Contraseña final | Estado esperado |
| --- | --- | --- |
| admin | `AdminCapturas2026!` | ACTIVA; ADMINISTRADOR y SEGURIDAD |
| demo.estudiante | `EstudianteFinal2026!` | ACTIVA; ESTUDIANTE |
| demo.docente | `DocenteRecuperado2026!` | ACTIVA; DOCENTE |
| demo.administrativo | `Administrativo2026!` | ACTIVA; ADMINISTRATIVO |
| demo.seguridad | `Seguridad2026!` | ACTIVA; SEGURIDAD |
| demo.bloqueo | `Bloqueo2026!` | ACTIVA; sin roles |
| demo.baja | `Baja2026!` | DESHABILITADA; login rechazado |

Si omitiste recuperación, Ana conserva `EstudianteNuevo2026!` y Diego conserva `Docente2026!`. Si hiciste parte de recuperación, usa la última clave que se guardó exitosamente.

1. Confirma que restauraste los cuatro parámetros de seguridad.
2. Revisa que las capturas sean legibles y no tengan contraseñas ni tokens visibles.
3. En la demo local detén aplicación y buzón con **Ctrl+C** en sus terminales. La base queda guardada; los mensajes del buzón se pierden al cerrarlo.
4. En la web pública, al terminar, deshabilita las cuentas de demostración que ya no necesites. Conserva tu administrador real.

## 13. Cobertura para el documento

| Caso de uso | Prueba de este manual |
| --- | --- |
| CU-01 Registrar usuario | 03 y 04 |
| CU-02 Buscar usuario | 05 |
| CU-03 Modificar datos | 06 |
| CU-04 Iniciar sesión | 02, 07 y 09 |
| CU-05 Cerrar sesión | 12 |
| CU-06 Cerrar por inactividad | 14 |
| CU-07 Cerrar sesión de usuario | 13 |
| CU-08 Cambiar contraseña | 15 |
| CU-09 Solicitar recuperación | 17 |
| CU-10 Recuperar acceso administrativamente | 18 |
| CU-11 Desbloquear cuenta | 16 |
| CU-12 Asignar rol | 08 y 10 |
| CU-13 Modificar vigencia | 11 |
| CU-14 Retirar rol | 10 |
| CU-15 Consultar bitácora | 21 |
| CU-16 Configurar seguridad | 22 |
| CU-17 Registrar intento | 16 y 21 |
| CU-18 Registrar intervención | 06, 10, 13 y 21 |
| CU-19 Generar token | 17, 19 y 20 |
| CU-20 Deshabilitar cuenta | 23 |

### Plantilla para cada evidencia

```text
Prueba: 16 — Bloqueo y desbloqueo
Caso de uso: CU-11
Cuenta utilizada: admin / demo.bloqueo
Acción: desbloquear la cuenta después de tres intentos fallidos.
Resultado esperado: estado ACTIVA y contador de fallos en cero.
Resultado obtenido: [completar después de probar]
Estado: [Cumple / No cumple / Pendiente]
Captura: 16c-cuenta-desbloqueada.png
Fecha: [fecha de ejecución]
```

Orden sugerido del informe: acceso y paneles, usuarios, roles, sesiones, contraseñas, recuperación, auditoría, política y baja. El manual ofrece más de una captura por caso cuando se necesita mostrar la entrada y el resultado.
