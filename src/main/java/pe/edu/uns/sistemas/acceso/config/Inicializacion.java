package pe.edu.uns.sistemas.acceso.config;
import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.*;
import java.time.Instant;
import pe.edu.uns.sistemas.acceso.domain.*;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
import pe.edu.uns.sistemas.acceso.repository.*;
import pe.edu.uns.sistemas.acceso.service.*;
@Component
public class Inicializacion implements ApplicationRunner {
    private final UnidadTrabajo tx;private final UsuarioRepository usuarios;private final RolRepository roles;private final PermisoRepository permisos;private final PoliticaRepository politicas;private final ServicioAutenticacion acceso;private final String password;
    public Inicializacion(UnidadTrabajo t,UsuarioRepository u,RolRepository r,PermisoRepository p,PoliticaRepository po,ServicioAutenticacion a,@Value("${app.bootstrap-password}") String password){tx=t;usuarios=u;roles=r;permisos=p;politicas=po;acceso=a;this.password=password;}
    public void run(ApplicationArguments args){
        tx.ejecutar(()->{
            if(!politicas.existsById(1L))politicas.save(new PoliticaSeguridad());
            crearRol("ESTUDIANTE",Map.of("SERVICIOS_ESTUDIANTE","Servicios estudiantiles"));
            crearRol("DOCENTE",Map.of("SERVICIOS_DOCENTE","Servicios docentes"));
            crearRol("ADMINISTRATIVO",Map.of("SERVICIOS_ADMINISTRATIVO","Servicios administrativos"));
            crearRol("ADMINISTRADOR",Map.of("ADMINISTRAR","Administrar usuarios y acceso","AUDITAR","Consultar auditoría"));
            crearRol("SEGURIDAD",Map.of("AUDITAR","Consultar auditoría","CONFIGURAR","Configurar política de seguridad"));
            if(usuarios.count()==0){
                if(password.isBlank())throw new IllegalArgumentException("Primer inicio: configure ADMIN_PASSWORD con una contraseña segura (8–72 caracteres, mayúscula, minúscula, número y símbolo).");
                Usuario admin=new Usuario("admin","Administrador del sistema","admin@universidad.edu.pe",TipoPersona.ADMINISTRATIVO,true,acceso.inicializarCredencial(password));
                admin.asignar(new AsignacionRol(roles.findByNombre("ADMINISTRADOR").orElseThrow(),Instant.now(),null));
                admin.asignar(new AsignacionRol(roles.findByNombre("SEGURIDAD").orElseThrow(),Instant.now(),null));usuarios.save(admin);
            }
            return true;
        });
    }
    private void crearRol(String nombre,Map<String,String> codigos){
        if(roles.findByNombre(nombre).isPresent())return;
        Set<Permiso> lista=new HashSet<>();codigos.forEach((c,d)->lista.add(permisos.findById(c).orElseGet(()->permisos.save(new Permiso(c,d)))));roles.save(new Rol(nombre,lista));
    }
    @Scheduled(initialDelay=60000,fixedDelay=30000)
    public void expirar(){tx.ejecutar(()->{acceso.cerrarPorInactividad(Instant.now());return true;});}
}
