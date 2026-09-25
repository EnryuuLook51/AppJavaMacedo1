package pe.edu.uns.sistemas.acceso.service;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import pe.edu.uns.sistemas.acceso.domain.*;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
import pe.edu.uns.sistemas.acceso.repository.*;
@Service
public class ServicioAuditoria {
    private final AuditoriaRepository registros;
    private final ServicioAutorizacion autorizacion;
    public ServicioAuditoria(AuditoriaRepository r,ServicioAutorizacion a){registros=r;autorizacion=a;}
    public void registrarIntento(Usuario usuario,String id,ResultadoAcceso resultado,String motivo,String equipo,String red){
        IntentoAcceso evento=new IntentoAcceso(id,resultado,motivo,equipo,red);evento.vincularObjetivo(usuario);registros.save(evento);
    }
    public void registrarIntervencion(Usuario actor,Usuario objetivo,String accion,String detalle){registros.save(new IntervencionAdministrativa(actor,objetivo,accion,detalle));}
    public void registrarSolicitudRecuperacion(String id,Usuario usuario,String canal,ResultadoSolicitud resultado,String motivo,TokenRecuperacion token){
        registros.save(new SolicitudRecuperacion(id,usuario,canal,resultado,motivo,token));
    }
    public void registrarCierre(Sesion sesion,MotivoCierre motivo){registros.save(new CierreSesion(sesion,motivo));}
    public List<RegistroAuditoria> consultar(Usuario actor,String usuario,String resultado,String contexto,Instant desde,Instant hasta){
        autorizacion.exigirPermiso(actor,"AUDITAR");
        if(desde!=null && hasta!=null && desde.isAfter(hasta))throw new IllegalArgumentException("El rango de fechas es inválido.");
        return registros.findAllByOrderByFechaHoraDesc().stream()
            .filter(r->usuario==null || usuario.isBlank() || (r.getUsuarioObjetivo()!=null && r.getUsuarioObjetivo().toLowerCase().contains(usuario.toLowerCase())))
            .filter(r->resultado==null || resultado.isBlank() || (r instanceof IntentoAcceso i && i.getResultado().name().equals(resultado)) || (r instanceof SolicitudRecuperacion s && s.getResultado().name().equals(resultado)))
            .filter(r->contexto==null || contexto.isBlank() || r.getContexto().toLowerCase().contains(contexto.toLowerCase()))
            .filter(r->desde==null || !r.getFechaHora().isBefore(desde)).filter(r->hasta==null || !r.getFechaHora().isAfter(hasta)).toList();
    }
}
