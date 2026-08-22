package utng.biblioteca.service;

import utng.biblioteca.dao.impl.MultaDaoImpl;
import utng.biblioteca.dao.impl.PrestamoDaoImpl;
import utng.biblioteca.dao.impl.SolicitudPrestamoDaoImpl;
import utng.biblioteca.dao.interfaces.MultaDao;
import utng.biblioteca.dao.interfaces.PrestamoDao;
import utng.biblioteca.dao.interfaces.SolicitudPrestamoDao;
import utng.biblioteca.dto.PrestamoDetalle;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.dto.SolicitudPrestamoDetalle;
import utng.biblioteca.model.Usuario;
import utng.biblioteca.session.Sesion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Prestamos, devoluciones y consulta del historial.
 *
 * Las reglas duras (limite por rol, adeudo, disponibilidad) las valida
 * SP_RegistrarPrestamo dentro de una transaccion. Aqui solo se hacen las
 * comprobaciones que permiten dar un mensaje inmediato al usuario sin ir a
 * la base, y se resuelve quien queda registrado como administrador.
 */
public class PrestamoService extends ServicioBase {

    private final PrestamoDao prestamoDao;
    private final MultaDao multaDao;
    private final SolicitudPrestamoDao solicitudDao;

    public PrestamoService() {
        this(new PrestamoDaoImpl(), new MultaDaoImpl(), new SolicitudPrestamoDaoImpl());
    }

    public PrestamoService(PrestamoDao prestamoDao, MultaDao multaDao, SolicitudPrestamoDao solicitudDao) {
        this.prestamoDao = prestamoDao;
        this.multaDao = multaDao;
        this.solicitudDao = solicitudDao;
    }

    /**
     * Registra un prestamo para el usuario indicado.
     * Si quien opera es personal de biblioteca queda como responsable.
     */
    public Resultado registrar(int idUsuario, List<Integer> idsEjemplares) {
        if (idsEjemplares == null || idsEjemplares.isEmpty()) {
            return Resultado.error("Selecciona al menos un ejemplar.");
        }

        Integer idAdministrador = Sesion.esPersonalBiblioteca() ? Sesion.getIdUsuario() : null;
        return operar("registrar el prestamo",
                () -> prestamoDao.registrarPrestamo(idUsuario, idAdministrador, idsEjemplares));
    }

    // ------------------------------------------------------- solicitudes de prestamo

    /**
     * El usuario pide un material desde su interfaz. No se lleva nada todavia:
     * queda una solicitud pendiente que el personal de biblioteca aprueba o
     * rechaza. Al aprobar es cuando nace el prestamo y se asigna el ejemplar.
     */
    public Resultado solicitarMaterial(int idMaterial) {
        Usuario usuario = Sesion.getUsuario();
        if (usuario == null) {
            return Resultado.error(Sesion.esInvitado()
                    ? "Estas navegando sin cuenta. Inicia sesión para solicitar prestamos."
                    : "Inicia sesión para solicitar un prestamo.");
        }
        if (!Sesion.puedeSolicitarPrestamo()) {
            return Resultado.error("El rol " + Sesion.getNombreRol()
                                 + " solo tiene acceso de consulta, no puede solicitar préstamos.");
        }
        return operar("enviar la solicitud",
                () -> solicitudDao.solicitar(usuario.getIdUsuario(), idMaterial));
    }

    /** Bandeja del personal de biblioteca. @param estado null = todas. */
    public List<SolicitudPrestamoDetalle> bandejaSolicitudes(String estado) {
        return consultar("listar solicitudes de préstamos", () -> solicitudDao.listar(null, estado));
    }

    public List<SolicitudPrestamoDetalle> misSolicitudes() {
        Integer id = Sesion.getIdUsuario();
        if (id == null) {
            return List.of();
        }
        return consultar("listar mis solicitudes", () -> solicitudDao.listar(id, null));
    }

    /** Aprobar genera el prestamo con el primer ejemplar libre del material. */
    public Resultado aprobarSolicitud(int idSolicitud, String observaciones) {
        return responderSolicitud(idSolicitud, true, observaciones);
    }

    public Resultado rechazarSolicitud(int idSolicitud, String observaciones) {
        return responderSolicitud(idSolicitud, false, observaciones);
    }

    private Resultado responderSolicitud(int idSolicitud, boolean aprobar, String observaciones) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Solo el personal de biblioteca responde solicitudes de prestamo.");
        }
        return operar("responder la solicitud",
                () -> solicitudDao.responder(idSolicitud, Sesion.getIdUsuario(), aprobar, observaciones));
    }

    /** El propio usuario retira su solicitud mientras siga pendiente. */
    public Resultado cancelarSolicitud(int idSolicitud) {
        Integer id = Sesion.getIdUsuario();
        if (id == null) {
            return Resultado.error("No hay sesión activa.");
        }
        return operar("cancelar la solicitud", () -> solicitudDao.cancelar(idSolicitud, id));
    }

    public Resultado devolver(int idDetallePrestamo) {
        return devolver(idDetallePrestamo, false);
    }

    public Resultado devolver(int idDetallePrestamo, boolean perdido) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Solo el personal de biblioteca registra devoluciones.");
        }
        return operar("registrar la devolución",
                () -> prestamoDao.registrarDevolucion(idDetallePrestamo, Sesion.getIdUsuario(), perdido));
    }

    /** Historial completo (vista de administrador). */
    public List<PrestamoDetalle> listarTodos(String texto, boolean soloPendientes) {
        return consultar("listar préstamos", () -> prestamoDao.listar(null, soloPendientes, texto));
    }

    /** Prestamos del usuario en sesion. */
    public List<PrestamoDetalle> misPrestamos(boolean soloPendientes) {
        Integer id = Sesion.getIdUsuario();
        if (id == null) {
            return List.of();
        }
        return consultar("listar mis préstamos", () -> prestamoDao.listar(id, soloPendientes, null));
    }

    public List<PrestamoDetalle> listarDe(int idUsuario, boolean soloPendientes) {
        return consultar("listar préstamos del usuario",
                () -> prestamoDao.listar(idUsuario, soloPendientes, null));
    }

    public List<PrestamoDetalle> retrasados() {
        return consultar("listar retrasos", prestamoDao::listarRetrasados);
    }

    /** Se ejecuta al entrar el personal de biblioteca para poner al dia los estados. */
    public int actualizarVencidos() {
        return valor("actualizar préstamos vencidos", prestamoDao::actualizarVencidos, () -> 0);
    }

    public int materialesEnPoder(int idUsuario) {
        return valor("contar materiales en poder",
                () -> prestamoDao.materialesEnPoder(idUsuario), () -> 0);
    }

    /**
     * Cuantos ejemplares mas puede llevarse el usuario en sesion,
     * segun el limite de su rol y lo que ya tiene prestado.
     */
    public int cupoDisponible() {
        Usuario usuario = Sesion.getUsuario();
        if (usuario == null || usuario.getRol() == null) {
            return 0;
        }
        int limite = usuario.getRol().getMaxMateriales();
        int pendientes = valor("contar solicitudes pendientes",
                () -> solicitudDao.pendientesDe(usuario.getIdUsuario()), () -> 0);
        return Math.max(0, limite - materialesEnPoder(usuario.getIdUsuario()) - pendientes);
    }

    public BigDecimal miAdeudo() {
        Integer id = Sesion.getIdUsuario();
        if (id == null) {
            return BigDecimal.ZERO;
        }
        return valor("consultar adeudo", () -> multaDao.adeudoDe(id), () -> BigDecimal.ZERO);
    }
}
