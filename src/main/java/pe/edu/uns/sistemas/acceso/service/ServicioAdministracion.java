package pe.edu.uns.sistemas.acceso.service;
import org.springframework.stereotype.Service;
import java.util.*;
import java.time.Instant;
import pe.edu.uns.sistemas.acceso.domain.*;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
import pe.edu.uns.sistemas.acceso.repository.*;
import pe.edu.uns.sistemas.acceso.exception.AccesoException;
@Service
public class ServicioAdministracion {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PoliticaRepository politicas;
    private final IControlAcceso acceso;
    private final ServicioAutorizacion autorizacion;
    private final ServicioAuditoria auditoria;
    public ServicioAdministracion(UsuarioRepository u,RolRepository r,PoliticaRepository p,IControlAcceso c,ServicioAutorizacion a,ServicioAuditoria au){usuarios=u;roles=r;politicas=p;acceso=c;autorizacion=a;auditoria=au;}
    public Usuario registrarUsuario(Usuario actor,String id,String nombres,String contacto,TipoPersona tipo,boolean autorizado,String clave){
        autorizacion.exigirAdministracion(actor);
        if(usuarios.findByIdentificacion(id).isPresent())throw new AccesoException(409,"La identificación ya está registrada.");
        Usuario usuario=usuarios.save(new Usuario(id,nombres,contacto,tipo,autorizado,acceso.inicializarCredencial(clave)));
        auditoria.registrarIntervencion(actor,usuario,"REGISTRAR_USUARIO","Registro sin roles asignados");return usuario;
    }
    public List<Usuario> buscarUsuario(Usuario actor,String criterio){
        autorizacion.exigirAdministracion(actor);String q=criterio.toLowerCase();
        return usuarios.findAll().stream().filter(u->u.getIdentificacion().toLowerCase().contains(q)||u.getNombres().toLowerCase().contains(q)).toList();
    }
    public Usuario obtener(Usuario actor,UUID id){autorizacion.exigirAdministracion(actor);return usuarios.findById(id).orElseThrow(()->new AccesoException(404,"Usuario no encontrado."));}
    public void modificarDatos(Usuario actor,Usuario usuario,String nombres,String contacto){autorizacion.exigirAdministracion(actor);usuario.actualizarDatosBasicos(nombres,contacto);auditoria.registrarIntervencion(actor,usuario,"MODIFICAR_DATOS","Datos básicos actualizados");}
    public void cerrarSesion(Usuario actor,Sesion sesion){autorizacion.exigirAdministracion(actor);acceso.cerrarSesionAdministrativa(sesion);auditoria.registrarIntervencion(actor,sesion.usuario(),"CERRAR_SESION",sesion.getIdSesion().toString());}
    public void recuperarAcceso(Usuario actor,Usuario usuario){autorizacion.exigirAdministracion(actor);acceso.iniciarRecuperacion(usuario);auditoria.registrarIntervencion(actor,usuario,"RECUPERAR_ACCESO","Token enviado al medio de contacto registrado");}
    public void desbloquearCuenta(Usuario actor,Usuario usuario){autorizacion.exigirAdministracion(actor);acceso.desbloquearCuenta(usuario.cuenta());auditoria.registrarIntervencion(actor,usuario,"DESBLOQUEAR_CUENTA","Contador reiniciado");}
    public void deshabilitarCuenta(Usuario actor,Usuario usuario){
        autorizacion.exigirAdministracion(actor);
        if(actor.getIdUsuario().equals(usuario.getIdUsuario()))throw new IllegalArgumentException("No puede deshabilitar su propia cuenta.");
        acceso.deshabilitarCuenta(usuario);auditoria.registrarIntervencion(actor,usuario,"DESHABILITAR_CUENTA","Sesiones cerradas y tokens invalidados");
    }
    public void asignarRol(Usuario actor,Usuario usuario,UUID rolId,Instant inicio,Instant fin){
        autorizacion.exigirAdministracion(actor);Rol rol=roles.findById(rolId).orElseThrow(()->new IllegalArgumentException("Rol inexistente."));
        autorizacion.validarAsignacion(usuario,rol);
        AsignacionRol nueva=new AsignacionRol(rol,inicio,fin);
        validarPeriodoDisponible(usuario,rolId,null,inicio,fin);
        usuario.asignar(nueva);auditoria.registrarIntervencion(actor,usuario,"ASIGNAR_ROL",rol.getNombre());
    }
    private void validarPeriodoDisponible(Usuario usuario,UUID rolId,UUID excluida,Instant inicio,Instant fin){
        if(usuario.getAsignaciones().stream().anyMatch(a->a.getRol().getIdRol().equals(rolId)&&a.getRetiradaEn()==null &&
            !a.getIdAsignacion().equals(excluida) &&
            (fin==null || a.getInicioVigencia().isBefore(fin)) && (a.getFinVigencia()==null || inicio.isBefore(a.getFinVigencia()))))
            throw new IllegalArgumentException("Ya existe una asignación de este rol en ese período.");
    }
    private AsignacionRol asignacion(Usuario usuario,UUID id){return usuario.getAsignaciones().stream().filter(a->a.getIdAsignacion().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Asignación inexistente."));}
    public void modificarVigencia(Usuario actor,Usuario usuario,UUID id,Instant inicio,Instant fin){
        autorizacion.exigirAdministracion(actor);AsignacionRol a=asignacion(usuario,id);
        new AsignacionRol(a.getRol(),inicio,fin); // Valida fechas antes de comprobar solapamientos.
        validarPeriodoDisponible(usuario,a.getRol().getIdRol(),id,inicio,fin);
        a.modificarVigencia(inicio,fin);auditoria.registrarIntervencion(actor,usuario,"MODIFICAR_VIGENCIA",a.getRol().getNombre());
    }
    public void retirarRol(Usuario actor,Usuario usuario,UUID id){autorizacion.exigirAdministracion(actor);AsignacionRol a=asignacion(usuario,id);a.retirar(Instant.now());auditoria.registrarIntervencion(actor,usuario,"RETIRAR_ROL",a.getRol().getNombre());}
    public void configurarSeguridad(Usuario actor,int intentos,int inactividad,int token,int historicas){
        autorizacion.exigirPermiso(actor,"CONFIGURAR");politicas.findById(1L).orElseThrow().actualizar(intentos,inactividad,token,historicas);
        auditoria.registrarIntervencion(actor,null,"CONFIGURAR_SEGURIDAD","Intentos="+intentos+", inactividad="+inactividad+", token="+token+", históricas="+historicas);
    }
}
