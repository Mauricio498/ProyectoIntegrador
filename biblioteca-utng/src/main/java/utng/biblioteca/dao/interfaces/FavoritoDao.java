package utng.biblioteca.dao.interfaces;

import utng.biblioteca.dto.MaterialCatalogo;

import java.sql.SQLException;
import java.util.List;

/** Materiales marcados como favoritos por un usuario. */
public interface FavoritoDao {

    List<MaterialCatalogo> listar(int idUsuario) throws SQLException;

    boolean esFavorito(int idUsuario, int idMaterial) throws SQLException;

    void agregar(int idUsuario, int idMaterial) throws SQLException;

    void quitar(int idUsuario, int idMaterial) throws SQLException;
}
