package utng.biblioteca.dao.interfaces;

import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.Rol;
import utng.biblioteca.model.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Acceso a Usuario y Rol. */
public interface UsuarioDao {

    /**
     * Ejecuta SP_Login. Devuelve el usuario si las credenciales son validas.
     * El propio procedimiento deja el registro en la tabla Acceso.
     */
    Optional<Usuario> autenticar(String usuario, String passwordHash) throws SQLException;

    /** Deja constancia del cierre de sesion en la tabla Acceso. */
    void registrarLogout(int idUsuario) throws SQLException;

    Optional<Usuario> buscarPorId(int idUsuario) throws SQLException;

    /** @param texto busca en nombre, numero de control, correo o usuario. */
    List<Usuario> listar(String texto, Integer idRol, Boolean soloActivos) throws SQLException;

    int insertar(Usuario usuario) throws SQLException;

    void actualizar(Usuario usuario) throws SQLException;

    void cambiarEstado(int idUsuario, boolean activo) throws SQLException;

    void cambiarPassword(int idUsuario, String passwordHash) throws SQLException;

    List<Rol> listarRoles() throws SQLException;

    /** Roles que se ofrecen en la pantalla de registro (Estudiante y Profesor). */
    List<Rol> listarRolesPublicos() throws SQLException;

    /**
     * Ejecuta SP_RegistrarCuenta: alta que hace el propio usuario desde la
     * pantalla de acceso. El procedimiento rechaza cualquier rol que no sea
     * Estudiante o Profesor.
     */
    Resultado registrarCuenta(Usuario usuario, String passwordHash, String rol) throws SQLException;
}
