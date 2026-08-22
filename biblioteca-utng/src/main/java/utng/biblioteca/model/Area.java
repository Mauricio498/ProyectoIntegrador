package utng.biblioteca.model;

/**
 * Área académica a la que pertenece un usuario.
 *
 * La UTNG agrupa su oferta en tres áreas; las carreras y especialidades no se
 * registran porque para los reportes basta el área.
 *
 * Es opcional: el personal de biblioteca no pertenece a ninguna, y los
 * usuarios cargados antes de la migración 05_area.sql tampoco la tienen.
 */
public class Area {

    private int idArea;
    private String nombre;
    private String siglas;
    private boolean estado = true;

    public int getIdArea() { return idArea; }
    public void setIdArea(int v) { this.idArea = v; }

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }

    public String getSiglas() { return siglas; }
    public void setSiglas(String v) { this.siglas = v; }

    public boolean isEstado() { return estado; }
    public void setEstado(boolean v) { this.estado = v; }

    /** Se muestra así en los ComboBox de la interfaz. */
    @Override
    public String toString() {
        if (siglas == null || siglas.isBlank()) {
            return nombre;
        }
        return siglas + " - " + nombre;
    }
}
