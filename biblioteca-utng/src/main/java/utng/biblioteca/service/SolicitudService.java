package utng.biblioteca.service;

import utng.biblioteca.dao.impl.SolicitudDaoImpl;
import utng.biblioteca.dao.interfaces.SolicitudDao;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.SolicitudAdquisicion;
import utng.biblioteca.session.Sesion;

import java.util.List;

/** Solicitudes de adquisicion: alta por el usuario, respuesta por el administrador. */
public class SolicitudService extends ServicioBase {

    private final SolicitudDao solicitudDao;

    public SolicitudService() {
        this(new SolicitudDaoImpl());
    }

    public SolicitudService(SolicitudDao solicitudDao) {
        this.solicitudDao = solicitudDao;
    }

    public List<SolicitudAdquisicion> bandeja(String estado) {
        return consultar("listar solicitudes", () -> solicitudDao.listar(null, estado));
    }

    public List<SolicitudAdquisicion> misSolicitudes() {
        Integer id = Sesion.getIdUsuario();
        if (id == null) {
            return List.of();
        }
        return consultar("listar mis solicitudes", () -> solicitudDao.listar(id, null));
    }

    public Resultado crear(String titulo, String autor, String editorial, String isbn, String descripcion) {
        Integer id = Sesion.getIdUsuario();
        if (id == null) {
            return Resultado.error("Inicia sesión para enviar una solicitud.");
        }
        if (titulo == null || titulo.isBlank()) {
            return Resultado.error("El título es obligatorio.");
        }
        return operar("enviar la solicitud", () -> {
            int nuevo = solicitudDao.crear(id, titulo.trim(), autor, editorial, isbn, descripcion);
            return Resultado.ok("Solicitud enviada. Recibirás respuesta del personal de biblioteca.", nuevo);
        });
    }

    public Resultado aprobar(int idSolicitud, String respuesta) {
        return responder(idSolicitud, "Aprobada", respuesta);
    }

    public Resultado rechazar(int idSolicitud, String respuesta) {
        return responder(idSolicitud, "Rechazada", respuesta);
    }

    private Resultado responder(int idSolicitud, String estado, String respuesta) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Solo el personal de biblioteca responde solicitudes.");
        }
        return operar("responder la solicitud",
                () -> solicitudDao.responder(idSolicitud, Sesion.getIdUsuario(), estado, respuesta));
    }
}
