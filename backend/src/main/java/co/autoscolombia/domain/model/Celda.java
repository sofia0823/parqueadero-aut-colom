
package co.autoscolombia.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "celdas")
public class Celda {

    public enum Estado {
        DISPONIBLE,
        OCUPADA,
        MANTENIMIENTO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String codigo;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Estado estado = Estado.DISPONIBLE;

    public Celda() {
    }

    public Celda(String codigo, String tipo) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.estado = Estado.DISPONIBLE;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
