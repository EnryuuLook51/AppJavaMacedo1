package pe.edu.uns.sistemas.acceso.repository;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.uns.sistemas.acceso.domain.Rol;
public interface RolRepository extends JpaRepository<Rol,UUID> {
    Optional<Rol> findByNombre(String nombre);
}
