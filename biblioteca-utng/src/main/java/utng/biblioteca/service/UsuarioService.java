package utng.biblioteca.service;

import utng.biblioteca.dao.impl.AreaDaoImpl;
import utng.biblioteca.dao.impl.UsuarioDaoImpl;
import utng.biblioteca.dao.interfaces.AreaDao;
import utng.biblioteca.dao.interfaces.UsuarioDao;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.Area;
import utng.biblioteca.model.Rol;
import utng.biblioteca.model.Usuario;
import utng.biblioteca.session.Sesion;
import utng.biblioteca.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

/** Alta, edición y consulta de usuarios. */
public class UsuarioService extends ServicioBase {

    private final UsuarioDao usuarioDao;
    private final AreaDao areaDao;

    public UsuarioService() {
        this(new UsuarioDaoImpl(), new AreaDaoImpl());
    }

    public UsuarioService(UsuarioDao usuarioDao, AreaDao areaDao) {
        this.usuarioDao = usuarioDao;
        this.areaDao = areaDao;
    }

    /**
     * Carreras y departamentos disponibles.
     * Lista vacía si todavía no se ejecutó la migración 05_area.sql.
     */
    public List<Area> areas() {
        return consultar("listar áreas", areaDao::listarActivas);
    }

    /** true si la base ya tiene el catálogo de áreas instalado. */
    public boolean hayAreas() {
        return areaDao.disponible();
    }

    public List<Usuario> listar(String texto, Integer idRol, boolean soloActivos) {
        return consultar("listar usuarios", () -> usuarioDao.listar(texto, idRol, soloActivos));
    }

    public List<Rol> roles() {
        return consultar("listar roles", usuarioDao::listarRoles);
    }

    public Optional<Usuario> porId(int idUsuario) {
        return valor("obtener usuario", () -> usuarioDao.buscarPorId(idUsuario), Optional::empty);
    }

    /**
     * Guarda un usuario nuevo o actualiza uno existente.
     *
     * @param passwordPlano solo se usa en el alta o cuando se quiere reiniciar
     *                      la contrasena; puede venir vacio al editar.
     */
    public Resultado guardar(Usuario usuario, String passwordPlano) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Tu rol no permite administrar usuarios.");
        }
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            return Resultado.error("El nombre es obligatorio.");
        }
        if (usuario.getRol() == null || usuario.getRol().getIdRol() <= 0) {
            return Resultado.error("Selecciona un rol.");
        }

        boolean esAdministrativo = "SuperAdministrador".equalsIgnoreCase(usuario.getRol().getNombre())
                                || "Administrador".equalsIgnoreCase(usuario.getRol().getNombre());

        if (esAdministrativo && !Sesion.esSuperAdministrador()) {
            return Resultado.error("Solo el superadministrador puede crear cuentas administrativas.");
        }

        boolean esNuevo = usuario.getIdUsuario() <= 0;

        if (esNuevo && usuario.getUsuario() != null && !usuario.getUsuario().isBlank()
                && (passwordPlano == null || passwordPlano.isBlank())) {
            return Resultado.error("Define una contraseñaa para la cuenta de acceso.");
        }

        return operar("guardar el usuario", () -> {
            if (esNuevo) {
                usuario.setPasswordHash(
                        passwordPlano == null || passwordPlano.isBlank()
                                ? null : PasswordUtil.hash(passwordPlano));
                int id = usuarioDao.insertar(usuario);
                return Resultado.ok("Usuario registrado.", id);
            }

            usuarioDao.actualizar(usuario);
            if (passwordPlano != null && !passwordPlano.isBlank()) {
                usuarioDao.cambiarPassword(usuario.getIdUsuario(), PasswordUtil.hash(passwordPlano));
            }
            return Resultado.ok("Usuario actualizado.", usuario.getIdUsuario());
        });
    }

    public Resultado cambiarEstado(int idUsuario, boolean activo) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Tu rol no permite administrar usuarios.");
        }
        if (Sesion.getIdUsuario() != null && Sesion.getIdUsuario() == idUsuario) {
            return Resultado.error("No puedes desactivar tu propia cuenta.");
        }
        return operar("cambiar el estado del usuario", () -> {
            usuarioDao.cambiarEstado(idUsuario, activo);
            return Resultado.ok(activo ? "Usuario reactivado." : "Usuario dado de baja.");
        });
    }

    // --------------------------------------------------- registro desde el acceso

    /** Roles que puede elegir quien crea su cuenta: Estudiante y Profesor. */
    public List<Rol> rolesPublicos() {
        return consultar("listar roles de registro", usuarioDao::listarRolesPublicos);
    }

    /**
     * Alta que hace el propio usuario desde la pantalla de acceso.
     *
     * A diferencia de {@link #guardar}, aqui no se exige sesion iniciada: es
     * el unico camino de alta sin credenciales, y por eso SP_RegistrarCuenta
     * rechaza en el motor cualquier rol distinto de Estudiante o Profesor.
     */
    public Resultado registrarCuenta(Usuario usuario, String passwordPlano,
                                     String passwordConfirmacion, String rol) {

        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            return Resultado.error("Escribe tu nombre completo.");
        }
        if (rol == null || rol.isBlank()) {
            return Resultado.error("Selecciona si eres estudiante o profesor.");
        }
        if (usuario.getNumeroControl() == null || usuario.getNumeroControl().isBlank()) {
            return Resultado.error("Escribe tu numero de control o de empleado.");
        }
        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            return Resultado.error("Escribe tu correo institucional.");
        }
        if (!usuario.getCorreo().contains("@") || !usuario.getCorreo().contains(".")) {
            return Resultado.error("El correo no tiene un formato valido.");
        }
        if (usuario.getUsuario() == null || usuario.getUsuario().isBlank()) {
            return Resultado.error("Define el usuario con el que iniciaras sesion.");
        }
        if (usuario.getUsuario().contains(" ")) {
            return Resultado.error("El usuario no puede llevar espacios.");
        }
        if (passwordPlano == null || passwordPlano.length() < 6) {
            return Resultado.error("La contrasena debe tener al menos 6 caracteres.");
        }
        if (!passwordPlano.equals(passwordConfirmacion)) {
            return Resultado.error("Las contrasenas no coinciden.");
        }

        return operar("crear la cuenta",
                () -> usuarioDao.registrarCuenta(usuario, PasswordUtil.hash(passwordPlano), rol));
    }

    /** Cambio de contrasena del propio usuario en sesion. */
    public Resultado cambiarMiPassword(String passwordActual, String passwordNuevo) {
        Usuario usuario = Sesion.getUsuario();
        if (usuario == null) {
            return Resultado.error("No hay sesión activa.");
        }
        if (!PasswordUtil.coincide(passwordActual, usuario.getPasswordHash())) {
            return Resultado.error("La contraseña actual no es correcta.");
        }
        if (passwordNuevo == null || passwordNuevo.length() < 6) {
            return Resultado.error("La nueva contraseña debe tener al menos 6 caracteres.");
        }
        return operar("cambiar la contraseña", () -> {
            String hash = PasswordUtil.hash(passwordNuevo);
            usuarioDao.cambiarPassword(usuario.getIdUsuario(), hash);
            usuario.setPasswordHash(hash);
            return Resultado.ok("Contraseña actualizada.");
        });
    }
}
