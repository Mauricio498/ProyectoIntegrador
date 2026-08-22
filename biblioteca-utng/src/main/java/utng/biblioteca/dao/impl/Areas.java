package utng.biblioteca.dao.impl;

import utng.biblioteca.dao.interfaces.AreaDao;

/**
 * Atajo para preguntar una sola vez si la migración 05_area.sql ya se
 * ejecutó, sin que cada DAO tenga que crear su propio AreaDao.
 *
 * Existe porque el área es opcional: las sentencias SQL de Usuario cambian
 * según haya o no columna idArea, y esa decisión se toma en varios puntos.
 */
final class Areas {

    private static final AreaDao DAO = new AreaDaoImpl();

    private Areas() {
    }

    static boolean disponible() {
        return DAO.disponible();
    }
}
