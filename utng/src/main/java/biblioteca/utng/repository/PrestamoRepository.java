package biblioteca.utng.repository;

import biblioteca.utng.model.EstadoPrestamo;
import biblioteca.utng.model.Libro;
import biblioteca.utng.model.Prestamo;
import biblioteca.utng.model.Usuario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a los datos de préstamos, tanto activos como históricos.
 */
public class PrestamoRepository {

    private final List<Prestamo> prestamos = new ArrayList<>();
    private int siguienteId;

    public PrestamoRepository(LibroRepository libroRepository, UsuarioRepository usuarioRepository) {
        cargarDatosDePrueba(libroRepository, usuarioRepository);
    }

    public List<Prestamo> findAll() {
        return new ArrayList<>(prestamos);
    }

    public List<Prestamo> findActivos() {
        return prestamos.stream().filter(Prestamo::estaActivo).toList();
    }

    public List<Prestamo> findHistorial() {
        return prestamos.stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.DEVUELTO)
                .toList();
    }

    public void guardar(Prestamo prestamo) {
        prestamos.add(0, prestamo);
    }

    public int generarSiguienteId() {
        return ++siguienteId;
    }

    private void cargarDatosDePrueba(LibroRepository libroRepository, UsuarioRepository usuarioRepository) {
        Usuario ana = usuarioRepository.getUsuarioActual();

        List<Libro> libros = libroRepository.findAll();
        Libro cleanArchitecture = libros.get(1); // id 2
        Libro refactoring = libros.get(2);       // id 3
        Libro sapiens = libros.get(4);           // id 5 (prestado)
        Libro quijote = libros.get(7);           // id 8
        Libro habitos = libros.get(9);            // id 10

        siguienteId = 0;
        LocalDate hoy = LocalDate.now();

        // Préstamo cuya fecha límite está muy próxima (2 días).
        prestamos.add(new Prestamo(++siguienteId, cleanArchitecture, ana,
                hoy.minusDays(12), hoy.plusDays(2), EstadoPrestamo.POR_VENCER));

        // Préstamo con margen amplio antes de vencer.
        prestamos.add(new Prestamo(++siguienteId, refactoring, ana,
                hoy.minusDays(5), hoy.plusDays(9), EstadoPrestamo.AL_DIA));

        // Historial: devueltos en distintos momentos del pasado.
        Prestamo prestamoSapiens = new Prestamo(++siguienteId, sapiens, ana,
                hoy.minusDays(99), hoy.minusDays(85), EstadoPrestamo.DEVUELTO);
        prestamoSapiens.setFechaDevueltoReal(hoy.minusDays(86));
        prestamos.add(prestamoSapiens);

        Prestamo prestamoQuijote = new Prestamo(++siguienteId, quijote, ana,
                hoy.minusDays(130), hoy.minusDays(116), EstadoPrestamo.DEVUELTO);
        prestamoQuijote.setFechaDevueltoReal(hoy.minusDays(113));
        prestamos.add(prestamoQuijote);

        Prestamo prestamoHabitos = new Prestamo(++siguienteId, habitos, ana,
                hoy.minusDays(160), hoy.minusDays(146), EstadoPrestamo.DEVUELTO);
        prestamoHabitos.setFechaDevueltoReal(hoy.minusDays(147));
        prestamos.add(prestamoHabitos);
    }
}
