package utng.biblioteca.model;

public class TipoMaterial {
    private int idTipoMaterial;
    private String nombre;
    private String descripcion;
    private boolean esPrestable;
    private boolean estado;

    // Constructor vacío
    public TipoMaterial() {
    }

    // Constructor para crear TipoMaterial nuevo
    public TipoMaterial(String nombre, String descripcion, boolean esPrestable) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.esPrestable = esPrestable;
        this.estado = true;
    }

    // Constructor con ID
    public TipoMaterial(int idTipoMaterial, String nombre, String descripcion, 
                        boolean esPrestable, boolean estado) {
        this.idTipoMaterial = idTipoMaterial;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.esPrestable = esPrestable;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdTipoMaterial() {
        return idTipoMaterial;
    }

    public void setIdTipoMaterial(int idTipoMaterial) {
        this.idTipoMaterial = idTipoMaterial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEsPrestable() {
        return esPrestable;
    }

    public void setEsPrestable(boolean esPrestable) {
        this.esPrestable = esPrestable;
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
