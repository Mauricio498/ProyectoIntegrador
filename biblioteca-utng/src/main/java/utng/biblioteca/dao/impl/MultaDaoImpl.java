package utng.biblioteca.dao.impl;

import utng.biblioteca.config.Conexion;
import utng.biblioteca.dao.interfaces.MultaDao;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.Multa;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MultaDaoImpl implements MultaDao {

    private static final String SQL_BASE =
            "SELECT MU.*, P.idUsuario, U.nombre AS usuarioNombre, M.titulo, E.codigoEjemplar "
          + "FROM Multa MU "
          + "INNER JOIN DetallePrestamo DP ON DP.idDetallePrestamo = MU.idDetallePrestamo "
          + "INNER JOIN Prestamo        P  ON P.idPrestamo         = DP.idPrestamo "
          + "INNER JOIN Usuario         U  ON U.idUsuario          = P.idUsuario "
          + "INNER JOIN Ejemplar        E  ON E.idEjemplar         = DP.idEjemplar "
          + "INNER JOIN Material        M  ON M.idMaterial         = E.idMaterial ";

    @Override
    public List<Multa> listar(Integer idUsuario, boolean soloPendientes) throws SQLException {
        StringBuilder sql = new StringBuilder(SQL_BASE).append("WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (idUsuario != null) {
            sql.append(" AND P.idUsuario = ?");
            parametros.add(idUsuario);
        }
        if (soloPendientes) {
            sql.append(" AND MU.estado = 'Pendiente'");
        }
        sql.append(" ORDER BY MU.fechaGeneracion DESC");

        List<Multa> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(Mapeos.multa(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Version plana para la tabla de administrador:
     * [idMulta, usuario, titulo, codigoEjemplar, diasRetraso, monto, estado].
     */
    @Override
    public List<String[]> listarConDetalle(Integer idUsuario, boolean soloPendientes) throws SQLException {
        StringBuilder sql = new StringBuilder(SQL_BASE).append("WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (idUsuario != null) {
            sql.append(" AND P.idUsuario = ?");
            parametros.add(idUsuario);
        }
        if (soloPendientes) {
            sql.append(" AND MU.estado = 'Pendiente'");
        }
        sql.append(" ORDER BY MU.fechaGeneracion DESC");

        List<String[]> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                            String.valueOf(rs.getInt("idMulta")),
                            rs.getString("usuarioNombre"),
                            rs.getString("titulo"),
                            rs.getString("codigoEjemplar"),
                            String.valueOf(rs.getInt("diasRetraso")),
                            String.valueOf(rs.getBigDecimal("monto")),
                            rs.getString("estado")
                    });
                }
            }
        }
        return lista;
    }

    @Override
    public Resultado registrarPago(int idMulta, Integer idAdministrador, BigDecimal monto) throws SQLException {
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_RegistrarPago(?, ?, ?, ?)}")) {

            cs.setInt(1, idMulta);
            if (idAdministrador == null) {
                cs.setNull(2, Types.INTEGER);
            } else {
                cs.setInt(2, idAdministrador);
            }
            cs.setBigDecimal(3, monto);
            cs.registerOutParameter(4, Types.VARCHAR);

            cs.execute();

            String mensaje = cs.getString(4);
            if (mensaje == null) {
                return Resultado.error("No se pudo registrar el pago.");
            }
            boolean exito = mensaje.startsWith("Multa saldada") || mensaje.startsWith("Pago parcial");
            return exito ? Resultado.ok(mensaje) : Resultado.error(mensaje);
        }
    }

    @Override
    public void condonar(int idMulta) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE Multa SET estado = 'Condonada' WHERE idMulta = ? AND estado = 'Pendiente'")) {
            ps.setInt(1, idMulta);
            ps.executeUpdate();
        }
    }

    @Override
    public BigDecimal adeudoDe(int idUsuario) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT dbo.FN_AdeudoUsuario(?)")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }
}
