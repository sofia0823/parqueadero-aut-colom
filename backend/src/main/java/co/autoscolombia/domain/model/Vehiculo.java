package co.autoscolombia.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehiculos", uniqueConstraints = @UniqueConstraint(name = "uk_vehiculo_placa", columnNames = "placa"))
public class Vehiculo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 10)
    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @NotBlank @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String propietario;

    @Size(max = 30)
    private String telefono;

    @Size(max = 40)
    private String tipo;

    @Column(nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Vehiculo() {}
    public Vehiculo(String placa, String propietario, String telefono, String tipo) {
        this.placa = placa == null ? null : placa.trim().toUpperCase();
        this.propietario = propietario == null ? null : propietario.trim();
        this.telefono = telefono;
        this.tipo = tipo;
        this.creadoEn = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa == null ? null : placa.trim().toUpperCase(); }
    public String getPropietario() { return propietario; }
    public void setPropietario(String propietario) { this.propietario = propietario; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
