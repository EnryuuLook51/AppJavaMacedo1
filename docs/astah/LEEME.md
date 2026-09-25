# Diagrama de clases para Astah

Archivo a abrir: **SistemaAcceso-completado.xml**.

Se trabajó sobre `/home/TheFool/Downloads/Class Diagram1.xml`, exportado por Astah. El original no fue modificado. La copia conserva los dos diagramas, las clases importadas de Java, sus atributos, operaciones y las extensiones JUDE de presentación.

## Abrir

1. Guarda tu proyecto actual en Astah.
2. Ve a **Tools → XML Input & Output → Open XML Project**.
3. Selecciona `SistemaAcceso-completado.xml`, dentro de esta carpeta.
4. Revisa **Class Diagram1** (dominio) y **Class Diagram2** (servicios y pantallas).
5. Guarda el resultado con **File → Save As** como `SistemaAcceso-completado.asta`.
6. Exporta cada diagrama con **Tools → Export Image** para insertarlo en el documento.

Usa la apertura de proyecto XML nativo, no un importador XMI 2.x: este archivo utiliza el formato XMI 1.1 / UML 1.4 con extensiones de Astah del original.

Referencia oficial: https://astah.net/support/astah-pro/user-guide/xml-input-and-output/

## Relaciones corregidas

Cada multiplicidad se aplica al extremo junto al nombre de la clase en la tabla. El rombo lleno está en el propietario.

| Propietario o extremo A | Extremo B | Relación |
| --- | --- | --- |
| Usuario `1` | CuentaAcceso `1` | Composición bidireccional; eliminada la segunda asociación redundante |
| Usuario `1` | AsignacionRol `0..*` | Composición |
| CuentaAcceso `1` | Credencial `1` | Composición |
| Credencial `1` | ClaveHistorica `0..*` | Composición; se conserva el orden del historial |
| CuentaAcceso `1` | Sesion `0..*` | Composición |
| CuentaAcceso `1` | TokenRecuperacion `0..*` | Composición |
| Rol `1` | AsignacionRol `0..*` | Asociación |
| Rol `0..*` | Permiso `0..*` | Asociación |
| Usuario `0..1` | RegistroAuditoria `0..*` | Objetivo opcional: permite auditar identificaciones inexistentes y acciones sobre el sistema |
| Usuario `1` | IntervencionAdministrativa `0..*` | Actor de la intervención |
| Sesion `1` | CierreSesion `0..1` | Una sesión puede no haberse cerrado todavía |
| TokenRecuperacion `0..1` | SolicitudRecuperacion `1` | Una solicitud fallida no genera token; cada token generado corresponde a una solicitud |

La relación opcional de usuario objetivo sigue el texto de la documentación («usuario objetivo opcional») y la implementación, aunque la imagen de referencia muestra un `1` junto a ese extremo.

Se conservaron las cuatro generalizaciones hacia `RegistroAuditoria` y la realización `ServicioAutenticacion → IControlAcceso`, que ya existían. Astah codifica esta última como `Usage` con estereotipo `realize`.

Se añadieron dependencias de Credencial, CuentaAcceso, Sesion y TokenRecuperacion hacia PoliticaSeguridad.

## Pantallas añadidas

En la parte superior de Class Diagram2 se incorporaron las fronteras conceptuales de la segunda página de documentación:

- PantallaAcceso: iniciar sesión, cerrar sesión, cambiar contraseña y recuperar acceso.
- PantallaAdministracion: gestionar usuarios, roles, cuentas y sesiones.
- PantallaAuditoria: establecer filtros y consultar bitácora.
- PantallaPoliticaSeguridad: mostrar y actualizar parámetros.

Cada pantalla tiene el estereotipo `boundary` y una dependencia al servicio correspondiente. Representan las vistas web; no se afirma que existan clases Java con esos nombres.

## Validación y presentación

Se validó que el XML puede analizarse, no tiene IDs duplicados ni referencias colgantes y conserva ambos diagramas. Se comprobó la matriz de relaciones y las cuatro herencias. El informe técnico está en `SistemaAcceso-completado.validacion.json`.

**La apertura real en Astah no se ha probado.** El formato de presentación de Astah es propietario; se reutilizaron las estructuras de tu exportación. Las posiciones originales se conservaron salvo la ampliación del marco del segundo diagrama para añadir las pantallas. Puede ser necesario acomodar etiquetas y líneas nuevas después de abrirlo. No se ha realizado una nueva distribución visual completa.

Se mantuvieron los atributos y métodos de tu exportación Java. Esta corrección completa relaciones y fronteras; no sustituye sus firmas por las abreviadas del diagrama conceptual de la documentación.

Para regenerar desde la raíz del proyecto:

```sh
python3 scripts/completar_astah.py '/home/TheFool/Downloads/Class Diagram1.xml' docs/astah/SistemaAcceso-completado.xml
```
