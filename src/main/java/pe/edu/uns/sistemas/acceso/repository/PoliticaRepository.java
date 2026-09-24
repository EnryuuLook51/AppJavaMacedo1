package pe.edu.uns.sistemas.acceso.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.uns.sistemas.acceso.domain.PoliticaSeguridad;
public interface PoliticaRepository extends JpaRepository<PoliticaSeguridad,Long>{}
