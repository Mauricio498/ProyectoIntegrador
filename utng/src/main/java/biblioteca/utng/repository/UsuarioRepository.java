package biblioteca.utng.repository;

import biblioteca.utng.model.Usuario;

/**
 * Acceso a los datos del usuario. En esta demostración solo existe un
 * usuario activo en la sesión (Ana Martínez Jasso), tal como se solicitó.
 */
public class UsuarioRepository {

    private final Usuario usuarioActual;

    public UsuarioRepository() {
        this.usuarioActual = new Usuario(
                1,
                "Ana Martínez Jasso",
                "GTID035-24",
                "ana.martinez@utng.edu.mx",
                "415 123 4567",
                "Ingeniería en Sistemas Computacionales",
                "8°A"
        );
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
