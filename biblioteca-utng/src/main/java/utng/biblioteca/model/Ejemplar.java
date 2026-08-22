package utng.biblioteca.model;

import java.time.LocalDate;

/**
 * Ejemplar representa una copia física específica de un Material.
 * La distinción entre Material y Ejemplar es importante:
 * - Material: registro bibliográfico (ej: "Don Quijote de la Mancha")
 * - Ejemplar: copia física específica (ej: Código 156.CD.45.3, disponible en Biblioteca A)
 */
public class Ejemplar {
    private int idEjemplar;
    private Material material;
    private Biblioteca biblioteca;
    private String codigoEjemplar;
    private String numeroAdquisicion;
    private LocalDate fechaIngreso;
    private String estado;

    // Constructor vacío
    public Ejemplar() {
    }

    // Constructor para crear Ejemplar nuevo
    public Ejemplar(Material material, Biblioteca biblioteca, String codigoEjemplar,
                    String numeroAdquisicion, LocalDate fechaIngreso) {
        this.material = material;
        this.biblioteca = biblioteca;
        this.codigoEjemplar = codigoEjemplar;
        this.numeroAdquisicion = numeroAdquisicion;
        this.fechaIngreso = fechaIngreso;
        this.estado = "Disponible";
    }

    // Constructor con ID
    public Ejemplar(int idEjemplar, Material material, Biblioteca biblioteca, 
                    String codigoEjemplar, String numeroAdquisicion, LocalDate fechaIngreso,
                    String estado) {
        this.idEjemplar = idEjemplar;
        this.material = material;
        this.biblioteca = biblioteca;
        this.codigoEjemplar = codigoEjemplar;
        this.numeroAdquisicion = numeroAdquisicion;
        this.fechaIngreso = fechaIngreso;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(int idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Biblioteca getBiblioteca() {
        return biblioteca;
    }

    public void setBiblioteca(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
    }

    public String getCodigoEjemplar() {
        return codigoEjemplar;
    }

    public void setCodigoEjemplar(String codigoEjemplar) {
        this.codigoEjemplar = codigoEjemplar;
    }

    public String getNumeroAdquisicion() {
        return numeroAdquisicion;
    }

    public void setNumeroAdquisicion(String numeroAdquisicion) {
        this.numeroAdquisicion = numeroAdquisicion;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Métodos auxiliares
    public boolean esDisponible() {
        return "Disponible".equalsIgnoreCase(estado);
    }

    public boolean esPrestado() {
        return "Prestado".equalsIgnoreCase(estado);
    }
}
