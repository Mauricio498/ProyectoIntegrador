package biblioteca.utng.controller;

import biblioteca.utng.model.Categoria;
import biblioteca.utng.model.Libro;
import biblioteca.utng.service.LibroService;
import biblioteca.utng.service.LibroService.FiltroDisponibilidad;
import biblioteca.utng.util.CardFactory;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Controlador de la pantalla Buscador: campo de búsqueda libre, filtros
 * por categoría, autor y disponibilidad, y resultados en tarjetas.
 */
public class BuscadorController {

    private static final String OPCION_TODAS = "Todas las categorías";
    private static final String OPCION_TODOS_AUTORES = "Todos los autores";

    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<String> comboCategoria;
    @FXML private ComboBox<String> comboAutor;
    @FXML private ComboBox<FiltroDisponibilidad> comboDisponibilidad;
    @FXML private Label lblResultados;
    @FXML private TilePane resultadosContainer;

    private LibroService libroService;
    private Consumer<Libro> irAPrestamoConLibro;

    public void init(LibroService libroService, Consumer<Libro> irAPrestamoConLibro) {
        this.libroService = libroService;
        this.irAPrestamoConLibro = irAPrestamoConLibro;

        configurarFiltros();
        ejecutarBusqueda();
    }

    private void configurarFiltros() {
        comboCategoria.getItems().add(OPCION_TODAS);
        for (Categoria categoria : Categoria.values()) {
            comboCategoria.getItems().add(categoria.getEtiqueta());
        }
        comboCategoria.getSelectionModel().select(OPCION_TODAS);

        comboAutor.getItems().add(OPCION_TODOS_AUTORES);
        List<String> autores = libroService.obtenerTodos().stream()
                .map(Libro::getAutor)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        comboAutor.getItems().addAll(autores);
        comboAutor.getSelectionModel().select(OPCION_TODOS_AUTORES);

        comboDisponibilidad.getItems().addAll(FiltroDisponibilidad.values());
        comboDisponibilidad.getSelectionModel().select(FiltroDisponibilidad.TODOS);
    }

    @FXML
    private void onBuscar() {
        ejecutarBusqueda();
    }

    @FXML
    private void onAplicarFiltros() {
        ejecutarBusqueda();
    }

    private void ejecutarBusqueda() {
        Categoria categoriaSeleccionada = resolverCategoria(comboCategoria.getValue());
        String autorSeleccionado = comboAutor.getValue();
        FiltroDisponibilidad disponibilidad = comboDisponibilidad.getValue();

        List<Libro> resultados = libroService.buscar(txtBusqueda.getText(), categoriaSeleccionada, disponibilidad);

        if (autorSeleccionado != null && !autorSeleccionado.equals(OPCION_TODOS_AUTORES)) {
            resultados = resultados.stream()
                    .filter(l -> l.getAutor().equals(autorSeleccionado))
                    .toList();
        }

        mostrarResultados(resultados);
    }

    private Categoria resolverCategoria(String etiqueta) {
        if (etiqueta == null || etiqueta.equals(OPCION_TODAS)) {
            return null;
        }
        for (Categoria categoria : Categoria.values()) {
            if (categoria.getEtiqueta().equals(etiqueta)) {
                return categoria;
            }
        }
        return null;
    }

    private void mostrarResultados(List<Libro> libros) {
        resultadosContainer.getChildren().clear();
        for (Libro libro : libros) {
            resultadosContainer.getChildren().add(
                    CardFactory.crearTarjetaLibro(libro, true, irAPrestamoConLibro)
            );
        }
        lblResultados.setText(libros.size() + (libros.size() == 1 ? " libro encontrado" : " libros encontrados"));
    }
}
