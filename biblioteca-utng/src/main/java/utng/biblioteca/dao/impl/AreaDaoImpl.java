package utng.biblioteca.dao.impl;

import utng.biblioteca.config.Conexion;
import utng.biblioteca.dao.interfaces.AreaDao;
import utng.biblioteca.model.Area;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AreaDaoImpl implements AreaDao {

    /** Se consulta una sola vez por ejecución; la tabla no aparece a medio uso. */
    private static Boolean tablaPresente;

    @Override
    public List<Area> listarActivas() throws SQLException {
        if (!disponible()) {
            return List.of();
        }

        List<Area> lista = new ArrayList<>();

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT * FROM VW_AreaActiva ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Area a = new Area();
                a.setIdArea(rs.getInt("idArea"));
                a.setNombre(rs.getString("nombre"));
                a.setSiglas(rs.getString("siglas"));
                a.setEstado(rs.getBoolean("estado"));
                lista.add(a);
            }
        }
        return lista;
    }

    @Override
    public synchronized boolean disponible() {
        if (tablaPresente != null) {
            return tablaPresente;
        }

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT CASE WHEN OBJECT_ID('Area', 'U') IS NULL THEN 0 ELSE 1 END");
             ResultSet rs = ps.executeQuery()) {

            tablaPresente = rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            System.err.println("[AreaDao] No se pudo verificar la tabla Area: " + e.getMessage());
            tablaPresente = false;
        }

        if (!tablaPresente) {
            System.out.println("Tabla Area no encontrada; ejecuta sql/05_area.sql "
                             + "si quieres capturar el área académica.");
        }

        return tablaPresente;
    }
}
