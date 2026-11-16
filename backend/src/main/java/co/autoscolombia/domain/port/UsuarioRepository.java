package co.autoscolombia.domain.port;

import co.autoscolombia.domain.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    List<Usuario> listar();

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorIdConBloqueo(Long id);

    boolean existePorNumeroDocumento(String numeroDocumento);

    boolean existePorNumeroDocumentoYIdDistinto(
            String numeroDocumento,
            Long id);

    Usuario guardar(Usuario usuario);
}
