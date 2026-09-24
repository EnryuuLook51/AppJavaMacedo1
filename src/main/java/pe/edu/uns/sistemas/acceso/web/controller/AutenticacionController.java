package pe.edu.uns.sistemas.acceso.web.controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.time.Instant;
import java.security.SecureRandom;
import org.springframework.http.ResponseCookie;
import pe.edu.uns.sistemas.acceso.domain.*;
import pe.edu.uns.sistemas.acceso.service.*;
import pe.edu.uns.sistemas.acceso.repository.SesionRepository;
import pe.edu.uns.sistemas.acceso.web.*;
import pe.edu.uns.sistemas.acceso.exception.AccesoException;
@RestController @RequestMapping("/api")
public class AutenticacionController {
    private final UnidadTrabajo tx;private final ServicioAutenticacion acceso;private final ServicioAutorizacion permisos;private final SesionActual actual;private final SesionRepository sesiones;
    public AutenticacionController(UnidadTrabajo t,ServicioAutenticacion a,ServicioAutorizacion p,SesionActual s,SesionRepository r){tx=t;acceso=a;permisos=p;actual=s;sesiones=r;}
    public record Login(@NotBlank @Size(max=40) String identificacion,@NotBlank @Size(max=72) String contrasena,@NotBlank @Size(max=120) String equipo){}
    public record Cambio(@NotBlank @Size(max=72) String actual,@NotBlank @Size(max=72) String nueva){}
    public record Recuperar(@NotBlank @Size(max=40) String identificacion){}
    public record Restablecer(@NotBlank @Size(max=100) String token,@NotBlank @Size(max=72) String nueva){}
    @PostMapping("/login") Object login(@Valid @RequestBody Login datos,HttpServletRequest req,HttpServletResponse res){
        byte[] random=new byte[32];new SecureRandom().nextBytes(random);String token=Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        Object resultado=tx.ejecutar(()->{Sesion s=acceso.iniciarSesion(datos.identificacion(),datos.contrasena(),datos.equipo(),req.getRemoteAddr());s.vincularToken(token);return Map.of("usuario",ResumenUsuario.de(s.usuario()));});
        res.addHeader("Set-Cookie",ResponseCookie.from("ACCESO",token).httpOnly(true).secure(req.isSecure()).sameSite("Strict").path("/").build().toString());return resultado;
    }
    @GetMapping({"/me","/estado"}) Object me(HttpServletRequest req){return tx.ejecutar(()->{Sesion s=actual.obtener(req);return Map.of("usuario",ResumenUsuario.de(s.usuario()),"sesion",s,"permisos",permisos.opcionesPermitidas(s.usuario(),Instant.now()));});}
    @PostMapping("/logout") Object logout(HttpServletRequest req,HttpServletResponse res){
        Object result=tx.ejecutar(()->{acceso.cerrarSesionActual(actual.obtener(req));return Map.of("mensaje","Sesión cerrada.");});
        res.addHeader("Set-Cookie",ResponseCookie.from("ACCESO","").httpOnly(true).sameSite("Strict").path("/").maxAge(0).build().toString());return result;
    }
    @PostMapping("/contrasena") Object cambiar(@Valid @RequestBody Cambio d,HttpServletRequest req){return tx.ejecutar(()->{acceso.cambiarContrasena(actual.obtener(req),d.actual(),d.nueva());return Map.of("mensaje","Contraseña actualizada.");});}
    @PostMapping("/recuperacion") Object recuperar(@Valid @RequestBody Recuperar d){return tx.ejecutar(()->{acceso.solicitarRecuperacion(d.identificacion());return Map.of("mensaje","Si la cuenta permite recuperación, recibirá un token en su correo registrado.");});}
    @PostMapping("/restablecer") Object restablecer(@Valid @RequestBody Restablecer d){return tx.ejecutar(()->{acceso.completarRecuperacion(d.token(),d.nueva());return Map.of("mensaje","Contraseña restablecida. Si existe un bloqueo, solicite el desbloqueo al administrador.");});}
    @GetMapping("/sesiones") Object sesiones(HttpServletRequest req){return tx.ejecutar(()->sesiones.findByUsuario(actual.obtener(req).usuario()));}
    @PostMapping("/sesiones/{id}/cerrar") Object cerrar(@PathVariable UUID id,HttpServletRequest req){return tx.ejecutar(()->{
        Sesion propia=actual.obtener(req);Sesion objetivo=sesiones.findById(id).orElseThrow(()->new AccesoException(404,"Sesión inexistente."));
        if(!propia.usuario().getIdUsuario().equals(objetivo.usuario().getIdUsuario()))throw new AccesoException(403,"La sesión no le pertenece.");
        acceso.cerrarSesionActual(objetivo);return Map.of("mensaje","Sesión cerrada.");
    });}
}
