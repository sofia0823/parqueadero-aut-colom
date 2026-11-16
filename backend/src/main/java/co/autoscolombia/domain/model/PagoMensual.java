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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "pagos_mensuales",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_pago_mensual_usuario_periodo",
                columnNames = {"usuario_id", "periodo"}
        ),
        indexes = {
                @Index(name = "ix_pago_mensual_fecha", columnList = "fecha_pago"),
                @Index(name = "ix_pago_mensual_periodo", columnList = "periodo")
        }
)
public class PagoMensual {

    public enum Estado {
        PAGADO,
        ANULADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 7)
    private String periodo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(nullable = false, length = 30)
    private String metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Estado estado;

    @Column(length = 80)
    private String referencia;

    protected PagoMensual() {
    }

    public PagoMensual(
            Usuario usuario,
            String periodo,
            BigDecimal valor,
            String metodo) {
        this.usuario = usuario;
        this.periodo = periodo;
        this.valor = valor;
        this.metodo = metodo;
        this.fechaPago = LocalDateTime.now();
        this.estado = Estado.PAGADO;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getPeriodo() {
        return periodo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public String getMetodo() {
        return metodo;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getReferencia() {
        return referencia;
    }
}
