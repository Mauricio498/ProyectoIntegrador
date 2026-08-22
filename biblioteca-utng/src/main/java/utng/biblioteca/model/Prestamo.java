package utng.biblioteca.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Prestamo {
    private int idPrestamo;
    private Usuario usuario;
    private Usuario administrador;
    private LocalDateTime fechaPrestamo;
    private LocalDate fechaLimite;
    private String estado;
    private List<DetallePrestamo> detalles;

    // Constructor vacío
    public Prestamo() {
        this.detalles = new ArrayList<>();
    }

    // Constructor para crear Prestamo nuevo
    public Prestamo(Usuario usuario, Usuario administrador, LocalDate fechaLimite) {
        this.usuario = usuario;
        this.administrador = administrador;
        this.fechaPrestamo = LocalDateTime.now();
        this.fechaLimite = fechaLimite;
        this.estado = "Activo";
        this.detalles = new ArrayList<>();
    }

    // Constructor con ID
    public Prestamo(int idPrestamo, Usuario usuario, Usuario administrador,
                    LocalDateTime fechaPrestamo, LocalDate fechaLimite, String estado) {
        this.idPrestamo = idPrestamo;
        this.usuario = usuario;
        this.administrador = administrador;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaLimite = fechaLimite;
        this.estado = estado;
        this.detalles = new ArrayList<>();
    }

    // Getters y Setters
    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Usuario administrador) {
        this.administrador = administrador;
    }

    public LocalDateTime getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDateTime fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<DetallePrestamo> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePrestamo> detalles) {
        this.detalles = detalles;
    }

    public void agregarDetalle(DetallePrestamo detalle) {
        this.detalles.add(detalle);
    }

    // Métodos auxiliares
    public boolean esVencido() {
        return LocalDate.now().isAfter(fechaLimite) && "Activo".equalsIgnoreCase(estado);
    }

    public boolean esActivo() {
        return "Activo".equalsIgnoreCase(estado);
    }

    public int getDiasRestantes() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
    }
}
