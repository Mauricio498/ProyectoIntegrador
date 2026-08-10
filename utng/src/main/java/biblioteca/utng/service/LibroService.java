package com.utng.biblioteca.service;

import com.utng.biblioteca.model.Categoria;
import com.utng.biblioteca.model.Libro;
import com.utng.biblioteca.repository.LibroRepository;

import java.util.List;
import java.util.Locale;

/**
 * Lógica de negocio relacionada con el catálogo de libros: búsqueda,
 * filtrado y consulta de destacados.
 */
public class LibroService {

    /** Filtro de disponibilidad usado en la pantalla Buscador. */
    public enum FiltroDisponibilidad {
        TODOS("Todos"),
        DISPONIBLE("Disponible"),
        PRESTADO("Prestado");

        private final String etiqueta;

        FiltroDisponibilidad(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private final LibroRepository libroRepository;

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    public List<Libro> obtenerTodos() {
        return libroRepository.findAll();
    }

    public List<Libro> obtenerDestacados() {
        return libroRepository.findDestacados(4);
    }

    /**
     * Busca y filtra libros por texto libre (título, autor o ISBN),
     * categoría y disponibilidad.
     */
    public List<Libro> buscar(String textoBusqueda, Categoria categoria, FiltroDisponibilidad disponibilidad) {
        String texto = textoBusqueda == null ? "" : textoBusqueda.trim().toLowerCase(Locale.ROOT);

        return libroRepository.findAll().stream()
                .filter(libro -> texto.isEmpty()
                        || libro.getTitulo().toLowerCase(Locale.ROOT).contains(texto)
                        || libro.getAutor().toLowerCase(Locale.ROOT).contains(texto)
                        || libro.getIsbn().toLowerCase(Locale.ROOT).contains(texto))
                .filter(libro -> categoria == null || libro.getCategoria() == categoria)
                .filter(libro -> switch (disponibilidad == null ? FiltroDisponibilidad.TODOS : disponibilidad) {
                    case TODOS -> true;
                    case DISPONIBLE -> libro.isDisponible();
                    case PRESTADO -> !libro.isDisponible();
                })
                .toList();
    }
}
