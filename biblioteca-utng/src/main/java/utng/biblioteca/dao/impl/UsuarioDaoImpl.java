package utng.biblioteca.dao.impl;

import utng.biblioteca.config.Conexion;
import utng.biblioteca.dao.interfaces.UsuarioDao;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.Rol;
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
import java.util.Optional;

public class UsuarioDaoImpl implements UsuarioDao {

    @Override
    public Optional<Usuario> autenticar(String usuario, String passwordHash) throws SQLException {
        String sql = "{call SP_Login(?, ?, ?)}";

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall(sql)) {

            cs.setString(1, usuario);
            cs.setString(2, passwordHash);
            cs.registerOutParameter(3, Types.INTEGER);

            boolean hayResultSet = cs.execute();

            // Cuando las credenciales fallan el SP no devuelve filas.
            while (!hayResultSet && cs.getUpdateCount() != -1) {
                hayResultSet = cs.getMoreResults();
            }

            Usuario encontrado = null;
            if (hayResultSet) {
                try (ResultSet rs = cs.getResultSet()) {
                    if (rs.next()) {
                        encontrado = Mapeos.usuario(rs);
                    }
                }
            }

            return Optional.ofNullable(encontrado);
        }
    }

    @Override
    public void registrarLogout(int idUsuario) throws SQLException {
        String sql = "INSERT INTO Acceso (idUsuario, tipoAcceso, resultado) VALUES (?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, "Logout");
            ps.setString(3, "Exitoso");
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM VW_Usuario WHERE idUsuario = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(Mapeos.usuario(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Usuario> listar(String texto, Integer idRol, Boolean soloActivos) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM VW_Usuario WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sql.append(" AND (nombre LIKE ?")
               .append(" OR ISNULL(numeroControl, '') LIKE ?")
               .append(" OR ISNULL(correo, '') LIKE ?")
               .append(" OR ISNULL(usuario, '') LIKE ?)");
            String patron = "%" + texto.trim() + "%";
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
        }

        if (idRol != null) {
            sql.append(" AND idRol = ?");
            parametros.add(idRol);
        }

        if (Boolean.TRUE.equals(soloActivos)) {
            sql.append(" AND estado = 1");
        }

        sql.append(" ORDER BY nombre");

        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(Mapeos.usuario(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public int insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO Usuario (numeroControl, nombre, correo, telefono, usuario, "
                   + "passwordHash, idRol, estado" + (Areas.disponible() ? ", idArea" : "") + ") "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?" + (Areas.disponible() ? ", ?" : "") + ")";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, vacioANulo(usuario.getNumeroControl()));
            ps.setString(2, usuario.getNombre());
            ps.setString(3, vacioANulo(usuario.getCorreo()));
            ps.setString(4, vacioANulo(usuario.getTelefono()));
            ps.setString(5, vacioANulo(usuario.getUsuario()));
            ps.setString(6, vacioANulo(usuario.getPasswordHash()));
            ps.setInt(7, usuario.getRol().getIdRol());
            ps.setBoolean(8, usuario.isEstado());
            if (Areas.disponible()) {
                asignarArea(ps, 9, usuario);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public void actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE Usuario SET numeroControl = ?, nombre = ?, correo = ?, telefono = ?, "
                   + "usuario = ?, idRol = ?, estado = ?"
                   + (Areas.disponible() ? ", idArea = ?" : "")
                   + " WHERE idUsuario = ?";

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, vacioANulo(usuario.getNumeroControl()));
            ps.setString(2, usuario.getNombre());
            ps.setString(3, vacioANulo(usuario.getCorreo()));
            ps.setString(4, vacioANulo(usuario.getTelefono()));
            ps.setString(5, vacioANulo(usuario.getUsuario()));
            ps.setInt(6, usuario.getRol().getIdRol());
            ps.setBoolean(7, usuario.isEstado());

            int siguiente = 8;
            if (Areas.disponible()) {
                asignarArea(ps, siguiente++, usuario);
            }
            ps.setInt(siguiente, usuario.getIdUsuario());

            ps.executeUpdate();
        }
    }

    @Override
    public void cambiarEstado(int idUsuario, boolean activo) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("UPDATE Usuario SET estado = ? WHERE idUsuario = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    @Override
    public void cambiarPassword(int idUsuario, String passwordHash) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("UPDATE Usuario SET passwordHash = ? WHERE idUsuario = ?")) {
            ps.setString(1, passwordHash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Rol> listarRoles() throws SQLException {
        List<Rol> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM Rol WHERE estado = 1 ORDER BY idRol");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(Mapeos.rol(rs));
            }
        }
        return lista;
    }

    @Override
    public List<Rol> listarRolesPublicos() throws SQLException {
        List<Rol> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM VW_RolPublico ORDER BY idRol");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(Mapeos.rol(rs));
            }
        }
        return lista;
    }

    @Override
    public Resultado registrarCuenta(Usuario usuario, String passwordHash, String rol)
            throws SQLException {

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall(Areas.disponible()
                     ? "{call SP_RegistrarCuenta(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}"
                     : "{call SP_RegistrarCuenta(?, ?, ?, ?, ?, ?, ?, ?, ?)}")) {

            cs.setString(1, usuario.getNombre());
            cs.setString(2, vacioANulo(usuario.getNumeroControl()));
            cs.setString(3, vacioANulo(usuario.getCorreo()));
            cs.setString(4, vacioANulo(usuario.getTelefono()));
            cs.setString(5, usuario.getUsuario());
            cs.setString(6, passwordHash);
            cs.setString(7, rol);

            // La migración 05_area.sql agrega el parámetro @idArea al final;
            // sin ella el procedimiento tiene un parámetro menos.
            int salida = 8;
            if (Areas.disponible()) {
                if (usuario.getArea() == null) {
                    cs.setNull(salida++, Types.INTEGER);
                } else {
                    cs.setInt(salida++, usuario.getArea().getIdArea());
                }
            }

            cs.registerOutParameter(salida, Types.INTEGER);
            cs.registerOutParameter(salida + 1, Types.VARCHAR);

            cs.execute();

            int idUsuario = cs.getInt(salida);
            boolean creado = !cs.wasNull() && idUsuario > 0;
            String mensaje = cs.getString(salida + 1);

            return creado
                    ? Resultado.ok(mensaje, idUsuario)
                    : Resultado.error(mensaje == null ? "No se pudo crear la cuenta." : mensaje);
        }
    }

    private static void asignarArea(PreparedStatement ps, int indice, Usuario usuario)
            throws SQLException {
        if (usuario.getArea() == null || usuario.getArea().getIdArea() <= 0) {
            ps.setNull(indice, Types.INTEGER);
        } else {
            ps.setInt(indice, usuario.getArea().getIdArea());
        }
    }

    /** Los indices unicos filtrados del esquema aceptan NULL pero no cadenas vacias repetidas. */
    private static String vacioANulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
