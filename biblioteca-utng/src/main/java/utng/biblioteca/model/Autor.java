package utng.biblioteca.model;

public class Autor {
    private int idAutor;
    private String nombre;

    // Constructor vacío
    public Autor() {
    }

    // Constructor para crear Autor nuevo
    public Autor(String nombre) {
        this.nombre = nombre;
    }

    // Constructor con ID
    public Autor(int idAutor, String nombre) {
        this.idAutor = idAutor;
        this.nombre = nombre;
    }

    // Getters y Setters
    public int getIdAutor() {
        return idAutor;
    }

    public void setIdAutor(int idAutor) {
        this.idAutor = idAutor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Se muestra asi en los ComboBox y listas de la interfaz. */

    @Override
    public String toString() {
        return nombre;
    }
}
