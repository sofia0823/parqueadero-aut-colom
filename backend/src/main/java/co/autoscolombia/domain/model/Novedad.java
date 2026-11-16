package co.autoscolombia.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "novedades")
@Check(
        name = "ck_novedad_una_relacion",
        constraints = "(vehiculo_id IS NOT NULL AND estancia_id IS NULL) "
                + "OR (vehiculo_id IS NULL AND estancia_id IS NOT NULL)"
)
public class Novedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estancia_id")
    private Estancia estancia;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 50)
    private String responsable;

    protected Novedad() {
    }

    public Novedad(
            Vehiculo vehiculo,
            Estancia estancia,
            String descripcion,
            String responsable) {
        this.vehiculo = vehiculo;
        this.estancia = estancia;
        this.descripcion = descripcion;
        this.responsable = responsable;
        this.fechaHora = LocalDateTime.now();
    }

    @PrePersist
    void asignarFechaHora() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Estancia getEstancia() {
        return estancia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getResponsable() {
        return responsable;
    }
}
