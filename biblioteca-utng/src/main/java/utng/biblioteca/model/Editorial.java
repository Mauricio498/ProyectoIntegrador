package utng.biblioteca.model;

public class Editorial {
    private int idEditorial;
    private String nombre;
    private boolean estado;

    // Constructor vacío
    public Editorial() {
    }

    // Constructor para crear Editorial nueva
    public Editorial(String nombre) {
        this.nombre = nombre;
        this.estado = true;
    }

    // Constructor con ID
    public Editorial(int idEditorial, String nombre, boolean estado) {
        this.idEditorial = idEditorial;
        this.nombre = nombre;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdEditorial() {
        return idEditorial;
    }

    public void setIdEditorial(int idEditorial) {
        this.idEditorial = idEditorial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
