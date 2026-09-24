package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="tipo_evento")
public abstract class RegistroAuditoria {
    @Id private UUID idEvento=UUID.randomUUID();
    private Instant fechaHora=Instant.now();
    private String contexto;
    private String usuarioObjetivo;
    protected RegistroAuditoria() {}
    protected RegistroAuditoria(String objetivo,String contexto){usuarioObjetivo=objetivo;this.contexto=contexto;}
    public UUID getIdEvento(){return idEvento;}
    public Instant getFechaHora(){return fechaHora;}
    public String getContexto(){return contexto;}
    public String getUsuarioObjetivo(){return usuarioObjetivo;}
    public String getTipo(){return getClass().getSimpleName();}
}
