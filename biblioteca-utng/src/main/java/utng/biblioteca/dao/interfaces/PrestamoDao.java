package utng.biblioteca.dao.interfaces;

import utng.biblioteca.dto.PrestamoDetalle;
import utng.biblioteca.dto.Resultado;

import java.sql.SQLException;
import java.util.List;

/**
 * Prestamos y devoluciones.
 *
 * Las operaciones que cambian datos van contra procedimientos almacenados,
 * porque ahi vive la validacion de reglas (limite por rol, adeudos,
 * disponibilidad del ejemplar) y la transaccion.
 */
public interface PrestamoDao {

    /** Ejecuta SP_RegistrarPrestamo con la lista de ejemplares solicitados. */
    Resultado registrarPrestamo(int idUsuario, Integer idAdministrador, List<Integer> idsEjemplares)
            throws SQLException;

    /** Ejecuta SP_RegistrarDevolucion sobre un renglon de DetallePrestamo. */
    Resultado registrarDevolucion(int idDetallePrestamo, Integer idAdministrador, boolean perdido)
            throws SQLException;

    /**
     * Consulta VW_PrestamoDetalle.
     *
     * @param idUsuario      null = todos los usuarios (vista de administrador)
     * @param soloPendientes true = solo lo que el usuario tiene en su poder
     * @param texto          filtra por titulo, codigo de ejemplar o nombre de usuario
     */
    List<PrestamoDetalle> listar(Integer idUsuario, boolean soloPendientes, String texto)
            throws SQLException;

    /** Ejemplares con retraso que siguen sin devolverse. */
    List<PrestamoDetalle> listarRetrasados() throws SQLException;

    /** Ejecuta SP_ActualizarPrestamosVencidos y devuelve cuantos cambiaron. */
    int actualizarVencidos() throws SQLException;

    /** Cuantos ejemplares tiene actualmente en su poder un usuario. */
    int materialesEnPoder(int idUsuario) throws SQLException;
}
