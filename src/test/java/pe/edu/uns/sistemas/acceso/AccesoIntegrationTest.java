package pe.edu.uns.sistemas.acceso;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import java.time.Instant;
import pe.edu.uns.sistemas.acceso.domain.*;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
import pe.edu.uns.sistemas.acceso.repository.*;
import pe.edu.uns.sistemas.acceso.service.*;
import pe.edu.uns.sistemas.acceso.exception.AccesoException;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
    "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop","app.bootstrap-password=InicialSegura1!"})
class AccesoIntegrationTest {
    @Autowired UnidadTrabajo tx;
    @Autowired ServicioAutenticacion acceso;
    @Autowired ServicioAdministracion admin;
    @Autowired ServicioAutorizacion permisos;
    @Autowired UsuarioRepository usuarios;
    @Autowired SesionRepository sesiones;
    @Autowired AuditoriaRepository auditoria;
    @Autowired PoliticaRepository politicas;
    @Autowired RolRepository roles;
    @Autowired TestRestTemplate http;
    @Autowired PlatformTransactionManager manager;
    @MockitoBean JavaMailSender mail;
    private Usuario actor(){return usuarios.findByIdentificacion("admin").orElseThrow();}
    private String crear(){
        String id="u"+UUID.randomUUID().toString().substring(0,12);
        tx.ejecutar(()->admin.registrarUsuario(actor(),id,"Persona de prueba","persona@example.com",TipoPersona.ESTUDIANTE,false,"PrimeraSegura1!").getIdentificacion());
        return id;
    }
    @BeforeEach void politica(){tx.ejecutar(()->{politicas.findById(1L).orElseThrow().actualizar(3,20,15,5);return true;});}
    @Test void bloqueoPersistenteAuditoriaYDesbloqueo(){
        String id=crear();
        for(int i=0;i<3;i++)assertThrows(AccesoException.class,()->tx.ejecutar(()->acceso.iniciarSesion(id,"incorrecta","Equipo","127.0.0.1")));
        tx.ejecutar(()->{
            Usuario u=usuarios.findByIdentificacion(id).orElseThrow();
            assertEquals(EstadoCuenta.BLOQUEADA,u.cuenta().getEstado());
            assertEquals(3,u.cuenta().getFallosConsecutivos());
            assertEquals(3,auditoria.findAllByOrderByFechaHoraDesc().stream().filter(e->e instanceof IntentoAcceso && id.equals(e.getUsuarioObjetivo())).count());
            return true;
        });
        assertThrows(AccesoException.class,()->tx.ejecutar(()->acceso.iniciarSesion(id,"PrimeraSegura1!","Equipo","127.0.0.1")));
        tx.ejecutar(()->{admin.desbloquearCuenta(actor(),usuarios.findByIdentificacion(id).orElseThrow());return true;});
        tx.ejecutar(()->{acceso.iniciarSesion(id,"PrimeraSegura1!","Equipo","127.0.0.1");assertEquals(0,usuarios.findByIdentificacion(id).orElseThrow().cuenta().getFallosConsecutivos());return true;});
    }
    @Test void sesionesIndependientesExpiracionYBaja(){
        String id=crear();UUID[] ids=new UUID[2];
        tx.ejecutar(()->{ids[0]=acceso.iniciarSesion(id,"PrimeraSegura1!","Laptop","127.0.0.1").getIdSesion();ids[1]=acceso.iniciarSesion(id,"PrimeraSegura1!","Móvil","127.0.0.2").getIdSesion();return true;});
        tx.ejecutar(()->{acceso.cerrarSesionActual(sesiones.findById(ids[0]).orElseThrow());assertEquals(EstadoSesion.ACTIVA,sesiones.findById(ids[1]).orElseThrow().getEstado());return true;});
        tx.ejecutar(()->{acceso.cerrarPorInactividad(Instant.now().plusSeconds(1300));assertEquals(EstadoSesion.CERRADA,sesiones.findById(ids[1]).orElseThrow().getEstado());return true;});
        tx.ejecutar(()->{acceso.iniciarSesion(id,"PrimeraSegura1!","Nuevo","127.0.0.1");admin.deshabilitarCuenta(actor(),usuarios.findByIdentificacion(id).orElseThrow());assertTrue(sesiones.findByUsuario(usuarios.findByIdentificacion(id).orElseThrow()).stream().allMatch(s->s.getEstado()==EstadoSesion.CERRADA));return true;});
        assertThrows(AccesoException.class,()->tx.ejecutar(()->acceso.iniciarSesion(id,"PrimeraSegura1!","Equipo","127.0.0.1")));
    }
    @Test void historialYPolitica(){
        String id=crear();
        tx.ejecutar(()->{Sesion s=acceso.iniciarSesion(id,"PrimeraSegura1!","PC","127.0.0.1");acceso.cambiarContrasena(s,"PrimeraSegura1!","SegundaSegura2!");return true;});
        assertThrows(IllegalArgumentException.class,()->tx.ejecutar(()->{Sesion s=acceso.iniciarSesion(id,"SegundaSegura2!","PC","127.0.0.1");acceso.cambiarContrasena(s,"SegundaSegura2!","PrimeraSegura1!");return true;}));
        assertThrows(IllegalArgumentException.class,()->tx.ejecutar(()->{admin.configurarSeguridad(actor(),0,20,15,5);return true;}));
        tx.ejecutar(()->{assertEquals(3,politicas.findById(1L).orElseThrow().getMaxIntentos());return true;});
    }
    private String tokenPara(String id){
        List<String> tokens=new ArrayList<>();
        doAnswer(inv->{SimpleMailMessage m=inv.getArgument(0);tokens.add(m.getText().split("es: ")[1].split("\n")[0]);return null;}).when(mail).send(any(SimpleMailMessage.class));
        tx.ejecutar(()->{acceso.solicitarRecuperacion(id);return true;});
        return tokens.get(0);
    }
    @Test void tokenUnicoUsoInvalidacionYBloqueo(){
        String id=crear();String anterior=tokenPara(id);String vigente=tokenPara(id);
        assertThrows(IllegalArgumentException.class,()->tx.ejecutar(()->{acceso.completarRecuperacion(anterior,"TerceraSegura3!");return true;}));
        for(int i=0;i<3;i++)assertThrows(AccesoException.class,()->tx.ejecutar(()->acceso.iniciarSesion(id,"mal","PC","127.0.0.1")));
        tx.ejecutar(()->{acceso.completarRecuperacion(vigente,"TerceraSegura3!");assertEquals(EstadoCuenta.BLOQUEADA,usuarios.findByIdentificacion(id).orElseThrow().cuenta().getEstado());return true;});
        assertThrows(IllegalArgumentException.class,()->tx.ejecutar(()->{acceso.completarRecuperacion(vigente,"CuartaSegura4!");return true;}));
        tx.ejecutar(()->{TokenRecuperacion t=new TokenRecuperacion(usuarios.findByIdentificacion(id).orElseThrow(),"valor",1);assertFalse(t.validar("valor",Instant.now().plusSeconds(61)));admin.deshabilitarCuenta(actor(),usuarios.findByIdentificacion(id).orElseThrow());return true;});
        reset(mail);tx.ejecutar(()->{acceso.solicitarRecuperacion(id);return true;});verifyNoInteractions(mail);
    }
    @Test void rolesVigentesYRestriccionAdministrativa(){
        String id=crear();
        assertThrows(AccesoException.class,()->tx.ejecutar(()->admin.buscarUsuario(usuarios.findByIdentificacion(id).orElseThrow(),"")));
        assertThrows(IllegalArgumentException.class,()->tx.ejecutar(()->{admin.asignarRol(actor(),usuarios.findByIdentificacion(id).orElseThrow(),roles.findByNombre("ADMINISTRADOR").orElseThrow().getIdRol(),Instant.now(),null);return true;}));
        tx.ejecutar(()->{
            Usuario u=usuarios.findByIdentificacion(id).orElseThrow();
            admin.asignarRol(actor(),u,roles.findByNombre("DOCENTE").orElseThrow().getIdRol(),Instant.now().plusSeconds(3600),null);
            assertTrue(u.rolesVigentes(Instant.now()).isEmpty());
            AsignacionRol a=u.getAsignaciones().get(0);admin.modificarVigencia(actor(),u,a.getIdAsignacion(),Instant.now().minusSeconds(10),null);
            assertTrue(permisos.opcionesPermitidas(u,Instant.now()).contains("SERVICIOS_DOCENTE"));
            admin.retirarRol(actor(),u,a.getIdAsignacion());assertTrue(u.rolesVigentes(Instant.now()).isEmpty());return true;
        });
    }
    @Test void busquedaNoModificaObjetivoYAuditoriaFiltra(){
        String id=crear();
        tx.ejecutar(()->{Usuario u=usuarios.findByIdentificacion(id).orElseThrow();admin.modificarDatos(actor(),u,"Nombre actualizado","nuevo@example.com");
            assertEquals(1,admin.buscarUsuario(actor(),id).size());
            assertTrue(u.getAsignaciones().isEmpty());
            assertFalse(new ServicioAuditoria(auditoria,permisos).consultar(actor(),id,null,null,null,null).isEmpty());return true;});
    }
    @Test void httpRequiereSesionCsrfYNoExponeSecretos(){
        assertEquals(HttpStatus.UNAUTHORIZED,http.getForEntity("/api/admin/usuarios",String.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,http.postForEntity("/api/login",Map.of("identificacion","admin","contrasena","InicialSegura1!","equipo","Test"),String.class).getStatusCode());
        ResponseEntity<JsonNode> csrf=http.getForEntity("/api/csrf",JsonNode.class);
        HttpHeaders h=new HttpHeaders();h.setContentType(MediaType.APPLICATION_JSON);
        h.set(csrf.getBody().get("headerName").asText(),csrf.getBody().get("token").asText());
        h.set("Cookie",csrf.getHeaders().getFirst("Set-Cookie").split(";")[0]);
        ResponseEntity<String> login=http.exchange("/api/login",HttpMethod.POST,new HttpEntity<>(Map.of("identificacion","admin","contrasena","InicialSegura1!","equipo","HTTP"),h),String.class);
        assertEquals(HttpStatus.OK,login.getStatusCode(),login.getBody());
        h.set("Cookie",login.getHeaders().getFirst("Set-Cookie").split(";")[0]);
        ResponseEntity<String> profile=http.exchange("/api/me",HttpMethod.GET,new HttpEntity<>(h),String.class);
        assertEquals(HttpStatus.OK,profile.getStatusCode());
        assertFalse(profile.getBody().contains("hash"));assertFalse(profile.getBody().contains("credencial"));assertFalse(profile.getBody().contains("InicialSegura"));
        String content=http.getForObject("/",String.class);assertTrue(content.contains("Acceso institucional"));
    }
    @Test void datosPersistenEntreTransacciones(){
        String id=crear();
        new TransactionTemplate(manager).execute(status->{assertTrue(usuarios.findByIdentificacion(id).isPresent());return null;});
    }
}

