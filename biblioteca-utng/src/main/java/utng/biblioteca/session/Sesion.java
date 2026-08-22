package utng.biblioteca.session;

import utng.biblioteca.model.Usuario;

/**
 * Usuario autenticado en la aplicacion.
 *
 * Es un singleton porque la aplicacion de escritorio tiene una sola sesion
 * activa a la vez. El router y los controladores consultan aqui el rol para
 * decidir que interfaz y que acciones mostrar.
 */
public final class Sesion {

    /** Roles reconocidos por el sistema. Deben coincidir con la tabla Rol. */
    public enum RolTipo {
        SUPER_ADMINISTRADOR("SuperAdministrador"),
        ADMINISTRADOR("Administrador"),
        PROFESOR("Profesor"),
        ESTUDIANTE("Estudiante"),
        ESTUDIANTE_ESTADIA("Estudiante Estadia"),
        INVITADO("Invitado");

        private final String nombreEnBd;

        RolTipo(String nombreEnBd) {
            this.nombreEnBd = nombreEnBd;
        }

        public String getNombreEnBd() {
            return nombreEnBd;
        }

        /** Traduce el nombre guardado en Rol.nombre al enum; null si no coincide. */
        public static RolTipo desdeBd(String nombre) {
            if (nombre == null) {
                return null;
            }
            for (RolTipo r : values()) {
                if (r.nombreEnBd.equalsIgnoreCase(nombre.trim())) {
                    return r;
                }
            }
            return null;
        }
    }

    private static Usuario usuarioActual;

    /**
     * Modo consulta sin credenciales. No hay renglón en la tabla Usuario, así
     * que todo lo que dependa de un idUsuario (favoritos, préstamos, multas)
     * quedará fuera de su alcance y sus búsquedas se guardan con idUsuario NULL.
     */
    private static boolean modoInvitado;

    private Sesion() {
    }

    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
        modoInvitado = false;
    }

    /** Se llama desde el boton "Entrar sin cuenta" de la pantalla de acceso. */
    public static void iniciarComoInvitado() {
        usuarioActual = null;
        modoInvitado = true;
    }

    public static boolean esInvitado() {
        return modoInvitado;
    }

    public static void cerrar() {
        usuarioActual = null;
        modoInvitado = false;
    }

    public static Usuario getUsuario() {
        return usuarioActual;
    }

    public static boolean hayUsuario() {
        return usuarioActual != null;
    }

    /** Hay alguien usando la aplicacion, aunque sea sin cuenta. */
    public static boolean haySesion() {
        return usuarioActual != null || modoInvitado;
    }

    public static Integer getIdUsuario() {
        return usuarioActual == null ? null : usuarioActual.getIdUsuario();
    }

    public static RolTipo getRol() {
        if (modoInvitado) {
            return RolTipo.INVITADO;
        }
        if (usuarioActual == null || usuarioActual.getRol() == null) {
            return null;
        }
        return RolTipo.desdeBd(usuarioActual.getRol().getNombre());
    }

    /** true para SuperAdministrador y Administrador: usan la interfaz de gestión. */
    public static boolean esPersonalBiblioteca() {
        RolTipo rol = getRol();
        return rol == RolTipo.SUPER_ADMINISTRADOR || rol == RolTipo.ADMINISTRADOR;
    }

    /** Solo el superadministrador puede tocar catalogos base, roles y parametros. */
    public static boolean esSuperAdministrador() {
        return getRol() == RolTipo.SUPER_ADMINISTRADOR;
    }

    /** Profesor, Estudiante y Estudiante Estadía piden préstamos; el invitado no. */
    public static boolean puedeSolicitarPrestamo() {
        if (usuarioActual == null || usuarioActual.getRol() == null) {
            return false;
        }
        return usuarioActual.getRol().getMaxMateriales() > 0;
    }

    public static String getNombre() {
        if (modoInvitado) {
            return "Invitado";
        }
        return usuarioActual == null ? "Sin sesión" : usuarioActual.getNombre();
    }

    public static String getNombreRol() {
        if (modoInvitado) {
            return "Invitado";
        }
        if (usuarioActual == null || usuarioActual.getRol() == null) {
            return "Sin rol";
        }
        return usuarioActual.getRol().getNombre();
    }
}
