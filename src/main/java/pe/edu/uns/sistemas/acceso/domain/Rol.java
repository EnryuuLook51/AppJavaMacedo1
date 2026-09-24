package pe.edu.uns.sistemas.acceso.domain;
import jakarta.persistence.*;
import java.util.*;
@Entity
public class Rol {
    @Id private UUID idRol = UUID.randomUUID();
    @Column(unique=true,nullable=false) private String nombre;
    @ManyToMany(fetch=FetchType.EAGER) private Set<Permiso> permisos = new HashSet<>();
    protected Rol() {}
    public Rol(String nombre,Set<Permiso> permisos) { this.nombre=nombre; this.permisos=permisos; }
    public UUID getIdRol() { return idRol; }
    public String getNombre() { return nombre; }
    public Set<Permiso> getPermisos() { return Set.copyOf(permisos); }
    public boolean tienePermiso(String codigo) { return permisos.stream().anyMatch(p->p.getCodigo().equals(codigo)); }
}
