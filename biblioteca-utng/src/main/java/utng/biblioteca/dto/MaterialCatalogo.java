package utng.biblioteca.dto;

/**
 * Fila de la vista VW_CatalogoMaterial: un material con sus autores ya
 * concatenados y el conteo de ejemplares. Se usa en las tablas de gestion y
 * en las tarjetas del buscador, para no armar el mismo join en cada pantalla.
 */
public class MaterialCatalogo {

    private int idMaterial;
    private String titulo;
    private String isbn;
    private Integer anioPublicacion;
    private String clasificacion;
    private String estadoMaterial;
    private int idTipoMaterial;
    private String tipoMaterial;
    private boolean esPrestable;
    private Integer idEditorial;
    private String editorial;
    private String autores;
    private int totalEjemplares;
    private int ejemplaresDisponibles;

    public int getIdMaterial() { return idMaterial; }
    public void setIdMaterial(int v) { this.idMaterial = v; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { this.titulo = v; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String v) { this.isbn = v; }

    public Integer getAnioPublicacion() { return anioPublicacion; }
    public void setAnioPublicacion(Integer v) { this.anioPublicacion = v; }

    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String v) { this.clasificacion = v; }

    public String getEstadoMaterial() { return estadoMaterial; }
    public void setEstadoMaterial(String v) { this.estadoMaterial = v; }

    public int getIdTipoMaterial() { return idTipoMaterial; }
    public void setIdTipoMaterial(int v) { this.idTipoMaterial = v; }

    public String getTipoMaterial() { return tipoMaterial; }
    public void setTipoMaterial(String v) { this.tipoMaterial = v; }

    public boolean isEsPrestable() { return esPrestable; }
    public void setEsPrestable(boolean v) { this.esPrestable = v; }

    public Integer getIdEditorial() { return idEditorial; }
    public void setIdEditorial(Integer v) { this.idEditorial = v; }

    public String getEditorial() { return editorial; }
    public void setEditorial(String v) { this.editorial = v; }

    public String getAutores() { return autores; }
    public void setAutores(String v) { this.autores = v; }

    public int getTotalEjemplares() { return totalEjemplares; }
    public void setTotalEjemplares(int v) { this.totalEjemplares = v; }

    public int getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public void setEjemplaresDisponibles(int v) { this.ejemplaresDisponibles = v; }

    /** Hay al menos un ejemplar libre y el tipo permite prestamo. */
    public boolean isDisponible() {
        return esPrestable && ejemplaresDisponibles > 0;
    }

    public String getEtiquetaDisponibilidad() {
        if (!esPrestable) {
            return "Solo consulta";
        }
        return ejemplaresDisponibles > 0 ? "Disponible" : "Prestado";
    }

    /** Clase CSS del badge, coherente con usuario.css. */
    public String getClaseBadge() {
        if (!esPrestable) {
            return "badge-consulta";
        }
        return ejemplaresDisponibles > 0 ? "badge-disponible" : "badge-prestado";
    }

    /** Iniciales del titulo para el placeholder de portada. */
    public String getIniciales() {
        if (titulo == null || titulo.isBlank()) {
            return "?";
        }
        StringBuilder sb = new StringBuilder();
        for (String palabra : titulo.trim().split(" ")) {
            if (!palabra.isBlank() && sb.length() < 2) {
                sb.append(Character.toUpperCase(palabra.charAt(0)));
            }
        }
        return sb.toString();
    }

    /**
     * Color estable derivado del id, para las portadas de las tarjetas.
     * Solo tonos de la paleta institucional, para que el catálogo no rompa
     * la guía de estilo aunque cada material se vea distinto.
     */
    public String getColorPortada() {
        String[] paleta = {"#2A3087", "#18A680", "#262628", "#1F2568", "#969193", "#12805F"};
        return paleta[Math.abs(idMaterial) % paleta.length];
    }
}
