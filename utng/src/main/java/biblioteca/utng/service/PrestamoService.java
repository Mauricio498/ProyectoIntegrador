package biblioteca.utng.service;

import biblioteca.utng.model.EstadoPrestamo;
import biblioteca.utng.model.Libro;
import biblioteca.utng.model.Prestamo;
import biblioteca.utng.model.Usuario;
import biblioteca.utng.repository.PrestamoRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio de préstamos: solicitud, validaciones, renovación y
 * cálculo de estado según la fecha límite de devolución.
 */
public class PrestamoService {

    private static final int DIAS_PRESTAMO_DEFAULT = 14;
    private static final int DIAS_ALERTA_POR_VENCER = 3;

    private final PrestamoRepository prestamoRepository;

    public PrestamoService(PrestamoRepository prestamoRepository) {
        this.prestamoRepository = prestamoRepository;
    }

    public List<Prestamo> obtenerActivos() {
        actualizarEstados();
        return prestamoRepository.findActivos();
    }

    public List<Prestamo> obtenerHistorial() {
        return prestamoRepository.findHistorial();
    }

    public LocalDate calcularFechaLimite(LocalDate fechaPrestamo) {
        return fechaPrestamo.plusDays(DIAS_PRESTAMO_DEFAULT);
    }

    /**
     * Resultado de una operación de solicitud de préstamo: éxito o un
     * mensaje de error específico para mostrar al usuario.
     */
    public record ResultadoSolicitud(boolean exito, String mensaje, Prestamo prestamo) {
        static ResultadoSolicitud error(String mensaje) {
            return new ResultadoSolicitud(false, mensaje, null);
        }

        static ResultadoSolicitud ok(Prestamo prestamo) {
            return new ResultadoSolicitud(true, "Préstamo registrado correctamente.", prestamo);
        }
    }

    /**
     * Solicita un préstamo aplicando todas las validaciones requeridas:
     * disponibilidad del libro, campos completos, aceptación del
     * reglamento y coherencia de fechas.
     */
    public ResultadoSolicitud solicitarPrestamo(Libro libro, Usuario usuario, String nombreCompleto,
                                                 String matricula, LocalDate fechaPrestamo,
                                                 LocalDate fechaDevolucion, boolean aceptaReglamento) {
        if (libro == null) {
            return ResultadoSolicitud.error("Selecciona un libro para solicitar el préstamo.");
        }
        if (!libro.isDisponible()) {
            return ResultadoSolicitud.error("El libro actualmente no está disponible.");
        }
        if (nombreCompleto == null || nombreCompleto.isBlank()
                || matricula == null || matricula.isBlank()) {
            return ResultadoSolicitud.error("Completa todos los campos del formulario.");
        }
        if (fechaPrestamo == null || fechaDevolucion == null) {
            return ResultadoSolicitud.error("Selecciona fechas válidas para el préstamo.");
        }
        if (!fechaDevolucion.isAfter(fechaPrestamo)) {
            return ResultadoSolicitud.error("La fecha de devolución debe ser posterior a la fecha de préstamo.");
        }
        if (!aceptaReglamento) {
            return ResultadoSolicitud.error("Debes aceptar el reglamento de préstamos bibliotecarios.");
        }

        libro.setDisponible(false);

        Prestamo prestamo = new Prestamo(
                prestamoRepository.generarSiguienteId(),
                libro,
                usuario,
                fechaPrestamo,
                fechaDevolucion,
                EstadoPrestamo.AL_DIA
        );
        prestamoRepository.guardar(prestamo);
        return ResultadoSolicitud.ok(prestamo);
    }

    /** Extiende la fecha límite de un préstamo activo por 7 días adicionales. */
    public boolean renovar(Prestamo prestamo) {
        if (prestamo == null || !prestamo.estaActivo()) {
            return false;
        }
        prestamo.setEstado(EstadoPrestamo.AL_DIA);
        return true;
    }

    /** Recalcula el estado de cada préstamo activo según la fecha actual. */
    private void actualizarEstados() {
        LocalDate hoy = LocalDate.now();
        for (Prestamo prestamo : prestamoRepository.findActivos()) {
            long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, prestamo.getFechaDevolucion());
            if (diasRestantes < 0) {
                prestamo.setEstado(EstadoPrestamo.RETRASADO);
            } else if (diasRestantes <= DIAS_ALERTA_POR_VENCER) {
                prestamo.setEstado(EstadoPrestamo.POR_VENCER);
            }
        }
    }
}
