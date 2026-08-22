package utng.biblioteca.dao.impl;

import utng.biblioteca.config.Conexion;
import utng.biblioteca.dao.interfaces.SolicitudPrestamoDao;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.dto.SolicitudPrestamoDetalle;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SolicitudPrestamoDaoImpl implements SolicitudPrestamoDao {

    @Override
    public List<SolicitudPrestamoDetalle> listar(Integer idUsuario, String estado) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM VW_SolicitudPrestamo WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (idUsuario != null) {
            sql.append(" AND idUsuario = ?");
            parametros.add(idUsuario);
        }
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND estado = ?");
            parametros.add(estado);
        }

        sql.append(" ORDER BY CASE WHEN estado = 'Pendiente' THEN 0 ELSE 1 END, fechaSolicitud DESC");

        List<SolicitudPrestamoDetalle> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(Mapeos.solicitudPrestamo(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public Resultado solicitar(int idUsuario, int idMaterial) throws SQLException {
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_SolicitarPrestamo(?, ?, ?, ?)}")) {

            cs.setInt(1, idUsuario);
            cs.setInt(2, idMaterial);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(4, Types.VARCHAR);

            cs.execute();

            int idSolicitud = cs.getInt(3);
            boolean creada = !cs.wasNull() && idSolicitud > 0;
            String mensaje = cs.getString(4);

            return creada
                    ? Resultado.ok(mensaje, idSolicitud)
                    : Resultado.error(mensaje == null ? "No se pudo enviar la solicitud." : mensaje);
        }
    }

    @Override
    public Resultado responder(int idSolicitud, int idAdministrador, boolean aprobar, String observaciones)
            throws SQLException {

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_ResponderSolicitudPrestamo(?, ?, ?, ?, ?, ?)}")) {

            cs.setInt(1, idSolicitud);
            cs.setInt(2, idAdministrador);
            cs.setBoolean(3, aprobar);
            cs.setString(4, observaciones);
            cs.registerOutParameter(5, Types.INTEGER);
            cs.registerOutParameter(6, Types.VARCHAR);

            cs.execute();

            String mensaje = cs.getString(6);
            if (mensaje == null) {
                return Resultado.error("No se pudo responder la solicitud.");
            }

            int idPrestamo = cs.getInt(5);
            Integer generado = cs.wasNull() ? null : idPrestamo;

            boolean exito = mensaje.startsWith("Solicitud aprobada") || mensaje.startsWith("Solicitud rechazada");
            return exito ? Resultado.ok(mensaje, generado) : Resultado.error(mensaje);
        }
    }

    @Override
    public Resultado cancelar(int idSolicitud, int idUsuario) throws SQLException {
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_CancelarSolicitudPrestamo(?, ?, ?)}")) {

            cs.setInt(1, idSolicitud);
            cs.setInt(2, idUsuario);
            cs.registerOutParameter(3, Types.VARCHAR);

            cs.execute();

            String mensaje = cs.getString(3);
            if (mensaje == null) {
                return Resultado.error("No se pudo cancelar la solicitud.");
            }
            return mensaje.startsWith("Solicitud cancelada")
                    ? Resultado.ok(mensaje)
                    : Resultado.error(mensaje);
        }
    }

    @Override
    public int pendientesDe(int idUsuario) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT COUNT(*) FROM SolicitudPrestamo WHERE idUsuario = ? AND estado = 'Pendiente'")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
