package pe.edu.uns.sistemas.acceso.domain;

import jakarta.persistence.*;
import java.util.UUID;
import pe.edu.uns.sistemas.acceso.domain.Tipos.ResultadoSolicitud;

/** Evento de CU-09; conserva el resultado sin exponer el secreto enviado. */
@Entity
public class SolicitudRecuperacion extends RegistroAuditoria {
    private String canal;
    @Enumerated(EnumType.STRING) @Column(name="resultado_solicitud") private ResultadoSolicitud resultado;
    private String motivoSolicitud;
    @OneToOne @JoinColumn(name="token_generado_id",unique=true) private TokenRecuperacion token;

    protected SolicitudRecuperacion() {}
    public SolicitudRecuperacion(String identificacion, Usuario usuario, String canal,
                                 ResultadoSolicitud resultado, String motivo, TokenRecuperacion token) {
        super(identificacion,"Recuperación / "+canal);
        vincularObjetivo(usuario);
        this.canal=canal;
        this.resultado=resultado;
        this.motivoSolicitud=motivo;
        this.token=token;
    }
    public String getCanal(){return canal;}
    public ResultadoSolicitud getResultado(){return resultado;}
    public String getMotivo(){return motivoSolicitud;}
    public UUID getTokenGenerado(){return token==null?null:token.getIdToken();}
}
