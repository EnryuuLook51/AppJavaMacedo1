package pe.edu.uns.sistemas.acceso.service;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.function.Supplier;
import com.fasterxml.jackson.databind.*;
import pe.edu.uns.sistemas.acceso.exception.AccesoException;
/**
 * Serializa las operaciones de esta instalación local y mantiene el bloqueo hasta el commit.
 * Los rechazos de autenticación se confirman para conservar fallos y auditoría.
 */
@Component
public class UnidadTrabajo {
    private final TransactionTemplate transaccion;
    private final ObjectMapper mapper;
    public UnidadTrabajo(PlatformTransactionManager manager,ObjectMapper mapper){transaccion=new TransactionTemplate(manager);this.mapper=mapper;}
    public synchronized JsonNode ejecutar(Supplier<?> accion){
        AccesoException[] rechazo={null};
        JsonNode resultado=transaccion.execute(status->{
            try{return mapper.valueToTree(accion.get());}
            catch(AccesoException e){rechazo[0]=e;return null;}
        });
        if(rechazo[0]!=null)throw rechazo[0];
        return resultado;
    }
}
