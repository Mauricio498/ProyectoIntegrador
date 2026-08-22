package utng.biblioteca.service;

import utng.biblioteca.dto.RegistroImportacion;
import utng.biblioteca.dto.ResultadoImportacion;
import utng.biblioteca.dao.impl.CatalogoDaoImpl;
import utng.biblioteca.dao.impl.FavoritoDaoImpl;
import utng.biblioteca.dao.interfaces.CatalogoDao;
import utng.biblioteca.dao.interfaces.FavoritoDao;
import utng.biblioteca.dto.MaterialCatalogo;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.Autor;
import utng.biblioteca.model.Biblioteca;
import utng.biblioteca.model.Editorial;
import utng.biblioteca.model.Ejemplar;
import utng.biblioteca.model.Material;
import utng.biblioteca.model.TipoMaterial;
import utng.biblioteca.session.Sesion;

import java.util.List;
import java.util.Optional;

/**
 * Consulta y mantenimiento del acervo.
 *
 * Es el servicio que usan tanto el buscador del usuario final como el modulo
 * de gestion de materiales del administrador.
 */
public class CatalogoService extends ServicioBase {

    private final CatalogoDao catalogoDao;
    private final FavoritoDao favoritoDao;

    public ResultadoImportacion importarExcel(
        List<RegistroImportacion> registros) {
        if (!Sesion.esPersonalBiblioteca()) {
            return null;
        }

        try {

            return catalogoDao.importarExcel(registros);

        } catch (Exception e) {

            throw new RuntimeException(
                    "No se pudo importar el archivo Excel: "
                    + e.getMessage(),
                    e
            );
        }
    }

    public CatalogoService() {
        this(new CatalogoDaoImpl(), new FavoritoDaoImpl());
    }

    public CatalogoService(CatalogoDao catalogoDao, FavoritoDao favoritoDao) {
        this.catalogoDao = catalogoDao;
        this.favoritoDao = favoritoDao;
    }

    /**
     * Busqueda del catalogo. Registra el termino en la bitacora Busqueda
     * (con idUsuario nulo cuando consulta un invitado sin sesion).
     */
    public List<MaterialCatalogo> buscar(String texto, Integer idTipoMaterial, boolean soloDisponibles) {
        List<MaterialCatalogo> resultados =
                consultar("buscar en el cátalogo",
                        () -> catalogoDao.buscar(texto, idTipoMaterial, soloDisponibles));

        if (texto != null && !texto.isBlank()) {
            String filtros = "tipo=" + (idTipoMaterial == null ? "todos" : idTipoMaterial)
                           + "; soloDisponibles=" + soloDisponibles;
            try {
                catalogoDao.registrarBusqueda(Sesion.getIdUsuario(), texto.trim(), filtros, resultados.size());
            } catch (Exception e) {
                // La bitacora no debe impedir mostrar resultados.
                System.err.println("[CatalogoService] No se registró la búsqueda: " + e.getMessage());
            }
        }

        return resultados;
    }

    public List<MaterialCatalogo> listarTodo() {
        return consultar("listar el cátalogo", () -> catalogoDao.buscar(null, null, false));
    }

    /**
     * Búsqueda del módulo de gestión: incluye los materiales dados de baja
     * para que el personal pueda reactivarlos. No registra la búsqueda en la
     * bitácora, porque no es una consulta del catálogo público.
     */
    public List<MaterialCatalogo> buscarParaGestion(String texto, Integer idTipoMaterial,
                                                    boolean soloDisponibles) {
        if (!Sesion.esPersonalBiblioteca()) {
            return List.of();
        }
        return consultar("buscar en el acervo",
                () -> catalogoDao.buscarIncluyendoBajas(texto, idTipoMaterial, soloDisponibles));
    }

    public Optional<MaterialCatalogo> porId(int idMaterial) {
        return valor("obtener el material", () -> catalogoDao.buscarPorId(idMaterial), Optional::empty);
    }

    public List<MaterialCatalogo> destacados(int limite) {
        return consultar("obtener destacados", () -> catalogoDao.destacados(limite));
    }

    public List<TipoMaterial> tiposMaterial() {
        return consultar("listar tipos de material", catalogoDao::listarTiposMaterial);
    }

    public List<Editorial> editoriales() {
        return consultar("listar editoriales", catalogoDao::listarEditoriales);
    }

    public List<Autor> autores() {
        return consultar("listar autores", catalogoDao::listarAutores);
    }

    public List<Autor> autoresDe(int idMaterial) {
        return consultar("listar autores del material", () -> catalogoDao.autoresDeMaterial(idMaterial));
    }

    public List<Biblioteca> bibliotecas() {
        return consultar("listar bibliotecas", catalogoDao::listarBibliotecas);
    }

