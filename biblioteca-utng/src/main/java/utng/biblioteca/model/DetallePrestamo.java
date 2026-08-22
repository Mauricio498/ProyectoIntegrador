package utng.biblioteca.model;

import java.time.LocalDate;

public class DetallePrestamo {
    private int idDetallePrestamo;
    private Prestamo prestamo;
    private Ejemplar ejemplar;
    private LocalDate fechaDevolucion;
    private String estado;

    // Constructor vacío
    public DetallePrestamo() {
    }

    // Constructor para crear DetallePrestamo nuevo
    public DetallePrestamo(Prestamo prestamo, Ejemplar ejemplar) {
        this.prestamo = prestamo;
        this.ejemplar = ejemplar;
        this.estado = "Prestado";
    }

    // Constructor con ID
    public DetallePrestamo(int idDetallePrestamo, Prestamo prestamo, Ejemplar ejemplar,
                           LocalDate fechaDevolucion, String estado) {
        this.idDetallePrestamo = idDetallePrestamo;
        this.prestamo = prestamo;
        this.ejemplar = ejemplar;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdDetallePrestamo() {
        return idDetallePrestamo;
    }

    public void setIdDetallePrestamo(int idDetallePrestamo) {
        this.idDetallePrestamo = idDetallePrestamo;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    public Ejemplar getEjemplar() {
        return ejemplar;
    }

    public void setEjemplar(Ejemplar ejemplar) {
        this.ejemplar = ejemplar;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Métodos auxiliares
    public boolean esDevuelto() {
        return "Devuelto".equalsIgnoreCase(estado);
    }

    public boolean esPrestado() {
        return "Prestado".equalsIgnoreCase(estado);
    }

    public boolean esPerdido() {
        return "Perdido".equalsIgnoreCase(estado);
    }
}
