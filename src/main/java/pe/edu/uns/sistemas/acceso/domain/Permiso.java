package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
@Entity
public class Permiso {
    @Id private String codigo;
    private String descripcion;
    protected Permiso() {}
    public Permiso(String codigo,String descripcion) { this.codigo=codigo; this.descripcion=descripcion; }
    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
}
