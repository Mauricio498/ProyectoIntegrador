package biblioteca.utng.service;

import biblioteca.utng.model.Usuario;
import biblioteca.utng.repository.UsuarioRepository;

/**
 * Lógica de negocio relacionada con el usuario en sesión y su perfil.
 */
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obtenerUsuarioActual() {
        return usuarioRepository.getUsuarioActual();
    }

    /**
     * Actualiza los datos personales editables del usuario. Devuelve un
     * mensaje de error si alguna validación falla, o {@code null} si todo
     * fue exitoso.
     */
    public String actualizarDatosPersonales(String correo, String telefono, String carrera, String grupo) {
        if (correo == null || correo.isBlank() || !correo.contains("@")) {
            return "Ingresa un correo electrónico válido.";
        }
        if (telefono == null || telefono.isBlank()) {
            return "El teléfono no puede estar vacío.";
        }
        if (carrera == null || carrera.isBlank()) {
            return "La carrera no puede estar vacía.";
        }
        if (grupo == null || grupo.isBlank()) {
            return "El grupo no puede estar vacío.";
        }

        Usuario usuario = usuarioRepository.getUsuarioActual();
        usuario.setCorreo(correo.trim());
        usuario.setTelefono(telefono.trim());
        usuario.setCarrera(carrera.trim());
        usuario.setGrupo(grupo.trim());
        return null;
    }
}
