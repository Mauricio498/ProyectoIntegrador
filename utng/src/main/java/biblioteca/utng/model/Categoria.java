package com.utng.biblioteca.model;

/**
 * Categorías disponibles para clasificar los libros del catálogo.
 */
public enum Categoria {
    TECNOLOGIA("Tecnología"),
    LITERATURA("Literatura"),
    CIENCIA("Ciencia"),
    DESARROLLO_PERSONAL("Desarrollo personal"),
    DISENO("Diseño"),
    HISTORIA("Historia"),
    NOVELA("Novela");

    private final String etiqueta;

    Categoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
