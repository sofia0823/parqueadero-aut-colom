package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Usuario;
import co.autoscolombia.domain.port.UsuarioRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaUsuarioRepository implements UsuarioRepository {

    private final SpringDataUsuario jpa;

    public JpaUsuarioRepository(SpringDataUsuario jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Usuario> buscarPorIdConBloqueo(Long id) {
        return jpa.findByIdForUpdate(id);
    }
}
