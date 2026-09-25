package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
@Entity
public class Sesion {
    @Id private UUID idSesion=UUID.randomUUID();
    @ManyToOne(optional=false) private CuentaAcceso cuenta;
    private String equipo;
    @Column(unique=true) private String hashAutenticacion;
    private String direccionRed;
    private Instant iniciadaEn=Instant.now();
    private Instant ultimaActividad=iniciadaEn;
    private Instant cerradaEn;
    @Enumerated(EnumType.STRING) private EstadoSesion estado=EstadoSesion.ACTIVA;
    protected Sesion() {}
    public Sesion(Usuario usuario,String equipo,String red){this.cuenta=usuario.cuenta();this.equipo=equipo;direccionRed=red;}
    public UUID getIdSesion(){return idSesion;}
    public void vincularToken(String token){hashAutenticacion=TokenRecuperacion.hash(token);}
    public Usuario usuario(){return cuenta.usuario();}
    public String getEquipo(){return equipo;}
    public String getDireccionRed(){return direccionRed;}
    public Instant getIniciadaEn(){return iniciadaEn;}
    public Instant getUltimaActividad(){return ultimaActividad;}
    public Instant getCerradaEn(){return cerradaEn;}
    public EstadoSesion getEstado(){return estado;}
    public void registrarActividad(Instant ahora){if(estado==EstadoSesion.ACTIVA) ultimaActividad=ahora;}
    public boolean cerrar(){if(estado==EstadoSesion.CERRADA)return false;estado=EstadoSesion.CERRADA;cerradaEn=Instant.now();return true;}
    public boolean haExpirado(Instant ahora,int minutos){return estado==EstadoSesion.ACTIVA && !ahora.isBefore(ultimaActividad.plus(Duration.ofMinutes(minutos)));}
}
