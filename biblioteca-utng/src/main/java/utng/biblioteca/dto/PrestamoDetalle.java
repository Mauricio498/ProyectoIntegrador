package utng.biblioteca.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fila de la vista VW_PrestamoDetalle: un ejemplar prestado con los datos del
 * usuario, del material y los dias de retraso ya calculados por la base.
 */
public class PrestamoDetalle {

    private int idDetallePrestamo;
    private int idPrestamo;
    private int idUsuario;
    private String usuarioNombre;
    private String numeroControl;
    private Integer idAdministrador;
    private LocalDateTime fechaPrestamo;
    private LocalDate fechaLimite;
    private String estadoPrestamo;
    private int idEjemplar;
    private String codigoEjemplar;
    private String estadoEjemplar;
    private int idMaterial;
    private String titulo;
    private String isbn;
    private int idBiblioteca;
    private String biblioteca;
    private LocalDate fechaDevolucion;
    private String estadoDetalle;
    private int diasRetraso;

    public int getIdDetallePrestamo() { return idDetallePrestamo; }
    public void setIdDetallePrestamo(int v) { this.idDetallePrestamo = v; }

    public int getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(int v) { this.idPrestamo = v; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int v) { this.idUsuario = v; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String v) { this.usuarioNombre = v; }

    public String getNumeroControl() { return numeroControl; }
    public void setNumeroControl(String v) { this.numeroControl = v; }

    public Integer getIdAdministrador() { return idAdministrador; }
    public void setIdAdministrador(Integer v) { this.idAdministrador = v; }

    public LocalDateTime getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDateTime v) { this.fechaPrestamo = v; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate v) { this.fechaLimite = v; }

    public String getEstadoPrestamo() { return estadoPrestamo; }
    public void setEstadoPrestamo(String v) { this.estadoPrestamo = v; }

    public int getIdEjemplar() { return idEjemplar; }
    public void setIdEjemplar(int v) { this.idEjemplar = v; }

    public String getCodigoEjemplar() { return codigoEjemplar; }
    public void setCodigoEjemplar(String v) { this.codigoEjemplar = v; }

    public String getEstadoEjemplar() { return estadoEjemplar; }
    public void setEstadoEjemplar(String v) { this.estadoEjemplar = v; }

    public int getIdMaterial() { return idMaterial; }
    public void setIdMaterial(int v) { this.idMaterial = v; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { this.titulo = v; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String v) { this.isbn = v; }

    public int getIdBiblioteca() { return idBiblioteca; }
    public void setIdBiblioteca(int v) { this.idBiblioteca = v; }

    public String getBiblioteca() { return biblioteca; }
    public void setBiblioteca(String v) { this.biblioteca = v; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate v) { this.fechaDevolucion = v; }

    public String getEstadoDetalle() { return estadoDetalle; }
    public void setEstadoDetalle(String v) { this.estadoDetalle = v; }

    public int getDiasRetraso() { return diasRetraso; }
    public void setDiasRetraso(int v) { this.diasRetraso = v; }

    public boolean estaPendiente() {
        return "Prestado".equalsIgnoreCase(estadoDetalle);
    }

    /**
     * Estado legible que se muestra en la interfaz del usuario final.
     * Combina el estado del detalle con los dias de retraso y la cercania
     * a la fecha limite.
     */
    public String getEtiquetaEstado() {
        if ("Devuelto".equalsIgnoreCase(estadoDetalle)) {
            return diasRetraso > 0 ? "Devuelto con retraso" : "Devuelto";
        }
        if ("Perdido".equalsIgnoreCase(estadoDetalle)) {
            return "Extraviado";
        }
        if (diasRetraso > 0) {
            return "Retrasado";
        }
        if (fechaLimite != null && !LocalDate.now().plusDays(3).isBefore(fechaLimite)) {
            return "Por vencer";
        }
        return "Al dia";
    }

    /** Clase CSS del badge, coherente con usuario.css. */
    public String getClaseBadge() {
        return switch (getEtiquetaEstado()) {
            case "Retrasado", "Extraviado", "Devuelto con retraso" -> "badge-retrasado";
            case "Por vencer" -> "badge-por-vencer";
            case "Devuelto" -> "badge-devuelto";
            default -> "badge-al-dia";
        };
    }

    /** Dias que faltan para la fecha limite (negativo si ya paso). */
    public long getDiasRestantes() {
        if (fechaLimite == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
    }
}
