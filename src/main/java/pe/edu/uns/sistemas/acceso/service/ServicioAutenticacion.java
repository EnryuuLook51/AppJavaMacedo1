package pe.edu.uns.sistemas.acceso.service;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.util.*;
import java.security.SecureRandom;
import pe.edu.uns.sistemas.acceso.domain.*;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
import pe.edu.uns.sistemas.acceso.repository.*;
import pe.edu.uns.sistemas.acceso.exception.AccesoException;
@Service
public class ServicioAutenticacion implements IControlAcceso {
    private final UsuarioRepository usuarios;
    private final SesionRepository sesiones;
    private final PoliticaRepository politicas;
    private final TokenRepository tokens;
    private final PasswordEncoder encoder;
    private final ServicioAuditoria auditoria;
    private final JavaMailSender mail;
    private final String remitente;
    public ServicioAutenticacion(UsuarioRepository u,SesionRepository s,PoliticaRepository p,TokenRepository t,PasswordEncoder e,ServicioAuditoria a,JavaMailSender mail,@Value("${app.mail-from}") String from){
        usuarios=u;sesiones=s;politicas=p;tokens=t;encoder=e;auditoria=a;this.mail=mail;remitente=from;
    }
    public PoliticaSeguridad politica(){return politicas.findById(1L).orElseThrow();}
    public CuentaAcceso inicializarCredencial(String secreto){return new CuentaAcceso(new Credencial(secreto,politica(),encoder));}
    public Sesion iniciarSesion(String id,String secreto,String equipo,String red){
        Usuario usuario=usuarios.findByIdentificacion(id).orElse(null);
        String motivo=null;
        if(usuario==null)motivo="Credenciales incorrectas.";
        else if(!usuario.cuenta().puedeIniciarSesion())motivo="Cuenta "+usuario.cuenta().getEstado().name().toLowerCase()+". Contacte con un administrador.";
        else if(!usuario.cuenta().credencial().verificar(secreto,encoder)){
            usuario.cuenta().registrarFallo(politica().getMaxIntentos());
            motivo=usuario.cuenta().getEstado()==EstadoCuenta.BLOQUEADA?"Cuenta bloqueada por intentos fallidos.":"Credenciales incorrectas.";
        }
        auditoria.registrarIntento(id,motivo==null?ResultadoAcceso.EXITOSO:ResultadoAcceso.RECHAZADO,motivo,equipo,red);
        if(motivo!=null)throw new AccesoException(401,motivo);
        usuario.cuenta().reiniciarFallos();
        return sesiones.save(new Sesion(usuario,equipo,red));
    }
    public Sesion validarSesion(UUID id,boolean actividad){
        Sesion sesion=sesiones.findById(id).orElseThrow(()->new AccesoException(401,"Inicie sesión para continuar."));
        if(sesion.haExpirado(Instant.now(),politica().getInactividadMaxima()))cerrar(sesion,MotivoCierre.INACTIVIDAD);
        if(sesion.getEstado()!=EstadoSesion.ACTIVA || sesion.usuario().cuenta().getEstado()==EstadoCuenta.DESHABILITADA)
            throw new AccesoException(401,"La sesión terminó. Inicie sesión nuevamente.");
        if(actividad)sesion.registrarActividad(Instant.now());
        return sesion;
    }
    private void cerrar(Sesion sesion,MotivoCierre motivo){if(sesion.cerrar())auditoria.registrarCierre(sesion,motivo);}
    public void cerrarSesionActual(Sesion sesion){cerrar(sesion,MotivoCierre.VOLUNTARIO);}
    public void cerrarSesionAdministrativa(Sesion sesion){cerrar(sesion,MotivoCierre.ADMINISTRACION);}
    public void cerrarPorInactividad(Instant ahora){
        sesiones.findByEstado(EstadoSesion.ACTIVA).stream().filter(s->s.haExpirado(ahora,politica().getInactividadMaxima())).forEach(s->cerrar(s,MotivoCierre.INACTIVIDAD));
    }
    public void cambiarContrasena(Sesion sesion,String actual,String nueva){sesion.usuario().cuenta().credencial().cambiar(actual,nueva,politica(),encoder);}
    public void solicitarRecuperacion(String id){usuarios.findByIdentificacion(id).filter(u->u.cuenta().getEstado()!=EstadoCuenta.DESHABILITADA).ifPresent(this::iniciarRecuperacion);}
    public void iniciarRecuperacion(Usuario usuario){
        if(usuario.cuenta().getEstado()==EstadoCuenta.DESHABILITADA)throw new IllegalArgumentException("Una cuenta deshabilitada no puede recuperar acceso.");
        byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);
        String valor=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        SimpleMailMessage mensaje=new SimpleMailMessage();
        mensaje.setFrom(remitente);mensaje.setTo(usuario.getMedioContacto());mensaje.setSubject("Recuperación de acceso institucional");
        mensaje.setText("Su token de recuperación es: "+valor+"\nVigencia: "+politica().getVigenciaToken()+" minutos. Es de un solo uso. Si la cuenta está bloqueada, un administrador debe desbloquearla.");
        mail.send(mensaje);
        tokens.findByUsuario(usuario).forEach(TokenRecuperacion::invalidar);
        tokens.save(new TokenRecuperacion(usuario,valor,politica().getVigenciaToken()));
    }
    public void completarRecuperacion(String valor,String nueva){
        TokenRecuperacion token=tokens.findByHashToken(TokenRecuperacion.hash(valor)).orElseThrow(()->new IllegalArgumentException("Token inválido o expirado."));
        if(!token.validar(valor,Instant.now()) || token.usuario().cuenta().getEstado()==EstadoCuenta.DESHABILITADA)throw new IllegalArgumentException("Token inválido o expirado.");
        token.usuario().cuenta().credencial().restablecer(nueva,politica(),encoder);token.consumir();
    }
    public void desbloquearCuenta(CuentaAcceso cuenta){cuenta.desbloquear();}
    public void deshabilitarCuenta(Usuario usuario){
        usuario.cuenta().deshabilitar();sesiones.findByUsuario(usuario).forEach(this::cerrarSesionAdministrativa);tokens.findByUsuario(usuario).forEach(TokenRecuperacion::invalidar);
    }
}
