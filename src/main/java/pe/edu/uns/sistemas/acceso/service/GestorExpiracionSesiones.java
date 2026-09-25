package pe.edu.uns.sistemas.acceso.service;

import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Actor temporizador de CU-06, desacoplado del mecanismo de autenticación. */
@Component
public class GestorExpiracionSesiones {
    private final UnidadTrabajo tx;
    private final IControlAcceso acceso;

    public GestorExpiracionSesiones(UnidadTrabajo tx,IControlAcceso acceso){
        this.tx=tx;this.acceso=acceso;
    }
    @Scheduled(initialDelay=30000,fixedDelay=30000)
    public void ejecutar(){ejecutar(Instant.now());}

    public void ejecutar(Instant ahora){
        tx.ejecutar(()->{acceso.cerrarPorInactividad(ahora);return true;});
    }
}
