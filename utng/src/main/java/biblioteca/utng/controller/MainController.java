package biblioteca.utng.controller;

import biblioteca.utng.model.Usuario;
import biblioteca.utng.repository.LibroRepository;
import biblioteca.utng.repository.PrestamoRepository;
import biblioteca.utng.repository.UsuarioRepository;
import biblioteca.utng.service.LibroService;
import biblioteca.utng.service.PrestamoService;
import biblioteca.utng.service.UsuarioService;
import biblioteca.utng.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Map;

/**
 * Controlador del layout principal: arma el sidebar de navegación, la
 * barra superior con el usuario en sesión, y delega el cambio de
 * pantallas a {@link NavigationManager}.
 */
public class MainController {

    @FXML private Button btnInicio;
    @FXML private Button btnBuscador;
    @FXML private Button btnPrestamos;
    @FXML private Button btnPerfil;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblIniciales;
    @FXML private StackPane contentArea;

    private NavigationManager navigationManager;

    @FXML
    public void initialize() {
        // Capa de datos y servicios (instanciados una sola vez para toda la app).
        LibroRepository libroRepository = new LibroRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        PrestamoRepository prestamoRepository = new PrestamoRepository(libroRepository, usuarioRepository);

        LibroService libroService = new LibroService(libroRepository);
        UsuarioService usuarioService = new UsuarioService(usuarioRepository);
        PrestamoService prestamoService = new PrestamoService(prestamoRepository);

        navigationManager = new NavigationManager(contentArea, libroService, prestamoService, usuarioService);
        navigationManager.setBotonesNavegacion(Map.of(
                NavigationManager.Pantalla.INICIO, btnInicio,
                NavigationManager.Pantalla.BUSCADOR, btnBuscador,
                NavigationManager.Pantalla.PRESTAMOS, btnPrestamos,
                NavigationManager.Pantalla.PERFIL, btnPerfil
        ));

        Usuario usuario = usuarioService.obtenerUsuarioActual();
        lblNombreUsuario.setText(usuario.getNombre());
        lblIniciales.setText(usuario.getIniciales());

        navigationManager.irAInicio();
    }

    @FXML
    private void irAInicio() {
        navigationManager.irAInicio();
    }

    @FXML
    private void irABuscador() {
        navigationManager.irABuscador();
    }

    @FXML
    private void irAPrestamos() {
        navigationManager.irAPrestamos();
    }

    @FXML
    private void irAPerfil() {
        navigationManager.irAPerfil();
    }
}
