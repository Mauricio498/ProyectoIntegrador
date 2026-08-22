package utng.biblioteca.dao.impl;

import utng.biblioteca.dto.EstadisticasGenerales;
import utng.biblioteca.dto.MaterialCatalogo;
import utng.biblioteca.dto.PrestamoDetalle;
import utng.biblioteca.dto.SolicitudPrestamoDetalle;
import utng.biblioteca.model.Area;
import utng.biblioteca.model.Autor;
import utng.biblioteca.model.Biblioteca;
import utng.biblioteca.model.Editorial;
import utng.biblioteca.model.Ejemplar;
import utng.biblioteca.model.Material;
import utng.biblioteca.model.Multa;
import utng.biblioteca.model.Rol;
import utng.biblioteca.model.SolicitudAdquisicion;
import utng.biblioteca.model.TipoMaterial;
import utng.biblioteca.model.Usuario;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Conversion de ResultSet a objetos del modelo.
 *
 * Se concentra aqui para que los DAO solo se ocupen del SQL y para que un
 * cambio de columna se corrija en un solo lugar.
 */
final class Mapeos {

    private Mapeos() {
    }

    static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        Date d = rs.getDate(columna);
        return d == null ? null : d.toLocalDate();
    }

    static LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        Timestamp t = rs.getTimestamp(columna);
        return t == null ? null : t.toLocalDateTime();
    }

    static Integer enteroNulo(ResultSet rs, String columna) throws SQLException {
        int v = rs.getInt(columna);
        return rs.wasNull() ? null : v;
    }

    static BigDecimal decimal(ResultSet rs, String columna) throws SQLException {
        BigDecimal v = rs.getBigDecimal(columna);
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Mapea una fila de VW_Usuario (trae usuario y rol en el mismo renglon). */
    static Usuario usuario(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setIdRol(rs.getInt("idRol"));
        rol.setNombre(rs.getString("rol"));
        rol.setDescripcion(rs.getString("rolDescripcion"));
        rol.setMaxMateriales(rs.getInt("maxMateriales"));
        rol.setDiasPrestamo(rs.getInt("diasPrestamo"));
        rol.setUnidadPlazo(tieneColumna(rs, "unidadPlazo") ? rs.getString("unidadPlazo") : "HABILES");
        rol.setEstado(rs.getBoolean("rolActivo"));

        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("idUsuario"));
        u.setNumeroControl(rs.getString("numeroControl"));
        u.setNombre(rs.getString("nombre"));
        u.setCorreo(rs.getString("correo"));
        u.setTelefono(rs.getString("telefono"));
        u.setUsuario(rs.getString("usuario"));
        u.setPasswordHash(rs.getString("passwordHash"));
        u.setEstado(rs.getBoolean("estado"));
        u.setFechaRegistro(fechaHora(rs, "fechaRegistro"));
        u.setRol(rol);
        u.setArea(areaOpcional(rs));
        return u;
    }

    /**
     * Lee el área de una fila de VW_Usuario.
     *
     * La vista solo trae estas columnas después de ejecutar la migración
     * 05_area.sql, así que la ausencia se trata como "sin área" en lugar de
     * como error: el sistema funciona igual sin esa migración.
     */
    private static Area areaOpcional(ResultSet rs) throws SQLException {
        if (!tieneColumna(rs, "idArea")) {
            return null;
        }

        Integer idArea = enteroNulo(rs, "idArea");
        if (idArea == null) {
            return null;
        }

        Area area = new Area();
        area.setIdArea(idArea);
        area.setNombre(rs.getString("area"));
        area.setSiglas(rs.getString("areaSiglas"));
        return area;
    }

    private static boolean tieneColumna(ResultSet rs, String nombre) throws SQLException {
        java.sql.ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (nombre.equalsIgnoreCase(meta.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    static Rol rol(ResultSet rs) throws SQLException {
        Rol r = new Rol();
        r.setIdRol(rs.getInt("idRol"));
        r.setNombre(rs.getString("nombre"));
        r.setDescripcion(rs.getString("descripcion"));
        r.setMaxMateriales(rs.getInt("maxMateriales"));
        r.setDiasPrestamo(rs.getInt("diasPrestamo"));
        r.setUnidadPlazo(tieneColumna(rs, "unidadPlazo") ? rs.getString("unidadPlazo") : "HABILES");
        r.setEstado(rs.getBoolean("estado"));
        return r;
    }

    static MaterialCatalogo materialCatalogo(ResultSet rs) throws SQLException {
        MaterialCatalogo m = new MaterialCatalogo();
        m.setIdMaterial(rs.getInt("idMaterial"));
        m.setTitulo(rs.getString("titulo"));
        m.setIsbn(rs.getString("isbn"));
        m.setAnioPublicacion(enteroNulo(rs, "anioPublicacion"));
        m.setClasificacion(rs.getString("clasificacion"));
        m.setEstadoMaterial(rs.getString("estadoMaterial"));
        m.setIdTipoMaterial(rs.getInt("idTipoMaterial"));
        m.setTipoMaterial(rs.getString("tipoMaterial"));
        m.setEsPrestable(rs.getBoolean("esPrestable"));
        m.setIdEditorial(enteroNulo(rs, "idEditorial"));
        m.setEditorial(rs.getString("editorial"));
        m.setAutores(rs.getString("autores"));
        m.setTotalEjemplares(rs.getInt("totalEjemplares"));
        m.setEjemplaresDisponibles(rs.getInt("ejemplaresDisponibles"));
        return m;
    }

    static PrestamoDetalle prestamoDetalle(ResultSet rs) throws SQLException {
        PrestamoDetalle p = new PrestamoDetalle();
        p.setIdDetallePrestamo(rs.getInt("idDetallePrestamo"));
        p.setIdPrestamo(rs.getInt("idPrestamo"));
        p.setIdUsuario(rs.getInt("idUsuario"));
        p.setUsuarioNombre(rs.getString("usuarioNombre"));
        p.setNumeroControl(rs.getString("numeroControl"));
        p.setIdAdministrador(enteroNulo(rs, "idAdministrador"));
        p.setFechaPrestamo(fechaHora(rs, "fechaPrestamo"));
        p.setFechaLimite(fecha(rs, "fechaLimite"));
        p.setEstadoPrestamo(rs.getString("estadoPrestamo"));
        p.setIdEjemplar(rs.getInt("idEjemplar"));
        p.setCodigoEjemplar(rs.getString("codigoEjemplar"));
        p.setEstadoEjemplar(rs.getString("estadoEjemplar"));
        p.setIdMaterial(rs.getInt("idMaterial"));
        p.setTitulo(rs.getString("titulo"));
        p.setIsbn(rs.getString("isbn"));
        p.setIdBiblioteca(rs.getInt("idBiblioteca"));
        p.setBiblioteca(rs.getString("biblioteca"));
        p.setFechaDevolucion(fecha(rs, "fechaDevolucion"));
        p.setEstadoDetalle(rs.getString("estadoDetalle"));
        p.setDiasRetraso(rs.getInt("diasRetraso"));
        return p;
    }

    static EstadisticasGenerales estadisticas(ResultSet rs) throws SQLException {
        EstadisticasGenerales e = new EstadisticasGenerales();
        e.setTotalMateriales(rs.getInt("totalMateriales"));
        e.setTotalEjemplares(rs.getInt("totalEjemplares"));
        e.setEjemplaresDisponibles(rs.getInt("ejemplaresDisponibles"));
        e.setEjemplaresPrestados(rs.getInt("ejemplaresPrestados"));
        e.setUsuariosActivos(rs.getInt("usuariosActivos"));
        e.setPrestamosActivos(rs.getInt("prestamosActivos"));
        e.setPrestamosRetrasados(rs.getInt("prestamosRetrasados"));
        e.setSolicitudesPendientes(rs.getInt("solicitudesPendientes"));
        e.setSolicitudesPrestamo(rs.getInt("solicitudesPrestamo"));
        e.setMultasPendientes(decimal(rs, "multasPendientes"));
        return e;
    }

    static TipoMaterial tipoMaterial(ResultSet rs) throws SQLException {
        TipoMaterial t = new TipoMaterial();
        t.setIdTipoMaterial(rs.getInt("idTipoMaterial"));
        t.setNombre(rs.getString("nombre"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setEsPrestable(rs.getBoolean("esPrestable"));
        t.setEstado(rs.getBoolean("estado"));
        return t;
    }

    static Editorial editorial(ResultSet rs) throws SQLException {
        Editorial e = new Editorial();
        e.setIdEditorial(rs.getInt("idEditorial"));
        e.setNombre(rs.getString("nombre"));
        e.setEstado(rs.getBoolean("estado"));
        return e;
    }

    static Autor autor(ResultSet rs) throws SQLException {
        Autor a = new Autor();
        a.setIdAutor(rs.getInt("idAutor"));
        a.setNombre(rs.getString("nombre"));
        return a;
    }

    static Biblioteca biblioteca(ResultSet rs) throws SQLException {
        Biblioteca b = new Biblioteca();
        b.setIdBiblioteca(rs.getInt("idBiblioteca"));
        b.setNombre(rs.getString("nombre"));
        b.setUbicacion(rs.getString("ubicacion"));
        b.setEstado(rs.getBoolean("estado"));
        return b;
    }

    /**
     * Ejemplar con su material y biblioteca resueltos.
     * Espera el join de Ejemplar + Material + Biblioteca.
     */
    static Ejemplar ejemplar(ResultSet rs) throws SQLException {
        Material m = new Material();
        m.setIdMaterial(rs.getInt("idMaterial"));
        m.setTitulo(rs.getString("tituloMaterial"));

        Biblioteca b = new Biblioteca();
        b.setIdBiblioteca(rs.getInt("idBiblioteca"));
        b.setNombre(rs.getString("nombreBiblioteca"));

        Ejemplar e = new Ejemplar();
        e.setIdEjemplar(rs.getInt("idEjemplar"));
        e.setMaterial(m);
        e.setBiblioteca(b);
        e.setCodigoEjemplar(rs.getString("codigoEjemplar"));
        e.setNumeroAdquisicion(rs.getString("numeroAdquisicion"));
        e.setFechaIngreso(fecha(rs, "fechaIngreso"));
        e.setEstado(rs.getString("estado"));
        return e;
    }

    static Multa multa(ResultSet rs) throws SQLException {
        Multa m = new Multa();
        m.setIdMulta(rs.getInt("idMulta"));
        m.setFechaGeneracion(fechaHora(rs, "fechaGeneracion"));
        m.setDiasRetraso(rs.getInt("diasRetraso"));
        m.setTarifaDiaria(decimal(rs, "tarifaDiaria"));
        m.setMonto(decimal(rs, "monto"));
        m.setMotivo(rs.getString("motivo"));
        m.setEstado(rs.getString("estado"));
        return m;
    }

    /** Mapea una fila de VW_SolicitudPrestamo. */
    static SolicitudPrestamoDetalle solicitudPrestamo(ResultSet rs) throws SQLException {
        SolicitudPrestamoDetalle s = new SolicitudPrestamoDetalle();
        s.setIdSolicitudPrestamo(rs.getInt("idSolicitudPrestamo"));
        s.setIdUsuario(rs.getInt("idUsuario"));
        s.setUsuarioNombre(rs.getString("usuarioNombre"));
        s.setNumeroControl(rs.getString("numeroControl"));
        s.setUsuarioRol(rs.getString("usuarioRol"));
        s.setMaxMateriales(rs.getInt("maxMateriales"));
        s.setIdMaterial(rs.getInt("idMaterial"));
        s.setTitulo(rs.getString("titulo"));
        s.setIsbn(rs.getString("isbn"));
        s.setAutores(rs.getString("autores"));
        s.setTipoMaterial(rs.getString("tipoMaterial"));
        s.setEjemplaresDisponibles(rs.getInt("ejemplaresDisponibles"));
        s.setFechaSolicitud(fechaHora(rs, "fechaSolicitud"));
        s.setEstado(rs.getString("estado"));
        s.setAdministradorNombre(rs.getString("administradorNombre"));
        s.setFechaRespuesta(fechaHora(rs, "fechaRespuesta"));
        s.setObservaciones(rs.getString("observaciones"));
        s.setIdPrestamo(enteroNulo(rs, "idPrestamo"));
        s.setMaterialesEnPoder(rs.getInt("materialesEnPoder"));
        s.setAdeudo(decimal(rs, "adeudo"));
        return s;
    }

    static SolicitudAdquisicion solicitud(ResultSet rs) throws SQLException {
        SolicitudAdquisicion s = new SolicitudAdquisicion();
        s.setIdSolicitud(rs.getInt("idSolicitud"));
        s.setTitulo(rs.getString("titulo"));
        s.setAutor(rs.getString("autor"));
        s.setEditorial(rs.getString("editorial"));
        s.setIsbn(rs.getString("isbn"));
        s.setDescripcion(rs.getString("descripcion"));
        s.setFechaSolicitud(fechaHora(rs, "fechaSolicitud"));
        s.setEstado(rs.getString("estado"));
        s.setRespuesta(rs.getString("respuesta"));
        s.setFechaRespuesta(fechaHora(rs, "fechaRespuesta"));
        return s;
    }
}
