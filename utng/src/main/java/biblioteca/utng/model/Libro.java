package biblioteca.utng.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Representa un libro dentro del catálogo de la biblioteca digital.
 */
public class Libro {

    private final int id;
    private final String titulo;
    private final String autor;
    private final String isbn;
    private final Categoria categoria;
    private final String descripcion;
    private final BooleanProperty disponible;
    private final String colorPortada;

    public Libro(int id, String titulo, String autor, String isbn, Categoria categoria,
                 String descripcion, boolean disponible, String colorPortada) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.disponible = new SimpleBooleanProperty(disponible);
        this.colorPortada = colorPortada;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isDisponible() {
        return disponible.get();
    }

    public void setDisponible(boolean valor) {
        disponible.set(valor);
    }

    public BooleanProperty disponibleProperty() {
        return disponible;
    }

    /** Color hexadecimal usado para generar el placeholder de portada. */
    public String getColorPortada() {
        return colorPortada;
    }

    /** Iniciales del título, usadas en el placeholder de portada. */
    public String getIniciales() {
        String[] palabras = titulo.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isBlank() && sb.length() < 2) {
                sb.append(Character.toUpperCase(palabra.charAt(0)));
            }
        }
        return sb.toString();
    }
}