    public List<Ejemplar> ejemplaresDe(int idMaterial) {
        return consultar("listar ejemplares", () -> catalogoDao.listarEjemplares(idMaterial));
    }

    public List<Ejemplar> ejemplaresDisponiblesDe(int idMaterial) {
        return consultar("listar ejemplares disponibles",
                () -> catalogoDao.listarEjemplaresDisponibles(idMaterial));
    }

    /** Alta de material. Solo el personal de biblioteca puede ejecutarla. */
    public Resultado guardarMaterial(Material material, List<Integer> idsAutores) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Tu rol no permite modificar el acervo.");
        }
        if (material.getTitulo() == null || material.getTitulo().isBlank()) {
            return Resultado.error("El título es obligatorio.");
        }
        if (material.getTipoMaterial() == null || material.getTipoMaterial().getIdTipoMaterial() <= 0) {
            return Resultado.error("Selecciona el tipo de material.");
        }

        return operar("guardar el material", () -> {
            if (material.getIdMaterial() > 0) {
                catalogoDao.actualizarMaterial(material, idsAutores);
                return Resultado.ok("Material actualizado.", material.getIdMaterial());
            }
            int id = catalogoDao.insertarMaterial(material, idsAutores);
            return Resultado.ok("Material registrado.", id);
        });
    }

    public Resultado darDeBajaMaterial(int idMaterial) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Tu rol no permite modificar el acervo.");
        }
        return operar("dar de baja el material", () -> {
            catalogoDao.cambiarEstadoMaterial(idMaterial, "Baja");
            return Resultado.ok("Material dado de baja.");
        });
    }

    /**
     * Devuelve al catálogo un material dado de baja. Los ejemplares conservan
     * el estado que tenían, así que puede quedar activo pero sin copias libres.
     */
    public Resultado reactivarMaterial(int idMaterial) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Tu rol no permite modificar el acervo.");
        }
        return operar("reactivar el material", () -> {
            catalogoDao.cambiarEstadoMaterial(idMaterial, "Activo");
            return Resultado.ok("Material reactivado.");
        });
    }

    public Resultado guardarEjemplar(Ejemplar ejemplar) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Tu rol no permite modificar el acervo.");
        }
        if (ejemplar.getCodigoEjemplar() == null || ejemplar.getCodigoEjemplar().isBlank()) {
            return Resultado.error("El código del ejemplar es obligatorio.");
        }
        return operar("registrar el ejemplar", () -> {
            int id = catalogoDao.insertarEjemplar(ejemplar);
            return Resultado.ok("Ejemplar registrado.", id);
        });
    }

    public Resultado cambiarEstadoEjemplar(int idEjemplar, String estado) {
        if (!Sesion.esPersonalBiblioteca()) {
            return Resultado.error("Tu rol no permite modificar el acervo.");
        }
        return operar("cambiar el estado del ejemplar", () -> {
            catalogoDao.cambiarEstadoEjemplar(idEjemplar, estado);
            return Resultado.ok("Estado actualizado a " + estado + ".");
        });
    }

    public Resultado nuevoAutor(String nombre) {
        return operar("registrar el autor", () -> {
            int id = catalogoDao.insertarAutor(nombre.trim());
            return Resultado.ok("Autor registrado.", id);
        });
    }

    public Resultado nuevaEditorial(String nombre) {
        return operar("registrar la editorial", () -> {
            int id = catalogoDao.insertarEditorial(nombre.trim());
            return Resultado.ok("Editorial registrada.", id);
        });
    }

    // ---------------------------------------------------------------- favoritos

    public List<MaterialCatalogo> favoritos() {
        Integer id = Sesion.getIdUsuario();
        if (id == null) {
            return List.of();
        }
        return consultar("listar favoritos", () -> favoritoDao.listar(id));
    }

    public boolean esFavorito(int idMaterial) {
        Integer id = Sesion.getIdUsuario();
        if (id == null) {
            return false;
        }
        return valor("consultar favorito", () -> favoritoDao.esFavorito(id, idMaterial), () -> false);
    }

    /** Alterna el favorito y devuelve el estado resultante. */
    public Resultado alternarFavorito(int idMaterial) {
        Integer id = Sesion.getIdUsuario();
        if (id == null) {
            return Resultado.error("Inicia sesión para guardar favoritos.");
        }
        return operar("actualizar favoritos", () -> {
            if (favoritoDao.esFavorito(id, idMaterial)) {
                favoritoDao.quitar(id, idMaterial);
                return Resultado.ok("Quitado de favoritos.");
            }
            favoritoDao.agregar(id, idMaterial);
            return Resultado.ok("Agregado a favoritos.");
        });
    }
}
