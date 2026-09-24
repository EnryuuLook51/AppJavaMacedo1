package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;
@Entity
public class AsignacionRol {
    @Id private UUID idAsignacion=UUID.randomUUID();
    @ManyToOne(optional=false) private Rol rol;
    private Instant inicioVigencia;
    private Instant finVigencia;
    private Instant retiradaEn;
    protected AsignacionRol() {}
    public AsignacionRol(Rol rol,Instant inicio,Instant fin) { this.rol=rol; modificarVigencia(inicio,fin); }
    public UUID getIdAsignacion(){return idAsignacion;}
    public Rol getRol(){return rol;}
    public Instant getInicioVigencia(){return inicioVigencia;}
    public Instant getFinVigencia(){return finVigencia;}
    public Instant getRetiradaEn(){return retiradaEn;}
    public boolean esVigente(Instant fecha) { return retiradaEn==null && !fecha.isBefore(inicioVigencia) && (finVigencia==null || fecha.isBefore(finVigencia)); }
    public void modificarVigencia(Instant inicio,Instant fin) {
        if(retiradaEn!=null) throw new IllegalArgumentException("La asignación ya fue retirada.");
        if(inicio==null || (fin!=null && !fin.isAfter(inicio))) throw new IllegalArgumentException("El fin debe ser posterior al inicio.");
        inicioVigencia=inicio; finVigencia=fin;
    }
    public void retirar(Instant fecha) {
        if(retiradaEn!=null) throw new IllegalArgumentException("La asignación ya fue retirada.");
        retiradaEn=fecha;
    }
}
