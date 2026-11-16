package co.autoscolombia.application;

import co.autoscolombia.application.dto.UsuarioRequest;
import co.autoscolombia.application.dto.UsuarioResponse;
import co.autoscolombia.domain.model.Usuario;
import co.autoscolombia.domain.port.UsuarioRepository;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private static final Pattern EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.listar().stream().map(UsuarioService::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return respuesta(usuarios.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El usuario solicitado no existe."
                )));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest solicitud) {
        Datos datos = validar(solicitud);
        if (usuarios.existePorNumeroDocumento(datos.numeroDocumento())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese número de identificación."
            );
        }

        return respuesta(usuarios.guardar(new Usuario(
                datos.tipoDocumento(),
                datos.numeroDocumento(),
                datos.nombres(),
                datos.telefono(),
                datos.correo()
        )));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest solicitud) {
        Usuario usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El usuario solicitado no existe."
                ));
        Datos datos = validar(solicitud);
        if (usuarios.existePorNumeroDocumentoYIdDistinto(
                datos.numeroDocumento(),
                id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese número de identificación."
            );
        }

        usuario.actualizarDatos(
                datos.tipoDocumento(),
                datos.numeroDocumento(),
                datos.nombres(),
                datos.telefono(),
                datos.correo()
        );
        return respuesta(usuarios.guardar(usuario));
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El usuario solicitado no existe."
                ));
        usuario.desactivar();
        usuarios.guardar(usuario);
    }

    private static Datos validar(UsuarioRequest solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "Debes enviar los datos del usuario."
            );
        }
        String tipoDocumento = requerido(
                solicitud.tipoDocumento(),
                "El tipo de identificación"
        );
        String numeroDocumento = requerido(
                solicitud.numeroDocumento(),
                "El número de identificación"
        );
        String nombres = requerido(solicitud.nombres(), "Los nombres");
        String telefono = opcional(solicitud.telefono());
        String correo = opcional(solicitud.correo());

        maximo(tipoDocumento, 10, "El tipo de identificación");
        maximo(numeroDocumento, 25, "El número de identificación");
        maximo(nombres, 100, "Los nombres");
        maximo(telefono, 30, "El teléfono");
        maximo(correo, 120, "El correo");
        if (correo != null && !EMAIL.matcher(correo).matches()) {
            throw new IllegalArgumentException(
                    "El correo electrónico no tiene un formato válido."
            );
        }

        return new Datos(
                tipoDocumento,
                numeroDocumento,
                nombres,
                telefono,
                correo
        );
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.trim();
    }

    private static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static void maximo(String valor, int limite, String campo) {
        if (valor != null && valor.length() > limite) {
            throw new IllegalArgumentException(
                    campo + " no puede superar " + limite + " caracteres."
            );
        }
    }

    private static UsuarioResponse respuesta(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getTipoDocumento(),
                usuario.getNumeroDocumento(),
                usuario.getNombres(),
                usuario.getTelefono(),
                usuario.getCorreo(),
                usuario.isActivo(),
                usuario.getCreadoEn()
        );
    }

    private record Datos(
            String tipoDocumento,
            String numeroDocumento,
            String nombres,
            String telefono,
            String correo) {
    }
}
