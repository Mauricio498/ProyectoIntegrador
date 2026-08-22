package utng.biblioteca.dao.interfaces;

import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.Multa;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Multas y pagos. */
public interface MultaDao {

    /**
     * @param idUsuario       null = todas (vista de administrador)
     * @param soloPendientes  true = solo las que siguen sin pagarse
     */
    List<Multa> listar(Integer idUsuario, boolean soloPendientes) throws SQLException;

    /** Datos de contexto de una multa: quien la debe y por que material. */
    List<String[]> listarConDetalle(Integer idUsuario, boolean soloPendientes) throws SQLException;

    /** Ejecuta SP_RegistrarPago (el trigger impide sobrepagar). */
    Resultado registrarPago(int idMulta, Integer idAdministrador, BigDecimal monto) throws SQLException;

    void condonar(int idMulta) throws SQLException;

    /** Adeudo total pendiente de un usuario. */
    BigDecimal adeudoDe(int idUsuario) throws SQLException;
}
