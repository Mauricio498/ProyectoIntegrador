package com.utng.biblioteca.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

/**
 * Representa el préstamo de un {@link Libro} a un {@link Usuario} entre
 * dos fechas, con un estado que evoluciona con el tiempo.
 */
public class Prestamo {

    private final int id;
    private final Libro libro;
    private final Usuario usuario;
    private final LocalDate fechaPrestamo;
    private final LocalDate fechaDevolucion;
    private final ObjectProperty<EstadoPrestamo> estado;
    private LocalDate fechaDevueltoReal;

    public Prestamo(int id, Libro libro, Usuario usuario, LocalDate fechaPrestamo,
                     LocalDate fechaDevolucion, EstadoPrestamo estado) {
        this.id = id;
        this.libro = libro;
        this.usuario = usuario;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = new SimpleObjectProperty<>(estado);
    }

    public int getId() {
        return id;
    }

    public Libro getLibro() {
        return libro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public EstadoPrestamo getEstado() {
        return estado.get();
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado.set(estado);
    }

    public ObjectProperty<EstadoPrestamo> estadoProperty() {
        return estado;
    }

    public LocalDate getFechaDevueltoReal() {
        return fechaDevueltoReal;
    }

    public void setFechaDevueltoReal(LocalDate fechaDevueltoReal) {
        this.fechaDevueltoReal = fechaDevueltoReal;
    }

    /** Calcula si el préstamo está activo (no devuelto). */
    public boolean estaActivo() {
        return estado.get() != EstadoPrestamo.DEVUELTO;
    }
}
