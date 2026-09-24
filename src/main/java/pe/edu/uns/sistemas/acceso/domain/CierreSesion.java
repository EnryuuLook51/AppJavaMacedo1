package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.util.UUID;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
@Entity
public class CierreSesion extends RegistroAuditoria {
    private UUID sesion;
    @Enumerated(EnumType.STRING) private MotivoCierre motivo;
    protected CierreSesion(){}
    public CierreSesion(Sesion sesion,MotivoCierre motivo){super(sesion.usuario().getIdentificacion(),sesion.getEquipo());this.sesion=sesion.getIdSesion();this.motivo=motivo;}
    public UUID getSesion(){return sesion;}
    public MotivoCierre getMotivo(){return motivo;}
}
