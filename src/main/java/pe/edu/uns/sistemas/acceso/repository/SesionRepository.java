package pe.edu.uns.sistemas.acceso.repository;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.uns.sistemas.acceso.domain.*;
import pe.edu.uns.sistemas.acceso.domain.Tipos.EstadoSesion;
public interface SesionRepository extends JpaRepository<Sesion,UUID> {
    Optional<Sesion> findByHashAutenticacion(String hashAutenticacion);
    List<Sesion> findByUsuario(Usuario usuario);
    List<Sesion> findByEstado(EstadoSesion estado);
}
