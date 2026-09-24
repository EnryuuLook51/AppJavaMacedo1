package pe.edu.uns.sistemas.acceso.exception;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccesoException.class)
    ResponseEntity<ErrorResponse> acceso(AccesoException e){return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getMessage()));}
    @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class,HttpMessageNotReadableException.class})
    ResponseEntity<ErrorResponse> invalido(Exception e){return ResponseEntity.badRequest().body(new ErrorResponse(e instanceof IllegalArgumentException?e.getMessage():"Datos incompletos o inválidos."));}
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> conflicto(Exception e){return ResponseEntity.status(409).body(new ErrorResponse("El registro ya existe o entra en conflicto con otro dato."));}
    @ExceptionHandler(org.springframework.mail.MailException.class)
    ResponseEntity<ErrorResponse> correo(Exception e){return ResponseEntity.status(503).body(new ErrorResponse("No se pudo enviar el correo de recuperación. Revise el servicio SMTP."));}
}
