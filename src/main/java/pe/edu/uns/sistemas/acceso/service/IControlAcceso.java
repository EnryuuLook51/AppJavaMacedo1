package pe.edu.uns.sistemas.acceso.service;
import pe.edu.uns.sistemas.acceso.domain.*;
/** La administración opera sobre acceso mediante este contrato, sin conocer secretos. */
public interface IControlAcceso {
    CuentaAcceso inicializarCredencial(String secreto);
    void cerrarSesionAdministrativa(Sesion sesion);
    void iniciarRecuperacion(Usuario usuario);
    void desbloquearCuenta(CuentaAcceso cuenta);
    void deshabilitarCuenta(Usuario usuario);
    void cerrarPorInactividad(java.time.Instant ahora);
}
