package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
import pe.edu.uns.sistemas.acceso.domain.Tipos.*;
@Entity
public class TokenRecuperacion {
    @Id private UUID idToken=UUID.randomUUID();
    @ManyToOne(optional=false) private CuentaAcceso cuenta;
    private String hashToken;
    private Instant emitidoEn=Instant.now();
    private Instant expiraEn;
    @Enumerated(EnumType.STRING) private EstadoToken estado=EstadoToken.EMITIDO;
    protected TokenRecuperacion() {}
    public TokenRecuperacion(Usuario usuario,String valor,int minutos){this.cuenta=usuario.cuenta();hashToken=hash(valor);expiraEn=emitidoEn.plus(Duration.ofMinutes(minutos));}
    public static String hash(String valor) {
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8)));}
        catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    public UUID getIdToken(){return idToken;}
    public Usuario usuario(){return cuenta.usuario();}
    public boolean validar(String valor,Instant ahora){return estado==EstadoToken.EMITIDO && ahora.isBefore(expiraEn) && MessageDigest.isEqual(hashToken.getBytes(StandardCharsets.UTF_8),hash(valor).getBytes(StandardCharsets.UTF_8));}
    public void consumir(){estado=EstadoToken.USADO;}
    public void invalidar(){if(estado==EstadoToken.EMITIDO)estado=EstadoToken.INVALIDADO;}
}
