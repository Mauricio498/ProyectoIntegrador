package utng.biblioteca.dao.interfaces;

import utng.biblioteca.dto.Resultado;
import utng.biblioteca.dto.SolicitudPrestamoDetalle;

import java.sql.SQLException;
import java.util.List;

/**
 * Solicitudes de prestamo: el usuario pide un material y el personal de
 * biblioteca decide. Toda la validacion vive en los procedimientos
 * SP_SolicitarPrestamo y SP_ResponderSolicitudPrestamo.
 */
public interface SolicitudPrestamoDao {

    /**
     * @param idUsuario null = todas (bandeja del administrador)
     * @param estado    null o vacio = cualquier estado
     */
    List<SolicitudPrestamoDetalle> listar(Integer idUsuario, String estado) throws SQLException;

    Resultado solicitar(int idUsuario, int idMaterial) throws SQLException;

    /** @param aprobar true aprueba y genera el prestamo, false rechaza. */
    Resultado responder(int idSolicitud, int idAdministrador, boolean aprobar, String observaciones)
            throws SQLException;

    Resultado cancelar(int idSolicitud, int idUsuario) throws SQLException;

    /** Cuantas solicitudes pendientes tiene un usuario (cuentan para su cupo). */
    int pendientesDe(int idUsuario) throws SQLException;
}
