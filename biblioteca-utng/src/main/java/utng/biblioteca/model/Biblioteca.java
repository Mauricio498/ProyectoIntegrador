package utng.biblioteca.model;

public class Biblioteca {
    private int idBiblioteca;
    private String nombre;
    private String ubicacion;
    private boolean estado;

    // Constructor vacío
    public Biblioteca() {
    }

    // Constructor para crear una Biblioteca nueva
    public Biblioteca(String nombre, String ubicacion) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.estado = true;
    }

    // Constructor con ID
    public Biblioteca(int idBiblioteca, String nombre, String ubicacion, boolean estado) {
        this.idBiblioteca = idBiblioteca;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdBiblioteca() {
        return idBiblioteca;
    }

    public void setIdBiblioteca(int idBiblioteca) {
        this.idBiblioteca = idBiblioteca;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    /** Se muestra asi en los ComboBox de la interfaz. */

    @Override
    public String toString() {
        return nombre;
    }
}
