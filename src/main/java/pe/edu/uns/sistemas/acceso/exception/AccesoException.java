package pe.edu.uns.sistemas.acceso.exception;
public class AccesoException extends RuntimeException {
    private final int status;
    public AccesoException(int status,String mensaje){super(mensaje);this.status=status;}
    public int getStatus(){return status;}
}
