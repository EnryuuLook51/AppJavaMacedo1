package pe.edu.uns.sistemas.acceso.service;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import pe.edu.uns.sistemas.acceso.domain.*;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
import pe.edu.uns.sistemas.acceso.exception.AccesoException;
@Service
public class ServicioAutorizacion {
    public List<Rol> rolesVigentes(Usuario usuario,Instant fecha){return usuario.rolesVigentes(fecha);}
    public Set<String> opcionesPermitidas(Usuario usuario,Instant fecha){
        Set<String> permisos=new TreeSet<>();rolesVigentes(usuario,fecha).forEach(r->r.getPermisos().forEach(p->permisos.add(p.getCodigo())));return permisos;
    }
    public void exigirPermiso(Usuario actor,String codigo){
        if(!opcionesPermitidas(actor,Instant.now()).contains(codigo))throw new AccesoException(403,"No cuenta con el permiso requerido.");
    }
    public void exigirAdministracion(Usuario actor){exigirPermiso(actor,"ADMINISTRAR");}
    public void validarAsignacion(Usuario usuario,Rol rol){
        if((rol.tienePermiso("ADMINISTRAR") || rol.tienePermiso("CONFIGURAR")) &&
           (usuario.getTipoPersona()!=TipoPersona.ADMINISTRATIVO || !usuario.isAdministrativoAutorizado()))
            throw new IllegalArgumentException("Este rol requiere un trabajador administrativo expresamente autorizado.");
    }
}
