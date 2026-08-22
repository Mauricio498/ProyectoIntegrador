package utng.biblioteca.dto;

import java.time.LocalDate;

public class RegistroImportacion {

    private int filaExcel;
    private int idBiblioteca;
    private String numeroAdquisicion;
    private String codigoEjemplar;
    private String titulo;
    private String autor;
    private String editorial;
    private String isbn;
    private String clasificacion;
    private Integer anioPublicacion;
    private LocalDate fechaIngreso;
    private String tipoDetectado;

    public int getFilaExcel() {
        return filaExcel;
    }

    public void setFilaExcel(int filaExcel) {
        this.filaExcel = filaExcel;
    }

    public int getIdBiblioteca() {
        return idBiblioteca;
    }

    public void setIdBiblioteca(int idBiblioteca) {
        this.idBiblioteca = idBiblioteca;
    }

    public String getNumeroAdquisicion() {
        return numeroAdquisicion;
    }

    public void setNumeroAdquisicion(String numeroAdquisicion) {
        this.numeroAdquisicion = numeroAdquisicion;
    }

    public String getCodigoEjemplar() {
        return codigoEjemplar;
    }

    public void setCodigoEjemplar(String codigoEjemplar) {
        this.codigoEjemplar = codigoEjemplar;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public Integer getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(Integer anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getTipoDetectado() {
        return tipoDetectado;
    }

    public void setTipoDetectado(String tipoDetectado) {
        this.tipoDetectado = tipoDetectado;
    }

    @Override
    public String toString() {
        return filaExcel + " - "
                + titulo + " - "
                + tipoDetectado;
    }
}