package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
@Embeddable
class ClaveHistorica {
    private String hash;
    private Instant usadaHasta;
    protected ClaveHistorica() {}
    ClaveHistorica(String hash) { this.hash=hash; usadaHasta=Instant.now(); }
    boolean coincide(String secreto, PasswordEncoder encoder) { return encoder.matches(secreto,hash); }
}
