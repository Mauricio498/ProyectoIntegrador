package utng.biblioteca.service;

import utng.biblioteca.dao.impl.UsuarioDaoImpl;
import utng.biblioteca.dao.interfaces.UsuarioDao;
import utng.biblioteca.model.Usuario;
import utng.biblioteca.session.Sesion;
import utng.biblioteca.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Autenticacion y cierre de sesion.
 *
 * La validacion real la hace SP_Login, que ademas registra el intento en la
 * tabla Acceso. Aqui solo se aplica el hash y se llena la {@link Sesion}.
 */
public class AuthService extends ServicioBase {

    private final UsuarioDao usuarioDao;

    public AuthService() {
        this(new UsuarioDaoImpl());
    }

    public AuthService(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    /**
     * Intenta iniciar sesion. Si tiene exito deja el usuario en {@link Sesion}.
     *
     * @return el usuario autenticado, o vacio si las credenciales no sirven.
     */
    public Optional<Usuario> iniciarSesion(String usuario, String password) {
        if (usuario == null || usuario.isBlank() || password == null || password.isEmpty()) {
            return Optional.empty();
        }

        try {
            Optional<Usuario> encontrado =
                    usuarioDao.autenticar(usuario.trim(), PasswordUtil.hash(password));
            encontrado.ifPresent(Sesion::iniciar);
            return encontrado;
        } catch (SQLException e) {
            System.err.println("[AuthService] Error al autenticar: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Cierra la sesion y deja constancia en la bitacora de accesos.
     * Quien entro sin cuenta no tiene renglon que registrar.
     */
    public void cerrarSesion() {
        Integer id = Sesion.getIdUsuario();
        if (id != null) {
            try {
                usuarioDao.registrarLogout(id);
            } catch (SQLException e) {
                System.err.println("[AuthService] No se pudo registrar el logout: " + e.getMessage());
            }
        }
        Sesion.cerrar();
    }
}
