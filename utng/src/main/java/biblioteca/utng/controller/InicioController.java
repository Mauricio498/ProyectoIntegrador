package biblioteca.utng.controller;

import biblioteca.utng.model.Libro;
import biblioteca.utng.service.LibroService;
import biblioteca.utng.util.CardFactory;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;

import java.util.function.Consumer;

/**
 * Controlador de la pantalla de Inicio: hero de bienvenida, estadísticas
 * generales y libros destacados.
 */
public class InicioController {

    @FXML private HBox statsContainer;
    @FXML private TilePane destacadosContainer;

    private Runnable irABuscador;
    private Consumer<Libro> irAPrestamoConLibro;

    /** Inyecta dependencias y navegación; se llama justo después de cargar el FXML. */
    public void init(LibroService libroService, Runnable irABuscador, Consumer<Libro> irAPrestamoConLibro) {
        this.irABuscador = irABuscador;
        this.irAPrestamoConLibro = irAPrestamoConLibro;

        cargarEstadisticas();
        cargarDestacados(libroService);
    }

    @FXML
    private void onBuscarCatalogo() {
        if (irABuscador != null) {
            irABuscador.run();
        }
    }

    private void cargarEstadisticas() {
        statsContainer.getChildren().setAll(
                CardFactory.crearTarjetaEstadistica("3,482", "Libros disponibles", "📗"),
                CardFactory.crearTarjetaEstadistica("2", "Préstamos activos", "⏳"),
                CardFactory.crearTarjetaEstadistica("1,240", "Usuarios registrados", "👥")
        );
        for (var nodo : statsContainer.getChildren()) {
            HBox.setHgrow(nodo, javafx.scene.layout.Priority.ALWAYS);
        }
    }

    private void cargarDestacados(LibroService libroService) {
        destacadosContainer.getChildren().clear();
        for (Libro libro : libroService.obtenerDestacados()) {
            destacadosContainer.getChildren().add(
                    CardFactory.crearTarjetaLibro(libro, false, irAPrestamoConLibro)
            );
        }
    }
}
