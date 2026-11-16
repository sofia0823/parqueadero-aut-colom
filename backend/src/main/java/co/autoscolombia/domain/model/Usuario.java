package co.autoscolombia.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 25)
    private String numeroDocumento;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(length = 30)
    private String telefono;

    @Column(length = 120)
    private String correo;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    protected Usuario() {
    }

    public Usuario(
            String tipoDocumento,
            String numeroDocumento,
            String nombres,
            String telefono,
            String correo) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.telefono = telefono;
        this.correo = correo;
        this.activo = true;
    }

    @PrePersist
    void asignarFechaCreacion() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public boolean isActivo() {
        return activo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void actualizarDatos(
            String tipoDocumento,
            String numeroDocumento,
            String nombres,
            String telefono,
            String correo) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.telefono = telefono;
        this.correo = correo;
    }

    public void desactivar() {
        this.activo = false;
    }
}
