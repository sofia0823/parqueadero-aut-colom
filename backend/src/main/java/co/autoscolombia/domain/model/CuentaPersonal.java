package co.autoscolombia.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cuentas_personal",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_cuenta_personal_usuario",
                columnNames = "nombre_usuario"
        )
)
public class CuentaPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, length = 50)
    private String nombreUsuario;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolPersonal rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    protected CuentaPersonal() {
    }

    public CuentaPersonal(
            String nombreUsuario,
            String passwordHash,
            RolPersonal rol) {
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    @PrePersist
    void asignarFechaCreacion() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public RolPersonal getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void actualizarPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
