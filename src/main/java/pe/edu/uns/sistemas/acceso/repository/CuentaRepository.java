package pe.edu.uns.sistemas.acceso.repository;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.uns.sistemas.acceso.domain.CuentaAcceso;
public interface CuentaRepository extends JpaRepository<CuentaAcceso,UUID> {}
