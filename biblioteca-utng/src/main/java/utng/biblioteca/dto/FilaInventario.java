package utng.biblioteca.dto;

/**
 * Renglón del inventario del acervo: un material con sus ejemplares contados
 * por estado y cuántas veces se ha prestado.
 *
 * El campo {@code observacion} viene calculado desde SQL y señala qué revisar:
 * "Sin ejemplares", "Ninguno utilizable" o "Nunca prestado".
 */
public class FilaInventario {

    private int idMaterial;
    private String titulo;
    private String isbn;
    private String clasificacion;
    private String tipo;
    private String editorial;
    private String autores;
    private Integer anioPublicacion;
    private String estadoMaterial;

    private int ejemplares;
    private int disponibles;
    private int prestados;
    private int noUtilizables;
    private int vecesPrestado;

    private String observacion;

    public int getIdMaterial() { return idMaterial; }
    public void setIdMaterial(int v) { this.idMaterial = v; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { this.titulo = v; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String v) { this.isbn = v; }

    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String v) { this.clasificacion = v; }

    public String getTipo() { return tipo; }
    public void setTipo(String v) { this.tipo = v; }

    public String getEditorial() { return editorial; }
    public void setEditorial(String v) { this.editorial = v; }

    public String getAutores() { return autores; }
    public void setAutores(String v) { this.autores = v; }

    public Integer getAnioPublicacion() { return anioPublicacion; }
    public void setAnioPublicacion(Integer v) { this.anioPublicacion = v; }

    public String getEstadoMaterial() { return estadoMaterial; }
    public void setEstadoMaterial(String v) { this.estadoMaterial = v; }

    public int getEjemplares() { return ejemplares; }
    public void setEjemplares(int v) { this.ejemplares = v; }

    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int v) { this.disponibles = v; }

    public int getPrestados() { return prestados; }
    public void setPrestados(int v) { this.prestados = v; }

    public int getNoUtilizables() { return noUtilizables; }
    public void setNoUtilizables(int v) { this.noUtilizables = v; }

    public int getVecesPrestado() { return vecesPrestado; }
    public void setVecesPrestado(int v) { this.vecesPrestado = v; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String v) { this.observacion = v; }

    /** "2 / 3" — ejemplares libres sobre el total. */
    public String getDisponibilidad() {
        return disponibles + " / " + ejemplares;
    }

    public boolean tieneObservacion() {
        return observacion != null && !observacion.isBlank();
    }

    public String getAnioTexto() {
        return anioPublicacion == null || anioPublicacion == 0 ? "" : String.valueOf(anioPublicacion);
    }
}
