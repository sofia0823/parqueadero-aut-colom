package co.autoscolombia.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(
        name = "membresias_mensuales",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_membresia_usuario_inicio",
                columnNames = {"usuario_id", "fecha_inicio"}
        ),
        indexes = @Index(
                name = "ix_membresia_usuario_vigencia",
                columnList = "usuario_id, fecha_inicio, fecha_fin"
        )
)
public class MembresiaMensual {

    public enum Estado {
        ACTIVA,
        FINALIZADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Estado estado;

    protected MembresiaMensual() {
    }

    public MembresiaMensual(Usuario usuario, LocalDate fechaInicio) {
        this.usuario = usuario;
        this.fechaInicio = fechaInicio;
        this.estado = Estado.ACTIVA;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Estado getEstado() {
        return estado;
    }

    public void finalizar(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
        this.estado = Estado.FINALIZADA;
    }

    public boolean cubrePeriodo(java.time.YearMonth periodo) {
        LocalDate inicioPeriodo = periodo.atDay(1);
        LocalDate finPeriodo = periodo.atEndOfMonth();
        return !fechaInicio.isAfter(finPeriodo)
                && (fechaFin == null || !fechaFin.isBefore(inicioPeriodo));
    }
}
