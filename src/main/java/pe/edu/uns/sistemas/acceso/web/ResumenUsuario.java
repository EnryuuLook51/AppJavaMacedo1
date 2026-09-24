package pe.edu.uns.sistemas.acceso.web;
import pe.edu.uns.sistemas.acceso.domain.*;
import java.time.Instant;
import java.util.*;
public record ResumenUsuario(UUID idUsuario,String identificacion,String nombres,String medioContacto,
    String tipoPersona,boolean administrativoAutorizado,String estado,int fallosConsecutivos,
    List<String> rolesVigentes,List<AsignacionRol> asignaciones) {
    public static ResumenUsuario de(Usuario u){
        return new ResumenUsuario(u.getIdUsuario(),u.getIdentificacion(),u.getNombres(),u.getMedioContacto(),u.getTipoPersona().name(),
            u.isAdministrativoAutorizado(),u.cuenta().getEstado().name(),u.cuenta().getFallosConsecutivos(),
            u.rolesVigentes(Instant.now()).stream().map(Rol::getNombre).toList(),u.getAsignaciones());
    }
}
