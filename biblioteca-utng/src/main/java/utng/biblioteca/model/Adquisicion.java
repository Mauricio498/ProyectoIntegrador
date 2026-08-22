package utng.biblioteca.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Adquisicion {
    private int idAdquisicion;
    private LocalDate fechaAdquisicion;
    private String tipoAdquisicion;
    private String proveedor;
    private Usuario responsable;
    private String observaciones;
    private List<DetalleAdquisicion> detalles;

    // Constructor vacío
    public Adquisicion() {
        this.detalles = new ArrayList<>();
    }

    // Constructor para crear Adquisicion nueva
    public Adquisicion(LocalDate fechaAdquisicion, String tipoAdquisicion, 
                      String proveedor, Usuario responsable, String observaciones) {
        this.fechaAdquisicion = fechaAdquisicion;
        this.tipoAdquisicion = tipoAdquisicion;
        this.proveedor = proveedor;
        this.responsable = responsable;
        this.observaciones = observaciones;
        this.detalles = new ArrayList<>();
    }

    // Constructor con ID
    public Adquisicion(int idAdquisicion, LocalDate fechaAdquisicion, String tipoAdquisicion,
                      String proveedor, Usuario responsable, String observaciones) {
        this.idAdquisicion = idAdquisicion;
        this.fechaAdquisicion = fechaAdquisicion;
        this.tipoAdquisicion = tipoAdquisicion;
        this.proveedor = proveedor;
        this.responsable = responsable;
        this.observaciones = observaciones;
        this.detalles = new ArrayList<>();
    }

    // Getters y Setters
    public int getIdAdquisicion() {
        return idAdquisicion;
    }

    public void setIdAdquisicion(int idAdquisicion) {
        this.idAdquisicion = idAdquisicion;
    }

    public LocalDate getFechaAdquisicion() {
        return fechaAdquisicion;
    }

    public void setFechaAdquisicion(LocalDate fechaAdquisicion) {
        this.fechaAdquisicion = fechaAdquisicion;
    }

    public String getTipoAdquisicion() {
        return tipoAdquisicion;
    }

    public void setTipoAdquisicion(String tipoAdquisicion) {
        this.tipoAdquisicion = tipoAdquisicion;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public Usuario getResponsable() {
        return responsable;
    }

    public void setResponsable(Usuario responsable) {
        this.responsable = responsable;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<DetalleAdquisicion> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleAdquisicion> detalles) {
        this.detalles = detalles;
    }

    public void agregarDetalle(DetalleAdquisicion detalle) {
        this.detalles.add(detalle);
    }
}
