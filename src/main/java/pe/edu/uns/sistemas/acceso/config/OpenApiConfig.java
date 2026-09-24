package pe.edu.uns.sistemas.acceso.config;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;
import java.util.Map;
/** Expone el token CSRF para el cliente del mismo origen. Contrato REST en docs/api.md. */
@RestController
public class OpenApiConfig {
    @GetMapping("/api/csrf") public Map<String,String> csrf(CsrfToken token){return Map.of("token",token.getToken(),"headerName",token.getHeaderName());}
}
