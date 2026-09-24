package pe.edu.uns.sistemas.acceso.web;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.*;
import java.util.*;
import pe.edu.uns.sistemas.acceso.domain.*;
import pe.edu.uns.sistemas.acceso.repository.SesionRepository;
import pe.edu.uns.sistemas.acceso.service.ServicioAutenticacion;
import pe.edu.uns.sistemas.acceso.exception.AccesoException;
@Component
public class SesionActual {
    private final SesionRepository sesiones;
    private final ServicioAutenticacion acceso;
    public SesionActual(SesionRepository s,ServicioAutenticacion a){sesiones=s;acceso=a;}
    public Sesion obtener(HttpServletRequest request){
        String valor=Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
            .filter(c->c.getName().equals("ACCESO")).map(Cookie::getValue).findFirst().orElse("");
        Sesion sesion=sesiones.findByHashAutenticacion(TokenRecuperacion.hash(valor)).orElseThrow(()->new AccesoException(401,"Inicie sesión para continuar."));
        return acceso.validarSesion(sesion.getIdSesion(),!request.getRequestURI().endsWith("/estado"));
    }
}
