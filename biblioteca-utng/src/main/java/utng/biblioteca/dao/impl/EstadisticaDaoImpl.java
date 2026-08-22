package utng.biblioteca.dao.impl;

import utng.biblioteca.config.Conexion;
import utng.biblioteca.dao.interfaces.EstadisticaDao;
import utng.biblioteca.dto.Conteo;
import utng.biblioteca.dto.EstadisticasGenerales;
import utng.biblioteca.dto.Periodo;
import utng.biblioteca.dto.PuntoSerie;
import utng.biblioteca.dto.FilaInventario;
import utng.biblioteca.dto.ResumenAcervo;
import utng.biblioteca.dto.ResumenRango;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstadisticaDaoImpl implements EstadisticaDao {

    @Override
    public EstadisticasGenerales generales() throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM VW_EstadisticasGenerales");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Mapeos.estadisticas(rs) : new EstadisticasGenerales();
        }
    }

    @Override
    public ResumenRango resumen(Date desde, Date hasta) throws SQLException {
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_ResumenRango(?, ?)}")) {

            cs.setDate(1, desde);
            cs.setDate(2, hasta);

            try (ResultSet rs = cs.executeQuery()) {
                ResumenRango r = new ResumenRango();
                if (rs.next()) {
                    r.setAccesos(rs.getInt("accesos"));
                    r.setUsuariosDistintos(rs.getInt("usuariosDistintos"));
                    r.setBusquedas(rs.getInt("busquedas"));
                    r.setPrestamos(rs.getInt("prestamos"));
                    r.setDevoluciones(rs.getInt("devoluciones"));
                    r.setMultasGeneradas(rs.getInt("multasGeneradas"));
                    r.setMontoCobrado(Mapeos.decimal(rs, "montoCobrado"));
                }
                return r;
            }
        }
    }

    @Override
    public List<PuntoSerie> accesosPorPeriodo(Date desde, Date hasta, Periodo periodo)
            throws SQLException {
        return serie("SP_AccesosPorPeriodo", desde, hasta, periodo, "exitosos", "fallidos");
    }

    @Override
    public List<PuntoSerie> prestamosPorPeriodo(Date desde, Date hasta, Periodo periodo)
            throws SQLException {
        return serie("SP_PrestamosPorPeriodo", desde, hasta, periodo, "prestamos", "devoluciones");
    }

    @Override
    public List<PuntoSerie> busquedasPorPeriodo(Date desde, Date hasta, Periodo periodo)
            throws SQLException {
        return serie("SP_BusquedasPorPeriodo", desde, hasta, periodo, "total", "usuarios");
    }

    /** Las tres series comparten forma: fecha del periodo y dos medidas. */
    private List<PuntoSerie> serie(String procedimiento, Date desde, Date hasta, Periodo periodo,
                                   String columnaValor, String columnaSecundaria) throws SQLException {

        List<PuntoSerie> lista = new ArrayList<>();

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call " + procedimiento + "(?, ?, ?)}")) {

            cs.setDate(1, desde);
            cs.setDate(2, hasta);
            cs.setString(3, periodo.getClaveSql());

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    PuntoSerie punto = new PuntoSerie();
                    punto.setPeriodo(Mapeos.fecha(rs, "periodo"));
                    punto.setValor(rs.getInt(columnaValor));
                    punto.setValorSecundario(rs.getInt(columnaSecundaria));
                    lista.add(punto);
                }
            }
        }
        return lista;
    }

    @Override
    public List<Conteo> terminosMasBuscados(Date desde, Date hasta, int limite) throws SQLException {
        List<Conteo> lista = new ArrayList<>();

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_TerminosMasBuscados(?, ?, ?)}")) {

            cs.setDate(1, desde);
            cs.setDate(2, hasta);
            cs.setInt(3, limite);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Conteo c = new Conteo();
                    c.setEtiqueta(rs.getString("termino"));
                    c.setCantidad(rs.getInt("cantidad"));
                    c.setDetalle(String.format("%.1f resultados en promedio",
                            rs.getBigDecimal("promedioResultados") == null
                                    ? 0.0 : rs.getBigDecimal("promedioResultados").doubleValue()));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    @Override
    public List<Conteo> materialesMasPrestados(Date desde, Date hasta, int limite) throws SQLException {
        List<Conteo> lista = new ArrayList<>();

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_MaterialesMasPrestados(?, ?, ?)}")) {

            cs.setDate(1, desde);
            cs.setDate(2, hasta);
            cs.setInt(3, limite);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Conteo c = new Conteo();
                    c.setEtiqueta(rs.getString("titulo"));
                    c.setCantidad(rs.getInt("vecesPrestado"));
                    c.setDetalle(rs.getString("autores"));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    @Override
    public List<Conteo> prestamosPorRol(Date desde, Date hasta) throws SQLException {
        return conteoSimple("SP_PrestamosPorRolEnRango", desde, hasta, "rol", "cantidad");
    }

    @Override
    public List<Conteo> prestamosPorArea(Date desde, Date hasta) throws SQLException {
        try {
            return conteoSimple("SP_PrestamosPorArea", desde, hasta, "area", "cantidad");
        } catch (SQLException e) {
            // La migración 05_area.sql es opcional: si no se ejecutó, el módulo
            // de reportes simplemente no muestra la gráfica por área.
            System.err.println("[EstadisticaDao] SP_PrestamosPorArea no disponible: " + e.getMessage());
            return List.of();
        }
    }

    // ---------------------------------------------------------------- acervo

    @Override
    public ResumenAcervo resumenAcervo() throws SQLException {
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_AcervoResumen}");
             ResultSet rs = cs.executeQuery()) {

            ResumenAcervo r = new ResumenAcervo();
            if (rs.next()) {
                r.setMaterialesActivos(rs.getInt("materialesActivos"));
                r.setMaterialesBaja(rs.getInt("materialesBaja"));
                r.setEjemplaresVigentes(rs.getInt("ejemplaresVigentes"));
                r.setEjemplaresDisponibles(rs.getInt("ejemplaresDisponibles"));
                r.setEjemplaresPrestados(rs.getInt("ejemplaresPrestados"));
                r.setEjemplaresReparacion(rs.getInt("ejemplaresReparacion"));
                r.setEjemplaresExtraviados(rs.getInt("ejemplaresExtraviados"));
                r.setAutores(rs.getInt("autores"));
                r.setEditoriales(rs.getInt("editoriales"));
                r.setMaterialesSinEjemplar(rs.getInt("materialesSinEjemplar"));
                r.setEjemplaresPorMaterial(Mapeos.decimal(rs, "ejemplaresPorMaterial"));
            }
            return r;
        }
    }

    @Override
    public List<Conteo> acervoPorTipo() throws SQLException {
        List<Conteo> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_AcervoPorTipo}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new Conteo(rs.getString("tipo"), rs.getInt("materiales"),
                        rs.getInt("ejemplares") + " ejemplares, "
                      + rs.getInt("disponibles") + " disponibles"));
            }
        }
        return lista;
    }

    @Override
    public List<Conteo> acervoPorEstadoEjemplar() throws SQLException {
        return conteoSinFechas("SP_AcervoPorEstadoEjemplar", "estado", "cantidad");
    }

    @Override
    public List<Conteo> acervoPorBiblioteca() throws SQLException {
        List<Conteo> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_AcervoPorBiblioteca}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new Conteo(rs.getString("biblioteca"), rs.getInt("ejemplares"),
                        rs.getInt("disponibles") + " disponibles"));
            }
        }
        return lista;
    }

    @Override
    public List<Conteo> acervoPorClasificacion(int limite) throws SQLException {
        return conteoConLimite("SP_AcervoPorClasificacion", limite, "clasificacion", "materiales");
    }

    @Override
    public List<Conteo> acervoPorEditorial(int limite) throws SQLException {
        return conteoConLimite("SP_AcervoPorEditorial", limite, "editorial", "materiales");
    }

    @Override
    public List<FilaInventario> inventario(boolean soloConProblemas) throws SQLException {
        List<FilaInventario> lista = new ArrayList<>();

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call SP_AcervoInventario(?)}")) {

            cs.setBoolean(1, soloConProblemas);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    FilaInventario f = new FilaInventario();
                    f.setIdMaterial(rs.getInt("idMaterial"));
                    f.setTitulo(rs.getString("titulo"));
                    f.setIsbn(rs.getString("isbn"));
                    f.setClasificacion(rs.getString("clasificacion"));
                    f.setTipo(rs.getString("tipo"));
                    f.setEditorial(rs.getString("editorial"));
                    f.setAutores(rs.getString("autores"));
                    f.setAnioPublicacion(Mapeos.enteroNulo(rs, "anioPublicacion"));
                    f.setEstadoMaterial(rs.getString("estadoMaterial"));
                    f.setEjemplares(rs.getInt("ejemplares"));
                    f.setDisponibles(rs.getInt("disponibles"));
                    f.setPrestados(rs.getInt("prestados"));
                    f.setNoUtilizables(rs.getInt("noUtilizables"));
                    f.setVecesPrestado(rs.getInt("vecesPrestado"));
                    f.setObservacion(rs.getString("observacion"));
                    lista.add(f);
                }
            }
        }
        return lista;
    }

    private List<Conteo> conteoSinFechas(String procedimiento, String colEtiqueta, String colCantidad)
            throws SQLException {
        List<Conteo> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call " + procedimiento + "}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                lista.add(new Conteo(rs.getString(colEtiqueta), rs.getInt(colCantidad), null));
            }
        }
        return lista;
    }

    private List<Conteo> conteoConLimite(String procedimiento, int limite,
                                         String colEtiqueta, String colCantidad) throws SQLException {
        List<Conteo> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call " + procedimiento + "(?)}")) {
            cs.setInt(1, limite);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Conteo(rs.getString(colEtiqueta), rs.getInt(colCantidad), null));
                }
            }
        }
        return lista;
    }

    private List<Conteo> conteoSimple(String procedimiento, Date desde, Date hasta,
                                      String columnaEtiqueta, String columnaCantidad)
            throws SQLException {

        List<Conteo> lista = new ArrayList<>();

        try (Connection cn = Conexion.obtener();
             CallableStatement cs = cn.prepareCall("{call " + procedimiento + "(?, ?)}")) {

            cs.setDate(1, desde);
            cs.setDate(2, hasta);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Conteo(rs.getString(columnaEtiqueta),
                                         rs.getInt(columnaCantidad), null));
                }
            }
        }
        return lista;
    }
}
