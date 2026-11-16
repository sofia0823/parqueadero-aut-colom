package co.autoscolombia.domain.port;

import co.autoscolombia.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorIdConBloqueo(Long id);
}
