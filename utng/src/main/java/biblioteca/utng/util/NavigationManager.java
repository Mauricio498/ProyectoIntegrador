package biblioteca.utng.util;

import biblioteca.utng.controller.BuscadorController;
import biblioteca.utng.controller.InicioController;
import biblioteca.utng.controller.PerfilController;
import biblioteca.utng.controller.PrestamosController;
import biblioteca.utng.model.Libro;
import biblioteca.utng.service.LibroService;
import biblioteca.utng.service.PrestamoService;
import biblioteca.utng.service.UsuarioService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controla el cambio de pantallas dentro del área de contenido central,
 * inyecta los servicios necesarios en cada controlador y resalta el
 * botón de navegación actualmente seleccionado.
 */
public class NavigationManager {

    /** Identificadores de las pantallas disponibles. */
    public enum Pantalla { INICIO, BUSCADOR, PRESTAMOS, PERFIL }

    private final StackPane contentArea;
    private final LibroService libroService;
    private final PrestamoService prestamoService;
    private final UsuarioService usuarioService;
    private Map<Pantalla, Button> botonesNavegacion = Map.of();

    private Libro libroSeleccionadoParaPrestamo;

    public NavigationManager(StackPane contentArea, LibroService libroService,
                              PrestamoService prestamoService, UsuarioService usuarioService) {
        this.contentArea = contentArea;
        this.libroService = libroService;
        this.prestamoService = prestamoService;
        this.usuarioService = usuarioService;
    }

    public void setBotonesNavegacion(Map<Pantalla, Button> botones) {
        this.botonesNavegacion = botones;
    }

    public void irAInicio() {
        cargar(Pantalla.INICIO, "/fxml/inicio.fxml", (InicioController c) -> c.init(
                libroService,
                this::irABuscador,
                this::irAPrestamosConLibro
        ));
    }

    public void irABuscador() {
        cargar(Pantalla.BUSCADOR, "/fxml/buscador.fxml", (BuscadorController c) -> c.init(
                libroService,
                this::irAPrestamosConLibro
        ));
    }

    public void irAPrestamos() {
        irAPrestamosConLibro(null);
    }

    public void irAPrestamosConLibro(Libro libro) {
        this.libroSeleccionadoParaPrestamo = libro;
        cargar(Pantalla.PRESTAMOS, "/fxml/prestamos.fxml", (PrestamosController c) -> c.init(
                libroService, prestamoService, usuarioService, libroSeleccionadoParaPrestamo
        ));
    }

    public void irAPerfil() {
        cargar(Pantalla.PERFIL, "/fxml/perfil.fxml", (PerfilController c) -> c.init(
                prestamoService, usuarioService
        ));
    }

    private <T> void cargar(Pantalla pantalla, String recursoFxml, java.util.function.Consumer<T> inicializador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(recursoFxml));
            Parent vista = loader.load();
            T controller = loader.getController();
            inicializador.accept(controller);

            contentArea.getChildren().setAll(vista);
            resaltarBoton(pantalla);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la vista: " + recursoFxml, e);
        }
    }

    private void resaltarBoton(Pantalla pantalla) {
        for (Map.Entry<Pantalla, Button> entry : botonesNavegacion.entrySet()) {
            entry.getValue().getStyleClass().remove("nav-button-activo");
        }
        Button activo = botonesNavegacion.get(pantalla);
        if (activo != null) {
            activo.getStyleClass().add("nav-button-activo");
        }
    }

    public List<Pantalla> pantallas() {
        return List.of(Pantalla.values());
    }
}
