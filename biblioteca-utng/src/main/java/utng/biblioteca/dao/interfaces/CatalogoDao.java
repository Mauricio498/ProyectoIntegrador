package utng.biblioteca.dao.interfaces;

import utng.biblioteca.dto.RegistroImportacion;
import utng.biblioteca.dto.ResultadoImportacion;
import utng.biblioteca.dto.MaterialCatalogo;
import utng.biblioteca.model.Autor;
import utng.biblioteca.model.Biblioteca;
import utng.biblioteca.model.Editorial;
import utng.biblioteca.model.Ejemplar;
import utng.biblioteca.model.Material;
import utng.biblioteca.model.TipoMaterial;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Acervo: materiales, ejemplares y catalogos base (tipo, editorial, autor). */
public interface CatalogoDao {
    ResultadoImportacion importarExcel( List<RegistroImportacion> registros ) throws SQLException;
    /**
     * Consulta VW_CatalogoMaterial aplicando filtros opcionales.
     *
     * @param texto           titulo, ISBN, autor o clasificacion (puede ser null)
     * @param idTipoMaterial  null = todos
     * @param soloDisponibles true = solo materiales con ejemplares libres
     */
    List<MaterialCatalogo> buscar(String texto, Integer idTipoMaterial, boolean soloDisponibles)
            throws SQLException;

    /**
     * Igual que {@link #buscar}, pero incluye los materiales dados de baja.
     * Solo lo usa el módulo de gestión, para poder reactivarlos.
     */
    List<MaterialCatalogo> buscarIncluyendoBajas(String texto, Integer idTipoMaterial,
                                                 boolean soloDisponibles) throws SQLException;

    Optional<MaterialCatalogo> buscarPorId(int idMaterial) throws SQLException;

    /** Materiales mas prestados, para la seccion de destacados. */
    List<MaterialCatalogo> destacados(int limite) throws SQLException;

    int insertarMaterial(Material material, List<Integer> idsAutores) throws SQLException;

    void actualizarMaterial(Material material, List<Integer> idsAutores) throws SQLException;

    void cambiarEstadoMaterial(int idMaterial, String estado) throws SQLException;

    List<Ejemplar> listarEjemplares(int idMaterial) throws SQLException;

    /** Ejemplares en estado Disponible de un material, para armar un prestamo. */
    List<Ejemplar> listarEjemplaresDisponibles(int idMaterial) throws SQLException;

    int insertarEjemplar(Ejemplar ejemplar) throws SQLException;

    void cambiarEstadoEjemplar(int idEjemplar, String estado) throws SQLException;

    List<TipoMaterial> listarTiposMaterial() throws SQLException;

    List<Editorial> listarEditoriales() throws SQLException;

    List<Autor> listarAutores() throws SQLException;

    List<Autor> autoresDeMaterial(int idMaterial) throws SQLException;

    List<Biblioteca> listarBibliotecas() throws SQLException;

    int insertarAutor(String nombre) throws SQLException;

    int insertarEditorial(String nombre) throws SQLException;

    /** Bitacora de busquedas. idUsuario null = invitado. */
    void registrarBusqueda(Integer idUsuario, String termino, String filtros, int resultados)
            throws SQLException;
}
