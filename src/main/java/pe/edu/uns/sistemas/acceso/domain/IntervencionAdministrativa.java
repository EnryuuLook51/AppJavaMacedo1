package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
@Entity
public class IntervencionAdministrativa extends RegistroAuditoria {
    private String ejecutor;
    private String accion;
    private String detalle;
    private String objetivoSistema;
    protected IntervencionAdministrativa(){}
    public IntervencionAdministrativa(String actor,String objetivo,String accion,String detalle){
        super(objetivo,"Administración");ejecutor=actor;this.accion=accion;this.detalle=detalle;objetivoSistema=objetivo==null?"Política de seguridad":null;
    }
    public String getEjecutor(){return ejecutor;}
    public String getAccion(){return accion;}
    public String getDetalle(){return detalle;}
    public String getObjetivoSistema(){return objetivoSistema;}
}
