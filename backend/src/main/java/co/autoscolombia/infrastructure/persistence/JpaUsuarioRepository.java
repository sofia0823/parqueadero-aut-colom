package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Usuario;
import co.autoscolombia.domain.port.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaUsuarioRepository implements UsuarioRepository {

    private final SpringDataUsuario jpa;

    public JpaUsuarioRepository(SpringDataUsuario jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Usuario> listar() {
        return jpa.findAllByOrderByIdDesc();
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Usuario> buscarPorIdConBloqueo(Long id) {
        return jpa.findByIdForUpdate(id);
    }

    @Override
    public boolean existePorNumeroDocumento(String numeroDocumento) {
        return jpa.existsByNumeroDocumentoIgnoreCase(numeroDocumento);
    }

    @Override
    public boolean existePorNumeroDocumentoYIdDistinto(
            String numeroDocumento,
            Long id) {
        return jpa.existsByNumeroDocumentoIgnoreCaseAndIdNot(
                numeroDocumento,
                id
        );
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return jpa.save(usuario);
    }
}
