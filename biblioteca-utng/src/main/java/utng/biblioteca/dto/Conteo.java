package utng.biblioteca.dto;

/**
 * Par etiqueta/cantidad para rankings y gráficas de barras o pastel:
 * término más buscado, material más prestado, préstamos por rol o por área.
 *
 * El campo {@code detalle} lleva información de apoyo que la tabla muestra
 * pero la gráfica ignora (autores del material, promedio de resultados...).
 */
public class Conteo {

    private String etiqueta;
    private int cantidad;
    private String detalle;

    public Conteo() {
    }

    public Conteo(String etiqueta, int cantidad, String detalle) {
        this.etiqueta = etiqueta;
        this.cantidad = cantidad;
        this.detalle = detalle;
    }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String v) { this.etiqueta = v; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int v) { this.cantidad = v; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String v) { this.detalle = v; }

    /** Etiqueta recortada, para que las gráficas no se deformen. */
    public String getEtiquetaCorta() {
        if (etiqueta == null) {
            return "";
        }
        return etiqueta.length() <= 24 ? etiqueta : etiqueta.substring(0, 22) + "…";
    }
}
