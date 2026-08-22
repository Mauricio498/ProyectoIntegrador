package utng.biblioteca.dao.impl;

import utng.biblioteca.config.Conexion;
import utng.biblioteca.dao.interfaces.PrestamoDao;
import utng.biblioteca.dto.PrestamoDetalle;
import utng.biblioteca.dto.Resultado;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrestamoDaoImpl implements PrestamoDao {

    @Override
    public Resultado registrarPrestamo(int idUsuario, Integer idAdministrador, List<Integer> idsEjemplares)
            throws SQLException {

        if (idsEjemplares == null || idsEjemplares.isEmpty()) {
            return Resultado.error("Selecciona al menos un ejemplar.");
        }

        String lista = idsEjemplares.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_RegistrarPrestamo(?, ?, ?, ?, ?)}")) {

            cs.setInt(1, idUsuario);
            if (idAdministrador == null) {
                cs.setNull(2, Types.INTEGER);
            } else {
                cs.setInt(2, idAdministrador);
            }
            cs.setString(3, lista);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();

            int idPrestamo = cs.getInt(4);
            boolean generado = !cs.wasNull() && idPrestamo > 0;
            String mensaje = cs.getString(5);

            return generado
                    ? Resultado.ok(mensaje, idPrestamo)
                    : Resultado.error(mensaje == null ? "No se pudo registrar el prestamo." : mensaje);
        }
    }

    @Override
    public Resultado registrarDevolucion(int idDetallePrestamo, Integer idAdministrador, boolean perdido)
            throws SQLException {

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_RegistrarDevolucion(?, ?, ?, ?, ?)}")) {

            cs.setInt(1, idDetallePrestamo);
            if (idAdministrador == null) {
                cs.setNull(2, Types.INTEGER);
            } else {
                cs.setInt(2, idAdministrador);
            }
            cs.setBoolean(3, perdido);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();

            String mensaje = cs.getString(5);
            if (mensaje == null) {
                return Resultado.error("No se pudo registrar la devolucion.");
            }

            // El SP devuelve mensajes de error empezando con "Error" o describiendo
            // por que no se pudo continuar; los de exito siempre mencionan la devolucion.
            boolean exito = mensaje.startsWith("Devolucion");
            int idMulta = cs.getInt(4);
            Integer multaGenerada = cs.wasNull() ? null : idMulta;

            return exito ? Resultado.ok(mensaje, multaGenerada) : Resultado.error(mensaje);
        }
    }

    @Override
    public List<PrestamoDetalle> listar(Integer idUsuario, boolean soloPendientes, String texto)
            throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT * FROM VW_PrestamoDetalle WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (idUsuario != null) {
            sql.append(" AND idUsuario = ?");
            parametros.add(idUsuario);
        }

        if (soloPendientes) {
            sql.append(" AND estadoDetalle = 'Prestado'");
        }

        if (texto != null && !texto.isBlank()) {
            sql.append(" AND (titulo LIKE ? OR codigoEjemplar LIKE ? OR usuarioNombre LIKE ?")
               .append(" OR ISNULL(numeroControl, '') LIKE ?)");
            String patron = "%" + texto.trim() + "%";
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
        }

        sql.append(" ORDER BY CASE WHEN estadoDetalle = 'Prestado' THEN 0 ELSE 1 END, fechaLimite ASC");

        return consultar(sql.toString(), parametros);
    }

    @Override
    public List<PrestamoDetalle> listarRetrasados() throws SQLException {
        return consultar(
                "SELECT * FROM VW_PrestamoDetalle "
              + "WHERE estadoDetalle = 'Prestado' AND diasRetraso > 0 "
              + "ORDER BY diasRetraso DESC",
                List.of());
    }

    private List<PrestamoDetalle> consultar(String sql, List<Object> parametros) throws SQLException {
        List<PrestamoDetalle> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(Mapeos.prestamoDetalle(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public int actualizarVencidos() throws SQLException {
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_ActualizarPrestamosVencidos(?)}")) {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.execute();
            return cs.getInt(1);
        }
    }

    @Override
    public int materialesEnPoder(int idUsuario) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT dbo.FN_MaterialesEnPoder(?)")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
