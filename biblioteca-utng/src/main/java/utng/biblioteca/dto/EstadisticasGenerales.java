package utng.biblioteca.dto;

import java.math.BigDecimal;

/** Fila unica de VW_EstadisticasGenerales: alimenta las tarjetas del dashboard. */
public class EstadisticasGenerales {

    private int totalMateriales;
    private int totalEjemplares;
    private int ejemplaresDisponibles;
    private int ejemplaresPrestados;
    private int usuariosActivos;
    private int prestamosActivos;
    private int prestamosRetrasados;
    private int solicitudesPendientes;
    private int solicitudesPrestamo;
    private BigDecimal multasPendientes = BigDecimal.ZERO;

    public int getTotalMateriales() { return totalMateriales; }
    public void setTotalMateriales(int v) { this.totalMateriales = v; }

    public int getTotalEjemplares() { return totalEjemplares; }
    public void setTotalEjemplares(int v) { this.totalEjemplares = v; }

    public int getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public void setEjemplaresDisponibles(int v) { this.ejemplaresDisponibles = v; }

    public int getEjemplaresPrestados() { return ejemplaresPrestados; }
    public void setEjemplaresPrestados(int v) { this.ejemplaresPrestados = v; }

    public int getUsuariosActivos() { return usuariosActivos; }
    public void setUsuariosActivos(int v) { this.usuariosActivos = v; }

    public int getPrestamosActivos() { return prestamosActivos; }
    public void setPrestamosActivos(int v) { this.prestamosActivos = v; }

    public int getPrestamosRetrasados() { return prestamosRetrasados; }
    public void setPrestamosRetrasados(int v) { this.prestamosRetrasados = v; }

    public int getSolicitudesPendientes() { return solicitudesPendientes; }
    public void setSolicitudesPendientes(int v) { this.solicitudesPendientes = v; }

    /** Solicitudes de prestamo esperando respuesta del personal de biblioteca. */
    public int getSolicitudesPrestamo() { return solicitudesPrestamo; }
    public void setSolicitudesPrestamo(int v) { this.solicitudesPrestamo = v; }

    public BigDecimal getMultasPendientes() { return multasPendientes; }
    public void setMultasPendientes(BigDecimal v) { this.multasPendientes = v; }
}
