package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
@Entity
public class Credencial {
    @Id private UUID id = UUID.randomUUID();
    private String hashActual;
    private Instant modificadaEn;
    @ElementCollection @OrderColumn private List<ClaveHistorica> historial = new ArrayList<>();
    protected Credencial() {}
    public Credencial(String secreto, PoliticaSeguridad politica, PasswordEncoder encoder) {
        politica.validarContrasena(secreto); hashActual=encoder.encode(secreto); modificadaEn=Instant.now();
    }
    public boolean verificar(String secreto, PasswordEncoder encoder) { return encoder.matches(secreto,hashActual); }
    public void cambiar(String actual,String nueva,PoliticaSeguridad politica,PasswordEncoder encoder) {
        if(!verificar(actual,encoder)) throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        restablecer(nueva,politica,encoder);
    }
    public void restablecer(String nueva,PoliticaSeguridad politica,PasswordEncoder encoder) {
        politica.validarContrasena(nueva);
        if(verificar(nueva,encoder) || historial.stream().limit(politica.getCantidadHistoricas()).anyMatch(h->h.coincide(nueva,encoder)))
            throw new IllegalArgumentException("La contraseña ya fue utilizada anteriormente.");
        historial.add(0,new ClaveHistorica(hashActual));
        hashActual=encoder.encode(nueva); modificadaEn=Instant.now();
    }
}
