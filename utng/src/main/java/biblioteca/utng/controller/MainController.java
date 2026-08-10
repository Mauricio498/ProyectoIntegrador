package com.utng.biblioteca.controller;

import com.utng.biblioteca.model.Usuario;
import com.utng.biblioteca.repository.LibroRepository;
import com.utng.biblioteca.repository.PrestamoRepository;
import com.utng.biblioteca.repository.UsuarioRepository;
import com.utng.biblioteca.service.LibroService;
import com.utng.biblioteca.service.PrestamoService;
import com.utng.biblioteca.service.UsuarioService;
import com.utng.biblioteca.util.NavigationManager;
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
