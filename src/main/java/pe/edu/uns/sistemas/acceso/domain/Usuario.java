package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.util.*;
import java.time.Instant;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
@Entity
public class Usuario {
    @Id private UUID idUsuario=UUID.randomUUID();
    @Column(unique=true,nullable=false) private String identificacion;
    private String nombres;
    private String medioContacto;
    @Enumerated(EnumType.STRING) private TipoPersona tipoPersona;
    private boolean administrativoAutorizado;
    @OneToOne(cascade=CascadeType.ALL,optional=false) private CuentaAcceso cuenta;
    @OneToMany(cascade=CascadeType.ALL) @JoinColumn(name="usuario_id") private List<AsignacionRol> asignaciones=new ArrayList<>();
    protected Usuario() {}
    public Usuario(String identificacion,String nombres,String contacto,TipoPersona tipo,boolean autorizado,CuentaAcceso cuenta) {
        if(identificacion==null || !identificacion.matches("[a-zA-Z0-9._-]{3,40}")) throw new IllegalArgumentException("Identificación inválida (3–40 letras, números, punto o guion).");
        if(tipo==null || (autorizado && tipo!=TipoPersona.ADMINISTRATIVO)) throw new IllegalArgumentException("Solo un trabajador administrativo puede ser autorizado.");
        this.identificacion=identificacion; tipoPersona=tipo; administrativoAutorizado=autorizado; this.cuenta=cuenta; actualizarDatosBasicos(nombres,contacto);
    }
    public void actualizarDatosBasicos(String nombres,String contacto) {
        if(nombres==null || nombres.isBlank() || nombres.length()>120 || contacto==null || contacto.length()>200 || !contacto.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new IllegalArgumentException("Ingrese nombres y un correo electrónico válido.");
        this.nombres=nombres.trim(); medioContacto=contacto.trim();
    }
    public UUID getIdUsuario(){return idUsuario;}
    public String getIdentificacion(){return identificacion;}
    public String getNombres(){return nombres;}
    public String getMedioContacto(){return medioContacto;}
    public TipoPersona getTipoPersona(){return tipoPersona;}
    public boolean isAdministrativoAutorizado(){return administrativoAutorizado;}
    public CuentaAcceso cuenta(){return cuenta;}
    public List<AsignacionRol> getAsignaciones(){return List.copyOf(asignaciones);}
    public void asignar(AsignacionRol asignacion){asignaciones.add(asignacion);}
    public List<Rol> rolesVigentes(Instant fecha){return asignaciones.stream().filter(a->a.esVigente(fecha)).map(AsignacionRol::getRol).distinct().toList();}
}
