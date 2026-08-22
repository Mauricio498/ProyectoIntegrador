package utng.biblioteca.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fila de la vista VW_SolicitudPrestamo.
 *
 * Trae en un solo renglon lo que el administrador necesita para decidir:
 * quien pide, que material, cuantos ejemplares quedan libres, cuantos
 * materiales ya trae esa persona y si debe algo.
 */
public class SolicitudPrestamoDetalle {

    private int idSolicitudPrestamo;
    private int idUsuario;
    private String usuarioNombre;
    private String numeroControl;
    private String usuarioRol;
    private int maxMateriales;
    private int idMaterial;
    private String titulo;
    private String isbn;
    private String autores;
    private String tipoMaterial;
    private int ejemplaresDisponibles;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private String administradorNombre;
    private LocalDateTime fechaRespuesta;
    private String observaciones;
    private Integer idPrestamo;
    private int materialesEnPoder;
    private BigDecimal adeudo = BigDecimal.ZERO;

    public int getIdSolicitudPrestamo() { return idSolicitudPrestamo; }
    public void setIdSolicitudPrestamo(int v) { this.idSolicitudPrestamo = v; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int v) { this.idUsuario = v; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String v) { this.usuarioNombre = v; }

    public String getNumeroControl() { return numeroControl; }
    public void setNumeroControl(String v) { this.numeroControl = v; }

    public String getUsuarioRol() { return usuarioRol; }
    public void setUsuarioRol(String v) { this.usuarioRol = v; }

    public int getMaxMateriales() { return maxMateriales; }
    public void setMaxMateriales(int v) { this.maxMateriales = v; }

    public int getIdMaterial() { return idMaterial; }
    public void setIdMaterial(int v) { this.idMaterial = v; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { this.titulo = v; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String v) { this.isbn = v; }

    public String getAutores() { return autores; }
    public void setAutores(String v) { this.autores = v; }

    public String getTipoMaterial() { return tipoMaterial; }
    public void setTipoMaterial(String v) { this.tipoMaterial = v; }

    public int getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public void setEjemplaresDisponibles(int v) { this.ejemplaresDisponibles = v; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime v) { this.fechaSolicitud = v; }

    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }

    public String getAdministradorNombre() { return administradorNombre; }
    public void setAdministradorNombre(String v) { this.administradorNombre = v; }

    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime v) { this.fechaRespuesta = v; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { this.observaciones = v; }

    public Integer getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(Integer v) { this.idPrestamo = v; }

    public int getMaterialesEnPoder() { return materialesEnPoder; }
    public void setMaterialesEnPoder(int v) { this.materialesEnPoder = v; }

    public BigDecimal getAdeudo() { return adeudo; }
    public void setAdeudo(BigDecimal v) { this.adeudo = v; }

    public boolean estaPendiente() {
        return "Pendiente".equalsIgnoreCase(estado);
    }

    /** "3 / 5" -> lo que trae contra el limite de su rol. */
    public String getCupoTexto() {
        return materialesEnPoder + " / " + maxMateriales;
    }

    /**
     * Aviso corto para el administrador. Vacio cuando no hay nada que revisar
     * antes de aprobar.
     */
    public String getAdvertencia() {
        if (ejemplaresDisponibles == 0) {
            return "Sin ejemplares libres";
        }
        if (adeudo != null && adeudo.signum() > 0) {
            return "Tiene adeudo";
        }
        if (materialesEnPoder >= maxMateriales) {
            return "En su limite";
        }
        return "";
    }

    /** Clase CSS del badge de estado. */
    public String getClaseBadge() {
        return switch (estado == null ? "" : estado) {
            case "Aprobada"  -> "badge-disponible";
            case "Rechazada" -> "badge-retrasado";
            case "Cancelada" -> "badge-consulta";
            default          -> "badge-por-vencer";
        };
    }
}
