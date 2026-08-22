package utng.biblioteca.dao.impl;

import utng.biblioteca.config.Conexion;
import utng.biblioteca.dao.interfaces.SolicitudDao;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.SolicitudAdquisicion;
import utng.biblioteca.model.Usuario;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SolicitudDaoImpl implements SolicitudDao {

    @Override
    public List<SolicitudAdquisicion> listar(Integer idUsuario, String estado) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT S.*, U.nombre AS solicitante, A.nombre AS administrador "
              + "FROM SolicitudAdquisicion S "
              + "INNER JOIN Usuario U ON U.idUsuario = S.idUsuario "
              + "LEFT  JOIN Usuario A ON A.idUsuario = S.idAdministrador "
              + "WHERE 1 = 1");

        List<Object> parametros = new ArrayList<>();

        if (idUsuario != null) {
            sql.append(" AND S.idUsuario = ?");
            parametros.add(idUsuario);
        }
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND S.estado = ?");
            parametros.add(estado);
        }

        sql.append(" ORDER BY CASE WHEN S.estado = 'Pendiente' THEN 0 ELSE 1 END, S.fechaSolicitud DESC");

        List<SolicitudAdquisicion> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SolicitudAdquisicion s = Mapeos.solicitud(rs);

                    Usuario solicitante = new Usuario();
                    solicitante.setIdUsuario(rs.getInt("idUsuario"));
                    solicitante.setNombre(rs.getString("solicitante"));
                    s.setUsuario(solicitante);

                    String nombreAdmin = rs.getString("administrador");
                    if (nombreAdmin != null) {
                        Usuario admin = new Usuario();
                        admin.setIdUsuario(rs.getInt("idAdministrador"));
                        admin.setNombre(nombreAdmin);
                        s.setAdministrador(admin);
                    }

                    lista.add(s);
                }
            }
        }
        return lista;
    }

    @Override
    public int crear(int idUsuario, String titulo, String autor, String editorial,
                     String isbn, String descripcion) throws SQLException {

        String sql = "INSERT INTO SolicitudAdquisicion (idUsuario, titulo, autor, editorial, isbn, descripcion) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idUsuario);
            ps.setString(2, titulo);
            ps.setString(3, autor);
            ps.setString(4, editorial);
            ps.setString(5, isbn);
            ps.setString(6, descripcion);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public Resultado responder(int idSolicitud, int idAdministrador, String estado, String respuesta)
            throws SQLException {

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_ResponderSolicitud(?, ?, ?, ?, ?)}")) {

            cs.setInt(1, idSolicitud);
            cs.setInt(2, idAdministrador);
            cs.setString(3, estado);
            cs.setString(4, respuesta);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();

            String mensaje = cs.getString(5);
            if (mensaje == null) {
                return Resultado.error("No se pudo responder la solicitud.");
            }
            return mensaje.startsWith("Solicitud marcada")
                    ? Resultado.ok(mensaje)
                    : Resultado.error(mensaje);
        }
    }
}
