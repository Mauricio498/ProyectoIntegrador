package utng.biblioteca.dao.interfaces;

import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.SolicitudAdquisicion;

import java.sql.SQLException;
import java.util.List;

/** Solicitudes de adquisicion que levantan profesores y estudiantes. */
public interface SolicitudDao {

    /** @param idUsuario null = todas (bandeja del administrador) */
    List<SolicitudAdquisicion> listar(Integer idUsuario, String estado) throws SQLException;

    int crear(int idUsuario, String titulo, String autor, String editorial,
              String isbn, String descripcion) throws SQLException;

    /** Ejecuta SP_ResponderSolicitud. @param estado Aprobada o Rechazada. */
    Resultado responder(int idSolicitud, int idAdministrador, String estado, String respuesta)
            throws SQLException;
}
