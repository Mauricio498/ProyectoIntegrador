package utng.biblioteca.dao.interfaces;

import utng.biblioteca.model.Area;

import java.sql.SQLException;
import java.util.List;

/**
 * Catálogo de carreras y departamentos.
 *
 * La tabla llega con la migración 05_area.sql, que es opcional: si no se
 * ejecutó, la implementación devuelve lista vacía en lugar de fallar.
 */
public interface AreaDao {

    List<Area> listarActivas() throws SQLException;

    /** true si la tabla Area ya existe en la base. */
    boolean disponible();
}
