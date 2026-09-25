package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.util.UUID;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
@Entity
public class CuentaAcceso {
    @Id private UUID idCuenta = UUID.randomUUID();
    @OneToOne(mappedBy="cuenta",optional=false) private Usuario usuario;
    @Enumerated(EnumType.STRING) private EstadoCuenta estado = EstadoCuenta.ACTIVA;
    private int fallosConsecutivos;
    @OneToOne(cascade=CascadeType.ALL,optional=false) private Credencial credencial;
    protected CuentaAcceso() {}
    public CuentaAcceso(Credencial credencial) { this.credencial=credencial; }
    public UUID getIdCuenta() { return idCuenta; }
    public EstadoCuenta getEstado() { return estado; }
    public int getFallosConsecutivos() { return fallosConsecutivos; }
    public Credencial credencial() { return credencial; }
    public Usuario usuario() { return usuario; }
    void vincularUsuario(Usuario usuario) { this.usuario=usuario; }
    public void registrarFallo(int limite) { if(++fallosConsecutivos>=limite) estado=EstadoCuenta.BLOQUEADA; }
    public void reiniciarFallos() { fallosConsecutivos=0; }
    public void desbloquear() {
        if(estado==EstadoCuenta.DESHABILITADA) throw new IllegalArgumentException("La cuenta está deshabilitada.");
        estado=EstadoCuenta.ACTIVA; reiniciarFallos();
    }
    public void deshabilitar() { estado=EstadoCuenta.DESHABILITADA; }
    public boolean puedeIniciarSesion() { return estado==EstadoCuenta.ACTIVA; }
}
