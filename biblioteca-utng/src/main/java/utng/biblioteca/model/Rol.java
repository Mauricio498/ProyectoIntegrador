package utng.biblioteca.model;

public class Rol {
    private int idRol;
    private String nombre;
    private String descripcion;
    private int maxMateriales;
    private int diasPrestamo;

    /** Cómo se cuenta el plazo: HABILES, NATURALES o MESES. */
    private String unidadPlazo = "HABILES";
    private boolean estado;

    // Constructor vacío
    public Rol() {
    }

    // Constructor para crear un Rol nuevo
    public Rol(String nombre, String descripcion, int maxMateriales, int diasPrestamo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.maxMateriales = maxMateriales;
        this.diasPrestamo = diasPrestamo;
        this.estado = true;
    }

    // Constructor con ID
    public Rol(int idRol, String nombre, String descripcion, int maxMateriales, 
               int diasPrestamo, boolean estado) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.maxMateriales = maxMateriales;
        this.diasPrestamo = diasPrestamo;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
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

    public int getMaxMateriales() {
        return maxMateriales;
    }

    public void setMaxMateriales(int maxMateriales) {
        this.maxMateriales = maxMateriales;
    }

    public int getDiasPrestamo() {
        return diasPrestamo;
    }

    public void setDiasPrestamo(int diasPrestamo) {
        this.diasPrestamo = diasPrestamo;
    }

    public String getUnidadPlazo() {
        return unidadPlazo;
    }

    public void setUnidadPlazo(String unidadPlazo) {
        this.unidadPlazo = unidadPlazo;
    }

    /**
     * Plazo en texto, para mostrarlo sin que la interfaz tenga que saber
     * cómo se cuenta. Ejemplos: "5 días hábiles", "1 mes".
     */
    public String getPlazoTexto() {
        if (diasPrestamo <= 0) {
            return "Sin préstamo";
        }
        if ("MESES".equalsIgnoreCase(unidadPlazo)) {
            return diasPrestamo == 1 ? "1 mes" : diasPrestamo + " meses";
        }
        if ("NATURALES".equalsIgnoreCase(unidadPlazo)) {
            return diasPrestamo + (diasPrestamo == 1 ? " día" : " días");
        }
        return diasPrestamo + (diasPrestamo == 1 ? " día hábil" : " días hábiles");
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
