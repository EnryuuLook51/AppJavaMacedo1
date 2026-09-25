package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
@Entity
public class IntervencionAdministrativa extends RegistroAuditoria {
    private String ejecutor;
    @ManyToOne private Usuario actor;
    private String accion;
    private String detalle;
    private String objetivoSistema;
    protected IntervencionAdministrativa(){}
    public IntervencionAdministrativa(Usuario actor,Usuario objetivo,String accion,String detalle){
        super(objetivo==null?null:objetivo.getIdentificacion(),"Administración");this.actor=actor;vincularObjetivo(objetivo);ejecutor=actor.getIdentificacion();this.accion=accion;this.detalle=detalle;objetivoSistema=objetivo==null?"Política de seguridad":null;
    }
    public String getEjecutor(){return ejecutor;}
    public String getAccion(){return accion;}
    public String getDetalle(){return detalle;}
    public String getObjetivoSistema(){return objetivoSistema;}
}
