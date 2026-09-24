package pe.edu.uns.sistemas.acceso.web.controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.time.Instant;
import pe.edu.uns.sistemas.acceso.domain.*;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
import pe.edu.uns.sistemas.acceso.service.*;
import pe.edu.uns.sistemas.acceso.repository.*;
import pe.edu.uns.sistemas.acceso.web.*;
import pe.edu.uns.sistemas.acceso.exception.AccesoException;
@RestController @RequestMapping("/api/admin")
public class AdministracionController {
    private final UnidadTrabajo tx;private final ServicioAdministracion admin;private final ServicioAutorizacion permisos;private final SesionActual actual;private final SesionRepository sesiones;private final RolRepository roles;private final PoliticaRepository politicas;
    public AdministracionController(UnidadTrabajo t,ServicioAdministracion a,ServicioAutorizacion p,SesionActual s,SesionRepository se,RolRepository r,PoliticaRepository po){tx=t;admin=a;permisos=p;actual=s;sesiones=se;roles=r;politicas=po;}
    public record Registro(@NotBlank String identificacion,@NotBlank String nombres,@NotBlank @Email String medioContacto,@NotNull TipoPersona tipoPersona,boolean administrativoAutorizado,@NotBlank String contrasena){}
    public record Datos(@NotBlank String nombres,@NotBlank @Email String medioContacto){}
    public record Vigencia(UUID rol,@NotNull Instant inicio,Instant fin){}
    public record Politica(int maxIntentos,int inactividadMaxima,int vigenciaToken,int cantidadHistoricas){}
    @GetMapping("/usuarios") Object buscar(@RequestParam(defaultValue="") String q,HttpServletRequest req){return tx.ejecutar(()->admin.buscarUsuario(actual.obtener(req).usuario(),q).stream().map(ResumenUsuario::de).toList());}
    @PostMapping("/usuarios") Object registrar(@Valid @RequestBody Registro d,HttpServletRequest req){return tx.ejecutar(()->ResumenUsuario.de(admin.registrarUsuario(actual.obtener(req).usuario(),d.identificacion(),d.nombres(),d.medioContacto(),d.tipoPersona(),d.administrativoAutorizado(),d.contrasena())));}
    @GetMapping("/usuarios/{id}") Object obtener(@PathVariable UUID id,HttpServletRequest req){return tx.ejecutar(()->ResumenUsuario.de(admin.obtener(actual.obtener(req).usuario(),id)));}
    @PutMapping("/usuarios/{id}") Object editar(@PathVariable UUID id,@Valid @RequestBody Datos d,HttpServletRequest req){return tx.ejecutar(()->{Usuario actor=actual.obtener(req).usuario();admin.modificarDatos(actor,admin.obtener(actor,id),d.nombres(),d.medioContacto());return ok();});}
    @PostMapping("/usuarios/{id}/{accion:desbloquear|deshabilitar|recuperar}") Object accion(@PathVariable UUID id,@PathVariable String accion,HttpServletRequest req){return tx.ejecutar(()->{Usuario actor=actual.obtener(req).usuario();Usuario u=admin.obtener(actor,id);switch(accion){case "desbloquear"->admin.desbloquearCuenta(actor,u);case "deshabilitar"->admin.deshabilitarCuenta(actor,u);case "recuperar"->admin.recuperarAcceso(actor,u);}return ok();});}
    @GetMapping("/usuarios/{id}/sesiones") Object sesiones(@PathVariable UUID id,HttpServletRequest req){return tx.ejecutar(()->sesiones.findByUsuario(admin.obtener(actual.obtener(req).usuario(),id)));}
    @PostMapping("/sesiones/{id}/cerrar") Object cerrar(@PathVariable UUID id,HttpServletRequest req){return tx.ejecutar(()->{admin.cerrarSesion(actual.obtener(req).usuario(),sesiones.findById(id).orElseThrow(()->new AccesoException(404,"Sesión inexistente.")));return ok();});}
    @GetMapping("/roles") Object roles(HttpServletRequest req){return tx.ejecutar(()->{permisos.exigirAdministracion(actual.obtener(req).usuario());return roles.findAll();});}
    @PostMapping("/usuarios/{id}/roles") Object asignar(@PathVariable UUID id,@Valid @RequestBody Vigencia d,HttpServletRequest req){return tx.ejecutar(()->{Usuario a=actual.obtener(req).usuario();if(d.rol()==null)throw new IllegalArgumentException("Seleccione un rol.");admin.asignarRol(a,admin.obtener(a,id),d.rol(),d.inicio(),d.fin());return ok();});}
    @PutMapping("/usuarios/{id}/roles/{asignacion}") Object vigencia(@PathVariable UUID id,@PathVariable UUID asignacion,@Valid @RequestBody Vigencia d,HttpServletRequest req){return tx.ejecutar(()->{Usuario a=actual.obtener(req).usuario();admin.modificarVigencia(a,admin.obtener(a,id),asignacion,d.inicio(),d.fin());return ok();});}
    @DeleteMapping("/usuarios/{id}/roles/{asignacion}") Object retirar(@PathVariable UUID id,@PathVariable UUID asignacion,HttpServletRequest req){return tx.ejecutar(()->{Usuario a=actual.obtener(req).usuario();admin.retirarRol(a,admin.obtener(a,id),asignacion);return ok();});}
    @GetMapping("/politica") Object politica(HttpServletRequest req){return tx.ejecutar(()->{permisos.exigirPermiso(actual.obtener(req).usuario(),"CONFIGURAR");return politicas.findById(1L).orElseThrow();});}
    @PutMapping("/politica") Object configurar(@RequestBody Politica d,HttpServletRequest req){return tx.ejecutar(()->{admin.configurarSeguridad(actual.obtener(req).usuario(),d.maxIntentos(),d.inactividadMaxima(),d.vigenciaToken(),d.cantidadHistoricas());return ok();});}
    private Map<String,String> ok(){return Map.of("mensaje","Operación realizada correctamente.");}
}
