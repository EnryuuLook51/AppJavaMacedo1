package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
@Entity
public class PoliticaSeguridad {
    @Id private Long id = 1L;
    private int maxIntentos = 5;
    private int inactividadMaxima = 20;
    private int vigenciaToken = 15;
    private int cantidadHistoricas = 5;
    public int getMaxIntentos() { return maxIntentos; }
    public int getInactividadMaxima() { return inactividadMaxima; }
    public int getVigenciaToken() { return vigenciaToken; }
    public int getCantidadHistoricas() { return cantidadHistoricas; }
    public String getReglasContrasena() { return "Entre 8 y 72 caracteres, con mayúscula, minúscula, número y símbolo."; }
    public void actualizar(int intentos, int inactividad, int token, int historicas) {
        if(intentos<1 || intentos>20 || inactividad<1 || inactividad>1440 || token<1 || token>1440 || historicas<1 || historicas>24)
            throw new IllegalArgumentException("Rangos: intentos 1–20, minutos 1–1440, contraseñas históricas 1–24.");
        maxIntentos=intentos; inactividadMaxima=inactividad; vigenciaToken=token; cantidadHistoricas=historicas;
    }
    public void validarContrasena(String secreto) {
        if(secreto==null || secreto.length()<8 || secreto.length()>72 || secreto.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72 ||
           !secreto.matches("(?s).*[A-Z].*") || !secreto.matches("(?s).*[a-z].*") ||
           !secreto.matches("(?s).*[0-9].*") || !secreto.matches("(?s).*[^a-zA-Z0-9\\s].*"))
            throw new IllegalArgumentException(getReglasContrasena());
    }
}
