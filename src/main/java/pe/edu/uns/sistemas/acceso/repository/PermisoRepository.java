package pe.edu.uns.sistemas.acceso.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.uns.sistemas.acceso.domain.Permiso;
public interface PermisoRepository extends JpaRepository<Permiso,String>{}
