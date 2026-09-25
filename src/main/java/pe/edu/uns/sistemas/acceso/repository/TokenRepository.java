package pe.edu.uns.sistemas.acceso.repository;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.uns.sistemas.acceso.domain.*;
public interface TokenRepository extends JpaRepository<TokenRecuperacion,UUID>{
    Optional<TokenRecuperacion> findByHashToken(String hashToken);
    @org.springframework.data.jpa.repository.Query("select t from TokenRecuperacion t where t.cuenta.usuario = :usuario")
    List<TokenRecuperacion> findByUsuario(Usuario usuario);
}
