package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.util.UUID;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
@Entity
public class CierreSesion extends RegistroAuditoria {
    @OneToOne @JoinColumn(name="sesion_cerrada_id",unique=true) private Sesion sesion;
    @Enumerated(EnumType.STRING) private MotivoCierre motivo;
    protected CierreSesion(){}
    public CierreSesion(Sesion sesion,MotivoCierre motivo){super(sesion.usuario().getIdentificacion(),sesion.getEquipo());vincularObjetivo(sesion.usuario());this.sesion=sesion;this.motivo=motivo;}
    public UUID getSesion(){return sesion.getIdSesion();}
    public MotivoCierre getMotivo(){return motivo;}
}
