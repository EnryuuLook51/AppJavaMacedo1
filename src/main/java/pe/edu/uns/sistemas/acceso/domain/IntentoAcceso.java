package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
@Entity
public class IntentoAcceso extends RegistroAuditoria {
    private String identificadorIngresado;
    @Enumerated(EnumType.STRING) private ResultadoAcceso resultado;
    private String motivoRechazo;
    private String equipo;
    private String direccionRed;
    protected IntentoAcceso(){}
    public IntentoAcceso(String identificador,ResultadoAcceso resultado,String motivo,String equipo,String red){
        super(identificador,equipo+" / "+red);identificadorIngresado=identificador;this.resultado=resultado;motivoRechazo=motivo;this.equipo=equipo;direccionRed=red;
    }
    public String getIdentificadorIngresado(){return identificadorIngresado;}
    public ResultadoAcceso getResultado(){return resultado;}
    public String getMotivoRechazo(){return motivoRechazo;}
    public String getEquipo(){return equipo;}
    public String getDireccionRed(){return direccionRed;}
}
