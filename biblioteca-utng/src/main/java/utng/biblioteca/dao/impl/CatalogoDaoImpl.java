package utng.biblioteca.dao.impl;

import utng.biblioteca.dto.RegistroImportacion;
import utng.biblioteca.dto.ResultadoImportacion;
import utng.biblioteca.config.Conexion;
import utng.biblioteca.dao.interfaces.CatalogoDao;
import utng.biblioteca.dto.MaterialCatalogo;
import utng.biblioteca.model.Autor;
import utng.biblioteca.model.Biblioteca;
import utng.biblioteca.model.Editorial;
import utng.biblioteca.model.Ejemplar;
import utng.biblioteca.model.Material;
import utng.biblioteca.model.TipoMaterial;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CatalogoDaoImpl implements CatalogoDao {
    private void validarRegistroImportacion(
        RegistroImportacion registro) throws SQLException {

    if (registro.getTitulo() == null
            || registro.getTitulo().isBlank()) {

        throw new SQLException(
                "Fila " + registro.getFilaExcel()
                + ": el título está vacío."
        );
    }

    if (registro.getNumeroAdquisicion() == null
            || registro.getNumeroAdquisicion().isBlank()) {

        throw new SQLException(
                "Fila " + registro.getFilaExcel()
                + ": el número de adquisición está vacío."
        );
    }

    if (registro.getTipoDetectado() == null
            || registro.getTipoDetectado().isBlank()) {

        throw new SQLException(
                "Fila " + registro.getFilaExcel()
                + ": no se pudo determinar el tipo."
        );
    }
}

    private int obtenerIdTipoMaterial(
            Connection cn,
            String nombre) throws SQLException {

        String sql = "SELECT idTipoMaterial "
                + "FROM TipoMaterial "
                + "WHERE nombre = ? AND estado = 1";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "No existe el tipo de material: "
                        + nombre);
    }

    private int obtenerOCrearEditorial(
            Connection cn,
            String nombre) throws SQLException {

        String buscar = "SELECT idEditorial "
                + "FROM Editorial "
                + "WHERE nombre = ?";

        try (PreparedStatement ps = cn.prepareStatement(buscar)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        String insertar = "INSERT INTO Editorial (nombre) "
                + "VALUES (?)";

        try (PreparedStatement ps = cn.prepareStatement(
                insertar,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "No se pudo crear la editorial: "
                        + nombre);
    }

    private int obtenerOCrearAutor(
            Connection cn,
            String nombre) throws SQLException {

        String buscar = "SELECT TOP 1 idAutor "
                + "FROM Autor "
                + "WHERE nombre = ? "
                + "ORDER BY idAutor";

        try (PreparedStatement ps = cn.prepareStatement(buscar)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        String insertar = "INSERT INTO Autor (nombre) "
                + "VALUES (?)";

        try (PreparedStatement ps = cn.prepareStatement(
                insertar,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "No se pudo crear el autor: "
                        + nombre);
    }

    private Integer buscarMaterialExistente(
            Connection cn,
            RegistroImportacion registro,
            Integer idEditorial) throws SQLException {

        // ------------------------------------------------------------
        // Primero por ISBN
        // ------------------------------------------------------------

        if (registro.getIsbn() != null
                && !registro.getIsbn().isBlank()) {

            String sql = "SELECT TOP 1 idMaterial "
                    + "FROM Material "
                    + "WHERE isbn = ? "
                    + "ORDER BY idMaterial";

            try (PreparedStatement ps = cn.prepareStatement(sql)) {

                ps.setString(
                        1,
                        registro.getIsbn());

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }

        // ------------------------------------------------------------
        // Si no hay ISBN, buscamos por título + año + editorial
        // ------------------------------------------------------------

        String sql = "SELECT TOP 1 idMaterial "
                + "FROM Material "
                + "WHERE titulo = ? "
                + "AND (anioPublicacion = ? "
                + "     OR (anioPublicacion IS NULL AND ? IS NULL)) "
                + "AND (idEditorial = ? "
                + "     OR (idEditorial IS NULL AND ? IS NULL)) "
                + "ORDER BY idMaterial";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, registro.getTitulo());

            if (registro.getAnioPublicacion() != null) {
                ps.setInt(
                        2,
                        registro.getAnioPublicacion());

                ps.setInt(
                        3,
                        registro.getAnioPublicacion());

            } else {

                ps.setNull(2, Types.SMALLINT);
                ps.setNull(3, Types.SMALLINT);
            }

            if (idEditorial != null) {

                ps.setInt(4, idEditorial);
                ps.setInt(5, idEditorial);

            } else {

                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.INTEGER);
            }

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return null;
    }

    private int insertarMaterialImportado(
            Connection cn,
            RegistroImportacion registro,
            Integer idEditorial,
            int idTipoMaterial) throws SQLException {

        String sql = "INSERT INTO Material "
                + "(titulo, isbn, anioPublicacion, "
                + " clasificacion, idEditorial, "
                + " idTipoMaterial, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'Activo')";

        try (PreparedStatement ps = cn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(
                    1,
                    registro.getTitulo());

            if (registro.getIsbn() != null
                    && !registro.getIsbn().isBlank()) {

                ps.setString(
                        2,
                        registro.getIsbn());

            } else {

                ps.setNull(2, Types.VARCHAR);
            }

            if (registro.getAnioPublicacion() != null) {

                ps.setInt(
                        3,
                        registro.getAnioPublicacion());

            } else {

                ps.setNull(3, Types.SMALLINT);
            }

            ps.setString(
                    4,
                    registro.getClasificacion());

            if (idEditorial != null) {

                ps.setInt(5, idEditorial);

            } else {

                ps.setNull(5, Types.INTEGER);
            }

            ps.setInt(
                    6,
                    idTipoMaterial);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "No se pudo insertar el material: "
                        + registro.getTitulo());
    }

    private void asociarAutor(
            Connection cn,
            int idMaterial,
            int idAutor) throws SQLException {

        String buscar = "SELECT 1 "
                + "FROM MaterialAutor "
                + "WHERE idMaterial = ? "
                + "AND idAutor = ?";

        try (PreparedStatement ps = cn.prepareStatement(buscar)) {

            ps.setInt(1, idMaterial);
            ps.setInt(2, idAutor);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return;
                }
            }
        }

        String insertar = "INSERT INTO MaterialAutor "
                + "(idMaterial, idAutor) "
                + "VALUES (?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(insertar)) {

            ps.setInt(1, idMaterial);
            ps.setInt(2, idAutor);

            ps.executeUpdate();
        }
    }

    private void validarBiblioteca(
            Connection cn,
            int idBiblioteca) throws SQLException {

        String sql = "SELECT 1 "
                + "FROM Biblioteca "
                + "WHERE idBiblioteca = ? "
                + "AND estado = 1";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idBiblioteca);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "La biblioteca "
                                    + idBiblioteca
                                    + " no existe o está inactiva.");
                }
            }
        }
    }

    private int insertarEjemplarImportado(
            Connection cn,
            RegistroImportacion registro,
            int idMaterial) throws SQLException {

        String insertar =
                "INSERT INTO Ejemplar "
            + "(idMaterial, idBiblioteca, "
            + " numeroAdquisicion, fechaIngreso, estado) "
            + "OUTPUT INSERTED.idEjemplar "
            + "VALUES (?, ?, ?, ?, 'Disponible')";

        int idEjemplar;

        try (PreparedStatement ps =
                    cn.prepareStatement(insertar)) {

            ps.setInt(
                    1,
                    idMaterial
            );

            ps.setInt(
                    2,
                    registro.getIdBiblioteca()
            );

            ps.setString(
                    3,
                    registro.getNumeroAdquisicion()
            );

            if (registro.getFechaIngreso() != null) {

                ps.setDate(
                        4,
                        java.sql.Date.valueOf(
                                registro.getFechaIngreso()
                        )
                );

            } else {

                ps.setNull(
                        4,
                        Types.DATE
                );
            }

            try (ResultSet rs =
                        ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo obtener el ID del ejemplar."
                    );
                }

                idEjemplar = rs.getInt(1);
            }
        }

        String codigo =
                String.format(
                        "IMP-%06d",
                        idEjemplar
                );

        String actualizar =
                "UPDATE Ejemplar "
            + "SET codigoEjemplar = ? "
            + "WHERE idEjemplar = ?";

        try (PreparedStatement ps =
                    cn.prepareStatement(actualizar)) {

            ps.setString(1, codigo);
            ps.setInt(2, idEjemplar);

            ps.executeUpdate();
        }

        return idEjemplar;
    }

    @Override
    public ResultadoImportacion importarExcel(List<RegistroImportacion> registros) throws SQLException {
        if (registros == null || registros.isEmpty()) {
            return new ResultadoImportacion(0, 0, 0, 0);
        }
        try (Connection cn = Conexion.obtener()) {
            boolean autoCommitOriginal = cn.getAutoCommit();
            cn.setAutoCommit(false);
            try {
                int materialesCreados = 0;
                int materialesReutilizados = 0;
                int ejemplaresCreados = 0;
                for (RegistroImportacion registro : registros) {
                    validarRegistroImportacion(registro);

                    // ----------------------------------------------------
                    // 1. Obtener el tipo de material
                    // ----------------------------------------------------

                    int idTipoMaterial = obtenerIdTipoMaterial(
                            cn,
                            registro.getTipoDetectado());

                    // ----------------------------------------------------
                    // 2. Obtener o crear editorial
                    // ----------------------------------------------------

                    Integer idEditorial = null;

                    if (registro.getEditorial() != null
                            && !registro.getEditorial().isBlank()) {

                        idEditorial = obtenerOCrearEditorial(
                                cn,
                                registro.getEditorial());
                    }

                    // ----------------------------------------------------
                    // 3. Buscar si el material ya existe
                    // ----------------------------------------------------

                    Integer idMaterial = buscarMaterialExistente(
                            cn,
                            registro,
                            idEditorial);

                    // ----------------------------------------------------
                    // 4. Si no existe, crear material
                    // ----------------------------------------------------

                    if (idMaterial == null) {

                        idMaterial = insertarMaterialImportado(
                                cn,
                                registro,
                                idEditorial,
                                idTipoMaterial);

                        materialesCreados++;

                    } else {

                        materialesReutilizados++;
                    }

                    // ----------------------------------------------------
                    // 5. Autor
                    // ----------------------------------------------------

                    if (registro.getAutor() != null
                            && !registro.getAutor().isBlank()) {

                        int idAutor = obtenerOCrearAutor(
                                cn,
                                registro.getAutor());

                        asociarAutor(
                                cn,
                                idMaterial,
                                idAutor);
                    }

                    // ----------------------------------------------------
                    // 6. Biblioteca
                    // ----------------------------------------------------

                    validarBiblioteca(
                            cn,
                            registro.getIdBiblioteca());

                    // ----------------------------------------------------
                    // 7. Ejemplar
                    // ----------------------------------------------------

                    insertarEjemplarImportado(
                            cn,
                            registro,
                            idMaterial);

                    ejemplaresCreados++;
                }

                // --------------------------------------------------------
                // TODO salió correctamente
                // --------------------------------------------------------

                cn.commit();

                return new ResultadoImportacion(
                        registros.size(),
                        materialesCreados,
                        materialesReutilizados,
                        ejemplaresCreados);

            } catch (SQLException | RuntimeException e) {

                // --------------------------------------------------------
                // ALGO FALLÓ
                // --------------------------------------------------------

                cn.rollback();

                throw e;

            } finally {

                cn.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    private static final String SQL_EJEMPLAR = "SELECT E.idEjemplar, E.codigoEjemplar, E.numeroAdquisicion, E.fechaIngreso, E.estado, "
            + "       M.idMaterial, M.titulo AS tituloMaterial, "
            + "       B.idBiblioteca, B.nombre AS nombreBiblioteca "
            + "FROM Ejemplar E "
            + "INNER JOIN Material   M ON M.idMaterial   = E.idMaterial "
            + "INNER JOIN Biblioteca B ON B.idBiblioteca = E.idBiblioteca ";

    @Override
    public List<MaterialCatalogo> buscar(String texto, Integer idTipoMaterial, boolean soloDisponibles)
            throws SQLException {
        return buscarConFiltros(texto, idTipoMaterial, soloDisponibles, false);
    }

    @Override
    public List<MaterialCatalogo> buscarIncluyendoBajas(String texto, Integer idTipoMaterial,
            boolean soloDisponibles) throws SQLException {
        return buscarConFiltros(texto, idTipoMaterial, soloDisponibles, true);
    }

    private List<MaterialCatalogo> buscarConFiltros(String texto, Integer idTipoMaterial,
            boolean soloDisponibles, boolean incluirBajas)
            throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT * FROM VW_CatalogoMaterial WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (!incluirBajas) {
            sql.append(" AND estadoMaterial = ?");
            parametros.add("Activo");
        }

        if (texto != null && !texto.isBlank()) {
            sql.append(" AND (titulo LIKE ?")
                    .append(" OR ISNULL(isbn, '') LIKE ?")
                    .append(" OR autores LIKE ?")
                    .append(" OR ISNULL(clasificacion, '') LIKE ?)");
            String patron = "%" + texto.trim() + "%";
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
        }

        if (idTipoMaterial != null) {
            sql.append(" AND idTipoMaterial = ?");
            parametros.add(idTipoMaterial);
        }

        if (soloDisponibles) {
            sql.append(" AND ejemplaresDisponibles > 0 AND esPrestable = 1");
        }

        sql.append(" ORDER BY titulo");

        return consultarCatalogo(sql.toString(), parametros);
    }

    @Override
    public Optional<MaterialCatalogo> buscarPorId(int idMaterial) throws SQLException {
        List<MaterialCatalogo> lista = consultarCatalogo(
                "SELECT * FROM VW_CatalogoMaterial WHERE idMaterial = ?",
                List.of(idMaterial));
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    @Override
    public List<MaterialCatalogo> destacados(int limite) throws SQLException {
        String sql = "SELECT C.* FROM VW_CatalogoMaterial C "
                + "INNER JOIN VW_MaterialesPopulares P ON P.idMaterial = C.idMaterial "
                + "WHERE C.estadoMaterial = ? AND C.esPrestable = 1 "
                + "ORDER BY P.vecesPrestado DESC, C.titulo ASC "
                + "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        return consultarCatalogo(sql, List.of("Activo", limite));
    }

    private List<MaterialCatalogo> consultarCatalogo(String sql, List<Object> parametros) throws SQLException {
        List<MaterialCatalogo> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement(sql)) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(Mapeos.materialCatalogo(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public int insertarMaterial(Material material, List<Integer> idsAutores) throws SQLException {
        String sql = "INSERT INTO Material (titulo, isbn, anioPublicacion, clasificacion, "
                + "idEditorial, idTipoMaterial, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                int idMaterial;
                try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, material.getTitulo());
                    ps.setString(2, material.getIsbn());
                    if (material.getAnioPublicacion() > 0) {
                        ps.setInt(3, material.getAnioPublicacion());
                    } else {
                        ps.setNull(3, Types.SMALLINT);
                    }
                    ps.setString(4, material.getClasificacion());
                    if (material.getEditorial() != null && material.getEditorial().getIdEditorial() > 0) {
                        ps.setInt(5, material.getEditorial().getIdEditorial());
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }
                    ps.setInt(6, material.getTipoMaterial().getIdTipoMaterial());
                    ps.setString(7, material.getEstado() == null ? "Activo" : material.getEstado());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        idMaterial = rs.next() ? rs.getInt(1) : 0;
                    }
                }

                reemplazarAutores(cn, idMaterial, idsAutores);
                cn.commit();
                return idMaterial;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    @Override
    public void actualizarMaterial(Material material, List<Integer> idsAutores) throws SQLException {
        String sql = "UPDATE Material SET titulo = ?, isbn = ?, anioPublicacion = ?, clasificacion = ?, "
                + "idEditorial = ?, idTipoMaterial = ?, estado = ? WHERE idMaterial = ?";

        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, material.getTitulo());
                    ps.setString(2, material.getIsbn());
                    if (material.getAnioPublicacion() > 0) {
                        ps.setInt(3, material.getAnioPublicacion());
                    } else {
                        ps.setNull(3, Types.SMALLINT);
                    }
                    ps.setString(4, material.getClasificacion());
                    if (material.getEditorial() != null && material.getEditorial().getIdEditorial() > 0) {
                        ps.setInt(5, material.getEditorial().getIdEditorial());
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }
                    ps.setInt(6, material.getTipoMaterial().getIdTipoMaterial());
                    ps.setString(7, material.getEstado() == null ? "Activo" : material.getEstado());
                    ps.setInt(8, material.getIdMaterial());
                    ps.executeUpdate();
                }

                reemplazarAutores(cn, material.getIdMaterial(), idsAutores);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void reemplazarAutores(Connection cn, int idMaterial, List<Integer> idsAutores) throws SQLException {
        if (idsAutores == null) {
            return;
        }

        try (PreparedStatement borrar = cn.prepareStatement("DELETE FROM MaterialAutor WHERE idMaterial = ?")) {
            borrar.setInt(1, idMaterial);
            borrar.executeUpdate();
        }

        if (idsAutores.isEmpty()) {
            return;
        }

        try (PreparedStatement insertar = cn
                .prepareStatement("INSERT INTO MaterialAutor (idMaterial, idAutor) VALUES (?, ?)")) {
            for (Integer idAutor : idsAutores) {
                insertar.setInt(1, idMaterial);
                insertar.setInt(2, idAutor);
                insertar.addBatch();
            }
            insertar.executeBatch();
        }
    }

    @Override
    public void cambiarEstadoMaterial(int idMaterial, String estado) throws SQLException {
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement("UPDATE Material SET estado = ? WHERE idMaterial = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idMaterial);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Ejemplar> listarEjemplares(int idMaterial) throws SQLException {
        return consultarEjemplares(SQL_EJEMPLAR + "WHERE E.idMaterial = ? ORDER BY E.codigoEjemplar",
                idMaterial);
    }

    @Override
    public List<Ejemplar> listarEjemplaresDisponibles(int idMaterial) throws SQLException {
        return consultarEjemplares(
                SQL_EJEMPLAR + "WHERE E.idMaterial = ? AND E.estado = 'Disponible' "
                        + "ORDER BY E.codigoEjemplar",
                idMaterial);
    }

    private List<Ejemplar> consultarEjemplares(String sql, int idMaterial) throws SQLException {
        List<Ejemplar> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(Mapeos.ejemplar(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public int insertarEjemplar(Ejemplar ejemplar) throws SQLException {
        String sql = "INSERT INTO Ejemplar (idMaterial, idBiblioteca, codigoEjemplar, "
                + "numeroAdquisicion, fechaIngreso, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, ejemplar.getMaterial().getIdMaterial());
            ps.setInt(2, ejemplar.getBiblioteca().getIdBiblioteca());
            ps.setString(3, ejemplar.getCodigoEjemplar());
            ps.setString(4, ejemplar.getNumeroAdquisicion());
            if (ejemplar.getFechaIngreso() != null) {
                ps.setDate(5, Date.valueOf(ejemplar.getFechaIngreso()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, ejemplar.getEstado() == null ? "Disponible" : ejemplar.getEstado());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public void cambiarEstadoEjemplar(int idEjemplar, String estado) throws SQLException {
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement("UPDATE Ejemplar SET estado = ? WHERE idEjemplar = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idEjemplar);
            ps.executeUpdate();
        }
    }

    @Override
    public List<TipoMaterial> listarTiposMaterial() throws SQLException {
        List<TipoMaterial> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT * FROM TipoMaterial WHERE estado = 1 ORDER BY nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(Mapeos.tipoMaterial(rs));
            }
        }
        return lista;
    }

    @Override
    public List<Editorial> listarEditoriales() throws SQLException {
        List<Editorial> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT * FROM Editorial WHERE estado = 1 ORDER BY nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(Mapeos.editorial(rs));
            }
        }
        return lista;
    }

    @Override
    public List<Autor> listarAutores() throws SQLException {
        List<Autor> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement("SELECT * FROM Autor ORDER BY nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(Mapeos.autor(rs));
            }
        }
        return lista;
    }

    @Override
    public List<Autor> autoresDeMaterial(int idMaterial) throws SQLException {
        String sql = "SELECT A.* FROM Autor A "
                + "INNER JOIN MaterialAutor MA ON MA.idAutor = A.idAutor "
                + "WHERE MA.idMaterial = ? ORDER BY A.nombre";
        List<Autor> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(Mapeos.autor(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public List<Biblioteca> listarBibliotecas() throws SQLException {
        List<Biblioteca> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT * FROM Biblioteca WHERE estado = 1 ORDER BY nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(Mapeos.biblioteca(rs));
            }
        }
        return lista;
    }

    @Override
    public int insertarAutor(String nombre) throws SQLException {
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO Autor (nombre) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public int insertarEditorial(String nombre) throws SQLException {
        try (Connection cn = Conexion.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO Editorial (nombre) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public void registrarBusqueda(Integer idUsuario, String termino, String filtros, int resultados)
            throws SQLException {
        try (Connection cn = Conexion.obtener();
                CallableStatement cs = cn.prepareCall("{call SP_RegistrarBusqueda(?, ?, ?, ?)}")) {

            if (idUsuario == null) {
                cs.setNull(1, Types.INTEGER);
            } else {
                cs.setInt(1, idUsuario);
            }
            cs.setString(2, termino == null ? "" : termino);
            cs.setString(3, filtros);
            cs.setInt(4, resultados);
            cs.execute();
        }
    }
}
