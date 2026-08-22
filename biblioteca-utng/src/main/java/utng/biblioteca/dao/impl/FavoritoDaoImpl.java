package utng.biblioteca.dao.impl;

import utng.biblioteca.config.Conexion;
import utng.biblioteca.dao.interfaces.FavoritoDao;
import utng.biblioteca.dto.MaterialCatalogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoritoDaoImpl implements FavoritoDao {

    @Override
    public List<MaterialCatalogo> listar(int idUsuario) throws SQLException {
        String sql = "SELECT C.* FROM VW_CatalogoMaterial C "
                   + "INNER JOIN Favorito F ON F.idMaterial = C.idMaterial "
                   + "WHERE F.idUsuario = ? ORDER BY F.fechaAgregado DESC";

        List<MaterialCatalogo> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(Mapeos.materialCatalogo(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public boolean esFavorito(int idUsuario, int idMaterial) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT 1 FROM Favorito WHERE idUsuario = ? AND idMaterial = ?")) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void agregar(int idUsuario, int idMaterial) throws SQLException {
        if (esFavorito(idUsuario, idMaterial)) {
            return;
        }
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO Favorito (idUsuario, idMaterial) VALUES (?, ?)")) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idMaterial);
            ps.executeUpdate();
        }
    }

    @Override
    public void quitar(int idUsuario, int idMaterial) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(
                     "DELETE FROM Favorito WHERE idUsuario = ? AND idMaterial = ?")) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idMaterial);
            ps.executeUpdate();
        }
    }
}
