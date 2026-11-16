
package co.autoscolombia.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "estancias")
public class Estancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "celda_id", nullable = false)
    private Celda celda;

    @Column(nullable = false)
    private LocalDateTime entrada;

    private LocalDateTime salida;

    @Column(name = "registrado_por")
    private Long registradoPor;

    public Estancia() {}

    public Estancia(
            Vehiculo vehiculo,
            Celda celda,
            Long registradoPor) {
        this.vehiculo = vehiculo;
        this.celda = celda;
        this.registradoPor = registradoPor;
        this.entrada = LocalDateTime.now();
        this.salida = null;
    }

    public Long getId() {
        return id;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Celda getCelda() {
        return celda;
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public LocalDateTime getSalida() {
        return salida;
    }

    public Long getRegistradoPor() {
        return registradoPor;
    }

    public void setSalida(LocalDateTime salida) {
        this.salida = salida;
    }
}
