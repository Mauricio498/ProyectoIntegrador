package biblioteca.utng.repository;

import biblioteca.utng.model.Categoria;
import biblioteca.utng.model.Libro;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a los datos de libros. En esta versión de demostración los datos
 * viven en memoria, pero la interfaz permite sustituir fácilmente la
 * fuente por una base de datos real más adelante.
 */
public class LibroRepository {

    private final List<Libro> libros = new ArrayList<>();

    public LibroRepository() {
        cargarDatosDePrueba();
    }

    public List<Libro> findAll() {
        return new ArrayList<>(libros);
    }

    public Optional<Libro> findById(int id) {
        return libros.stream().filter(l -> l.getId() == id).findFirst();
    }

    public List<Libro> findDestacados(int cantidad) {
        return libros.subList(0, Math.min(cantidad, libros.size()));
    }

    private void cargarDatosDePrueba() {
        libros.add(new Libro(1, "Clean Code", "Robert C. Martin", "978-0132350884",
                Categoria.TECNOLOGIA,
                "Principios y prácticas para escribir código legible, mantenible y de calidad profesional.",
                true, "#14213D"));

        libros.add(new Libro(2, "Clean Architecture", "Robert C. Martin", "978-0134494166",
                Categoria.TECNOLOGIA,
                "Guía práctica sobre principios de arquitectura de software y buenas prácticas de diseño para sistemas mantenibles.",
                true, "#1D3461"));

        libros.add(new Libro(3, "Refactoring", "Martin Fowler", "978-0134757599",
                Categoria.TECNOLOGIA,
                "Técnicas para mejorar el diseño del código existente sin alterar su comportamiento.",
                true, "#2A6F97"));

        libros.add(new Libro(4, "Diseño de Interfaces", "Don Norman", "978-0465050659",
                Categoria.DISENO,
                "Fundamentos del diseño centrado en el usuario y la usabilidad de los productos digitales.",
                true, "#FCA311"));

        libros.add(new Libro(5, "Sapiens", "Yuval Noah Harari", "978-0062316097",
                Categoria.HISTORIA,
                "Un recorrido por la historia de la humanidad, desde la Edad de Piedra hasta la actualidad.",
                false, "#8D5524"));

        libros.add(new Libro(6, "El Principito", "Antoine de Saint-Exupéry", "978-0156012195",
                Categoria.NOVELA,
                "La entrañable historia de un pequeño príncipe que viaja de planeta en planeta.",
                true, "#588157"));

        libros.add(new Libro(7, "Cien Años de Soledad", "Gabriel García Márquez", "978-0307474728",
                Categoria.LITERATURA,
                "La saga de la familia Buendía en el pueblo mítico de Macondo, obra cumbre del realismo mágico.",
                true, "#9C6644"));

        libros.add(new Libro(8, "Don Quijote de la Mancha", "Miguel de Cervantes", "978-8420412146",
                Categoria.LITERATURA,
                "Las aventuras del ingenioso hidalgo, obra fundacional de la literatura en español.",
                true, "#6D597A"));

        libros.add(new Libro(9, "1984", "George Orwell", "978-0451524935",
                Categoria.NOVELA,
                "Una distopía sobre la vigilancia total y el control totalitario del pensamiento.",
                true, "#355070"));

        libros.add(new Libro(10, "Hábitos Atómicos", "James Clear", "978-0735211292",
                Categoria.DESARROLLO_PERSONAL,
                "Un método práctico y comprobado para crear buenos hábitos y romper los malos.",
                true, "#B56576"));

        libros.add(new Libro(11, "Sapiens: De Animales a Dioses (Ed. Ilustrada)", "Yuval Noah Harari", "978-0062316110",
                Categoria.CIENCIA,
                "Edición ilustrada del recorrido por la historia y evolución de la especie humana.",
                true, "#457B9D"));

        libros.add(new Libro(12, "El Poder del Ahora", "Eckhart Tolle", "978-1577314806",
                Categoria.DESARROLLO_PERSONAL,
                "Una guía para vivir en el presente y liberarse del ruido mental constante.",
                true, "#E76F51"));
    }
}
