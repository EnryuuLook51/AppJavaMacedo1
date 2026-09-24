package pe.edu.uns.sistemas.acceso.domain;
public final class Tipos {
    private Tipos() {}
    public enum TipoPersona { ESTUDIANTE, DOCENTE, ADMINISTRATIVO }
    public enum EstadoCuenta { ACTIVA, BLOQUEADA, DESHABILITADA }
    public enum EstadoSesion { ACTIVA, CERRADA }
    public enum EstadoToken { EMITIDO, USADO, INVALIDADO }
    public enum ResultadoAcceso { EXITOSO, RECHAZADO }
    public enum MotivoCierre { VOLUNTARIO, INACTIVIDAD, ADMINISTRACION }
}
