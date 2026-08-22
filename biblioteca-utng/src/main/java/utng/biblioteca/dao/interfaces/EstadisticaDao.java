package utng.biblioteca.dao.interfaces;

import utng.biblioteca.dto.Conteo;
import utng.biblioteca.dto.EstadisticasGenerales;
import utng.biblioteca.dto.Periodo;
import utng.biblioteca.dto.PuntoSerie;
import utng.biblioteca.dto.FilaInventario;
import utng.biblioteca.dto.ResumenAcervo;
import utng.biblioteca.dto.ResumenRango;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Consultas agregadas para el tablero y el módulo de reportes.
 *
 * El agrupamiento por día, semana o mes lo resuelve SQL Server con
 * dbo.FN_InicioPeriodo, no Java: así la serie llega lista para graficarse.
 */
public interface EstadisticaDao {

    EstadisticasGenerales generales() throws SQLException;

    /** Números del encabezado para el rango elegido. */
    ResumenRango resumen(Date desde, Date hasta) throws SQLException;

    /** Serie de accesos: valor = exitosos, valorSecundario = fallidos. */
    List<PuntoSerie> accesosPorPeriodo(Date desde, Date hasta, Periodo periodo) throws SQLException;

    /** Serie de circulación: valor = préstamos, valorSecundario = devoluciones. */
    List<PuntoSerie> prestamosPorPeriodo(Date desde, Date hasta, Periodo periodo) throws SQLException;

    /** Serie de búsquedas: valor = total, valorSecundario = usuarios distintos. */
    List<PuntoSerie> busquedasPorPeriodo(Date desde, Date hasta, Periodo periodo) throws SQLException;

    List<Conteo> terminosMasBuscados(Date desde, Date hasta, int limite) throws SQLException;

    List<Conteo> materialesMasPrestados(Date desde, Date hasta, int limite) throws SQLException;

    List<Conteo> prestamosPorRol(Date desde, Date hasta) throws SQLException;

    /**
     * Préstamos agrupados por área académica.
     * Devuelve lista vacía si todavía no se ejecutó la migración 05_area.sql.
     */
    List<Conteo> prestamosPorArea(Date desde, Date hasta) throws SQLException;

    // ------------------------------------------------------------ acervo
    // Estos no reciben fechas: describen el estado actual de la biblioteca,
    // no la actividad de un periodo.

    ResumenAcervo resumenAcervo() throws SQLException;

    /** Materiales y ejemplares por tipo. El detalle lleva el conteo de ejemplares. */
    List<Conteo> acervoPorTipo() throws SQLException;

    List<Conteo> acervoPorEstadoEjemplar() throws SQLException;

    List<Conteo> acervoPorBiblioteca() throws SQLException;

    List<Conteo> acervoPorClasificacion(int limite) throws SQLException;

    List<Conteo> acervoPorEditorial(int limite) throws SQLException;

    /** @param soloConProblemas true = solo lo que necesita revisión. */
    List<FilaInventario> inventario(boolean soloConProblemas) throws SQLException;
}
