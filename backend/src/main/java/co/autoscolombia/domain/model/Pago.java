package co.autoscolombia.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "pagos",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_pago_estancia",
            columnNames = "estancia_id"
        )
    }
)
public class Pago {

    public enum Estado {
        PENDIENTE,
        PAGADO,
        ANULADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estancia_id", nullable = false, unique = true)
    private Estancia estancia;

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

    public Pago() {
    }

    public Pago(Estancia estancia, BigDecimal valor, String metodo) {
        this.estancia = estancia;
        this.valor = valor;
        this.metodo = metodo;
        this.fechaPago = LocalDateTime.now();
        this.estado = Estado.PAGADO;
    }

    public Long getId() {
        return id;
    }

    public Estancia getEstancia() {
        return estancia;
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