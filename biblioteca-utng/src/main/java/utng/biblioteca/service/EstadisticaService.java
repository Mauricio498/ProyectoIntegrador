package utng.biblioteca.service;

import utng.biblioteca.dao.impl.EstadisticaDaoImpl;
import utng.biblioteca.dao.interfaces.EstadisticaDao;
import utng.biblioteca.dto.Conteo;
import utng.biblioteca.dto.EstadisticasGenerales;
import utng.biblioteca.dto.Periodo;
import utng.biblioteca.dto.PuntoSerie;
import utng.biblioteca.dto.FilaInventario;
import utng.biblioteca.dto.ResumenAcervo;
import utng.biblioteca.dto.ResumenRango;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos agregados para el tablero y el módulo de reportes.
 *
 * Aquí se normaliza el rango de fechas antes de bajar a la base: si vienen
 * invertidas se intercambian, y si falta alguna se completa, para que la
 * pantalla nunca mande una consulta imposible.
 */
public class EstadisticaService extends ServicioBase {

    private final EstadisticaDao estadisticaDao;

    public EstadisticaService() {
        this(new EstadisticaDaoImpl());
    }

    public EstadisticaService(EstadisticaDao estadisticaDao) {
        this.estadisticaDao = estadisticaDao;
    }

    public EstadisticasGenerales generales() {
        return valor("obtener estadísticas", estadisticaDao::generales, EstadisticasGenerales::new);
    }

    public ResumenRango resumen(LocalDate desde, LocalDate hasta) {
        Rango r = new Rango(desde, hasta);
        return valor("obtener el resumen del periodo",
                () -> estadisticaDao.resumen(r.desde, r.hasta), ResumenRango::new);
    }

    public List<PuntoSerie> accesos(LocalDate desde, LocalDate hasta, Periodo periodo) {
        Rango r = new Rango(desde, hasta);
        return consultar("obtener accesos por periodo",
                () -> estadisticaDao.accesosPorPeriodo(r.desde, r.hasta, periodo(periodo)));
    }

    public List<PuntoSerie> circulacion(LocalDate desde, LocalDate hasta, Periodo periodo) {
        Rango r = new Rango(desde, hasta);
        return consultar("obtener préstamos por periodo",
                () -> estadisticaDao.prestamosPorPeriodo(r.desde, r.hasta, periodo(periodo)));
    }

    public List<PuntoSerie> busquedas(LocalDate desde, LocalDate hasta, Periodo periodo) {
        Rango r = new Rango(desde, hasta);
        return consultar("obtener búsquedas por periodo",
                () -> estadisticaDao.busquedasPorPeriodo(r.desde, r.hasta, periodo(periodo)));
    }

    public List<Conteo> terminosMasBuscados(LocalDate desde, LocalDate hasta, int limite) {
        Rango r = new Rango(desde, hasta);
        return consultar("obtener términos más buscados",
                () -> estadisticaDao.terminosMasBuscados(r.desde, r.hasta, limite));
    }

    public List<Conteo> materialesMasPrestados(LocalDate desde, LocalDate hasta, int limite) {
        Rango r = new Rango(desde, hasta);
        return consultar("obtener materiales más prestados",
                () -> estadisticaDao.materialesMasPrestados(r.desde, r.hasta, limite));
    }

    public List<Conteo> prestamosPorRol(LocalDate desde, LocalDate hasta) {
        Rango r = new Rango(desde, hasta);
        return consultar("obtener préstamos por rol",
                () -> estadisticaDao.prestamosPorRol(r.desde, r.hasta));
    }

    public List<Conteo> prestamosPorArea(LocalDate desde, LocalDate hasta) {
        Rango r = new Rango(desde, hasta);
        return consultar("obtener préstamos por área",
                () -> estadisticaDao.prestamosPorArea(r.desde, r.hasta));
    }

    // ---------------------------------------------------------------- acervo
    // Reportes del catálogo. No llevan rango de fechas porque describen lo que
    // hay en la biblioteca hoy, no la actividad de un periodo.

    public ResumenAcervo resumenAcervo() {
        return valor("obtener el resumen del acervo",
                estadisticaDao::resumenAcervo, ResumenAcervo::new);
    }

    public List<Conteo> acervoPorTipo() {
        return consultar("obtener el acervo por tipo", estadisticaDao::acervoPorTipo);
    }

    public List<Conteo> acervoPorEstadoEjemplar() {
        return consultar("obtener ejemplares por estado", estadisticaDao::acervoPorEstadoEjemplar);
    }

    public List<Conteo> acervoPorBiblioteca() {
        return consultar("obtener el acervo por biblioteca", estadisticaDao::acervoPorBiblioteca);
    }

    public List<Conteo> acervoPorClasificacion(int limite) {
        return consultar("obtener el acervo por clasificación",
                () -> estadisticaDao.acervoPorClasificacion(limite));
    }

    public List<Conteo> acervoPorEditorial(int limite) {
        return consultar("obtener el acervo por editorial",
                () -> estadisticaDao.acervoPorEditorial(limite));
    }

    /** @param soloConProblemas true = solo los materiales que necesitan revisión. */
    public List<FilaInventario> inventario(boolean soloConProblemas) {
        return consultar("obtener el inventario",
                () -> estadisticaDao.inventario(soloConProblemas));
    }

    private static Periodo periodo(Periodo periodo) {
        return periodo == null ? Periodo.DIA : periodo;
    }

    /**
     * Rango saneado: completa fechas faltantes y corrige el orden si el
     * usuario eligió un "desde" posterior al "hasta".
     */
    private static final class Rango {

        private final Date desde;
        private final Date hasta;

        private Rango(LocalDate inicio, LocalDate fin) {
            LocalDate a = inicio == null ? LocalDate.now().minusDays(30) : inicio;
            LocalDate b = fin == null ? LocalDate.now() : fin;

            if (a.isAfter(b)) {
                LocalDate intercambio = a;
                a = b;
                b = intercambio;
            }

            this.desde = Date.valueOf(a);
            this.hasta = Date.valueOf(b);
        }
    }
}
