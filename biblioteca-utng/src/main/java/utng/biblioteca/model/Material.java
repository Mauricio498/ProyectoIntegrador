package utng.biblioteca.model;

import java.time.LocalDateTime;

public class Material {
    private int idMaterial;
    private String titulo;
    private String isbn;
    private int anioPublicacion;
    private String clasificacion;
    private Editorial editorial;
    private TipoMaterial tipoMaterial;
    private String estado;
    private LocalDateTime fechaRegistro;

    // Constructor vacío
    public Material() {
    }

    // Constructor para crear Material nuevo
    public Material(String titulo, String isbn, int anioPublicacion, 
                    String clasificacion, Editorial editorial, TipoMaterial tipoMaterial) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.anioPublicacion = anioPublicacion;
        this.clasificacion = clasificacion;
        this.editorial = editorial;
        this.tipoMaterial = tipoMaterial;
        this.estado = "Activo";
        this.fechaRegistro = LocalDateTime.now();
    }

    // Constructor con ID
    public Material(int idMaterial, String titulo, String isbn, int anioPublicacion,
                    String clasificacion, Editorial editorial, TipoMaterial tipoMaterial,
                    String estado, LocalDateTime fechaRegistro) {
        this.idMaterial = idMaterial;
        this.titulo = titulo;
        this.isbn = isbn;
        this.anioPublicacion = anioPublicacion;
        this.clasificacion = clasificacion;
        this.editorial = editorial;
        this.tipoMaterial = tipoMaterial;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
    public int getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(int idMaterial) {
        this.idMaterial = idMaterial;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(int anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public Editorial getEditorial() {
        return editorial;
    }

    public void setEditorial(Editorial editorial) {
        this.editorial = editorial;
    }

    public TipoMaterial getTipoMaterial() {
        return tipoMaterial;
    }

    public void setTipoMaterial(TipoMaterial tipoMaterial) {
        this.tipoMaterial = tipoMaterial;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // Método auxiliar
    public boolean esActivo() {
        return "Activo".equalsIgnoreCase(estado);
    }
}
