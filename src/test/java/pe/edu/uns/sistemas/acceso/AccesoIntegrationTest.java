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
    @Autowired TokenRepository tokens;
    @Autowired GestorExpiracionSesiones temporizador;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
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
    @Test void recuperacionAuditaCanalesRechazosYFalloDeCorreoSinInvalidarTokenAnterior(){
        String id=crear();String vigente=tokenPara(id);
        tx.ejecutar(()->{
            SolicitudRecuperacion solicitud=auditoria.findAllByOrderByFechaHoraDesc().stream()
                .filter(e->e instanceof SolicitudRecuperacion && id.equals(e.getUsuarioObjetivo()))
                .map(SolicitudRecuperacion.class::cast).findFirst().orElseThrow();
            assertEquals(ResultadoSolicitud.ENVIADA,solicitud.getResultado());
            assertEquals("AUTOGESTION",solicitud.getCanal());
            assertNotNull(solicitud.getTokenGenerado());return true;
        });
        doThrow(new org.springframework.mail.MailSendException("SMTP no disponible")).when(mail).send(any(SimpleMailMessage.class));
        AccesoException error=assertThrows(AccesoException.class,()->tx.ejecutar(()->{acceso.solicitarRecuperacion(id);return true;}));
        assertEquals(503,error.getStatus());
        tx.ejecutar(()->{
            assertEquals(1,tokens.findByUsuario(usuarios.findByIdentificacion(id).orElseThrow()).size());
            assertEquals(1,new ServicioAuditoria(auditoria,permisos).consultar(actor(),id,"ERROR_ENVIO",null,null,null).size());
            acceso.completarRecuperacion(vigente,"RecuperadaSegura2!");return true;
        });
        reset(mail);
        tx.ejecutar(()->{admin.recuperarAcceso(actor(),usuarios.findByIdentificacion(id).orElseThrow());return true;});
        tx.ejecutar(()->{
            assertTrue(auditoria.findAllByOrderByFechaHoraDesc().stream().anyMatch(e->e instanceof SolicitudRecuperacion s && id.equals(s.getUsuarioObjetivo()) && s.getCanal().equals("ADMINISTRACION")));
            acceso.solicitarRecuperacion("inexistente-"+id);
            assertEquals(1,new ServicioAuditoria(auditoria,permisos).consultar(actor(),"inexistente-"+id,"RECHAZADA",null,null,null).size());
            return true;
        });
    }
    @Test void temporizadorCierraSoloSesionVencidaYRegistraUnSoloCierre(){
        String id=crear();UUID[] ids=new UUID[2];Instant ahora=Instant.now();
        tx.ejecutar(()->{
            Sesion antigua=acceso.iniciarSesion(id,"PrimeraSegura1!","Antiguo","127.0.0.1");
            antigua.registrarActividad(ahora.minusSeconds(1300));ids[0]=antigua.getIdSesion();
            ids[1]=acceso.iniciarSesion(id,"PrimeraSegura1!","Activo","127.0.0.2").getIdSesion();return true;
        });
        temporizador.ejecutar(ahora);temporizador.ejecutar(ahora);
        tx.ejecutar(()->{
            assertEquals(EstadoSesion.CERRADA,sesiones.findById(ids[0]).orElseThrow().getEstado());
            assertEquals(EstadoSesion.ACTIVA,sesiones.findById(ids[1]).orElseThrow().getEstado());
            assertEquals(1,auditoria.findAllByOrderByFechaHoraDesc().stream().filter(e->e instanceof CierreSesion c && c.getSesion().equals(ids[0]) && c.getMotivo()==MotivoCierre.INACTIVIDAD).count());
            return true;
        });
    }
    @Test void editarVigenciaRechazaSolapamientoYConservaPeriodoOriginal(){
        String id=crear();Instant inicio=Instant.now();UUID[] asignaciones=new UUID[2];
        tx.ejecutar(()->{
            Usuario u=usuarios.findByIdentificacion(id).orElseThrow();UUID rol=roles.findByNombre("ESTUDIANTE").orElseThrow().getIdRol();
            admin.asignarRol(actor(),u,rol,inicio,inicio.plusSeconds(60));
            admin.asignarRol(actor(),u,rol,inicio.plusSeconds(120),null);
            asignaciones[0]=u.getAsignaciones().get(0).getIdAsignacion();asignaciones[1]=u.getAsignaciones().get(1).getIdAsignacion();return true;
        });
        assertThrows(IllegalArgumentException.class,()->tx.ejecutar(()->{admin.modificarVigencia(actor(),usuarios.findByIdentificacion(id).orElseThrow(),asignaciones[1],inicio.plusSeconds(30),null);return true;}));
        tx.ejecutar(()->{
            AsignacionRol segunda=usuarios.findByIdentificacion(id).orElseThrow().getAsignaciones().stream().filter(a->a.getIdAsignacion().equals(asignaciones[1])).findFirst().orElseThrow();
            assertEquals(inicio.plusSeconds(120).getEpochSecond(),segunda.getInicioVigencia().getEpochSecond());return true;
        });
    }
    @Test void relacionesPersistidasVinculanCuentaSesionTokenYAuditoria(){
        String id=crear();tokenPara(id);
        tx.ejecutar(()->{Sesion s=acceso.iniciarSesion(id,"PrimeraSegura1!","Relaciones","127.0.0.1");acceso.cerrarSesionActual(s);return true;});
        Integer relaciones=jdbc.queryForObject("""
            select count(*) from usuario u
            join cuenta_acceso c on c.id_cuenta=u.cuenta_id_cuenta
            join sesion s on s.cuenta_id_cuenta=c.id_cuenta
            join token_recuperacion t on t.cuenta_id_cuenta=c.id_cuenta
            join registro_auditoria cierre on cierre.sesion_cerrada_id=s.id_sesion
            join registro_auditoria solicitud on solicitud.token_generado_id=t.id_token
            where u.identificacion=?
            """,Integer.class,id);
        assertEquals(1,relaciones);
    }
}
