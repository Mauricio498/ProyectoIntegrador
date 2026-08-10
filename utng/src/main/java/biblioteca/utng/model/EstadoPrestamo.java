package biblioteca.utng.model;

/**
 * Estados posibles de un préstamo. Cada estado tiene una etiqueta legible
 * y una clase CSS asociada para pintar el badge correspondiente.
 */
public enum EstadoPrestamo {
    AL_DIA("Al día", "badge-al-dia"),
    POR_VENCER("Por vencer", "badge-por-vencer"),
    DEVUELTO("Devuelto", "badge-devuelto"),
    RETRASADO("Retrasado", "badge-retrasado");

    private final String etiqueta;
    private final String cssClass;

    EstadoPrestamo(String etiqueta, String cssClass) {
        this.etiqueta = etiqueta;
        this.cssClass = cssClass;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getCssClass() {
        return cssClass;
    }
}
