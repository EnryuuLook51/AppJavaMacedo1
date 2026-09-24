package pe.edu.uns.sistemas.acceso.repository;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.uns.sistemas.acceso.domain.Usuario;
public interface UsuarioRepository extends JpaRepository<Usuario,UUID> {
    Optional<Usuario> findByIdentificacion(String identificacion);
}
