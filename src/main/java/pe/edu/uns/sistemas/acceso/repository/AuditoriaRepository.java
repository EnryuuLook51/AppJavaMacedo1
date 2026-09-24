package pe.edu.uns.sistemas.acceso.repository;
import java.util.*;
import org.springframework.data.repository.Repository;
import pe.edu.uns.sistemas.acceso.domain.RegistroAuditoria;
/** El contrato no expone edición ni eliminación de registros históricos. */
public interface AuditoriaRepository extends Repository<RegistroAuditoria,UUID> {
    <S extends RegistroAuditoria> S save(S evento);
    List<RegistroAuditoria> findAllByOrderByFechaHoraDesc();
}
