package pe.edu.uns.sistemas.acceso.web.controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import pe.edu.uns.sistemas.acceso.service.*;
import pe.edu.uns.sistemas.acceso.web.SesionActual;
@RestController @RequestMapping("/api/auditoria")
public class AuditoriaController {
    private final UnidadTrabajo tx;private final ServicioAuditoria auditoria;private final SesionActual actual;
    public AuditoriaController(UnidadTrabajo t,ServicioAuditoria a,SesionActual s){tx=t;auditoria=a;actual=s;}
    @GetMapping Object consultar(@RequestParam(required=false) String usuario,@RequestParam(required=false) String resultado,@RequestParam(required=false) String contexto,@RequestParam(required=false) Instant desde,@RequestParam(required=false) Instant hasta,HttpServletRequest req){
        return tx.ejecutar(()->auditoria.consultar(actual.obtener(req).usuario(),usuario,resultado,contexto,desde,hasta));
    }
}
