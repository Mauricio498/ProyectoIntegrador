package utng.biblioteca.controller.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import utng.biblioteca.controller.comun.Router;
import utng.biblioteca.service.AuthService;
import utng.biblioteca.service.PrestamoService;
import utng.biblioteca.session.Sesion;
import utng.biblioteca.util.Alertas;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Layout de la interfaz de gestion (superadministrador y administrador),
 * heredado del prototipo utng1: barra superior, menu lateral y area central
 * donde se cargan los modulos.
 */
public class AdminShellController {

    @FXML private StackPane contenido;
    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private Button btnDashboard;
    @FXML private Button btnMateriales;
    @FXML private Button btnUsuarios;
    @FXML private Button btnPrestamos;
    @FXML private Button btnMultas;
    @FXML private Button btnSolicitudes;
    @FXML private Button btnEstadisticas;

    private final Map<String, Button> botones = new LinkedHashMap<>();
    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        lblUsuario.setText(Sesion.getNombre());
        lblRol.setText(Sesion.getNombreRol());

        botones.put("dashboard",   btnDashboard);
        botones.put("materiales",  btnMateriales);
        botones.put("usuarios",    btnUsuarios);
        botones.put("prestamos",   btnPrestamos);
        botones.put("multas",      btnMultas);
        botones.put("solicitudes", btnSolicitudes);
        botones.put("estadisticas", btnEstadisticas);

        // Al entrar el personal de biblioteca se ponen al dia los prestamos
        // cuya fecha limite ya paso, para que el tablero refleje la realidad.
        int vencidos = new PrestamoService().actualizarVencidos();
        if (vencidos > 0) {
            System.out.println("Préstamos marcados como vencidos al iniciar: " + vencidos);
        }

        abrirDashboard();
    }

    @FXML private void abrirDashboard()   { cargar("dashboard",  "/fxml/admin/dashboard.fxml"); }
    @FXML private void abrirMateriales()  { cargar("materiales", "/fxml/admin/materiales.fxml"); }
    @FXML private void abrirPrestamos()   { cargar("prestamos",  "/fxml/admin/prestamos.fxml"); }
    @FXML private void abrirUsuarios()    { cargar("usuarios",   "/fxml/admin/usuarios.fxml"); }
    @FXML private void abrirMultas()      { cargar("multas",     "/fxml/admin/multas.fxml"); }
    @FXML private void abrirSolicitudes()  { cargar("solicitudes",  "/fxml/admin/solicitudes.fxml"); }
    @FXML private void abrirEstadisticas() { cargar("estadisticas", "/fxml/admin/estadisticas.fxml"); }

    @FXML
    private void cerrarSesion() {
        if (!Alertas.confirmar("Cerrar sesión", "Se cerrará tu sesión. ¿Quieres continuar?")) {
            return;
        }
        authService.cerrarSesion();
        Router.mostrarLogin();
    }

    private void cargar(String clave, String recursoFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(recursoFxml));
            Node vista = loader.load();
            contenido.getChildren().setAll(vista);
            resaltar(clave);
        } catch (IOException | RuntimeException e) {
            // FXMLLoader envuelve cualquier fallo del controlador, así que el
            // mensaje de primer nivel casi siempre viene vacío. Se desenvuelve
            // hasta la causa real para poder diagnosticar.
            Throwable causa = e;
            while (causa.getCause() != null) {
                causa = causa.getCause();
            }

            e.printStackTrace();

            Alertas.error("Biblioteca UTNG",
                    "No se pudo abrir el módulo.\n\n"
                  + causa.getClass().getSimpleName() + ": "
                  + (causa.getMessage() == null ? "sin detalle" : causa.getMessage()));
        }
    }

    private void pendiente(String modulo) {
        Alertas.info("Módulo en construcción",
                "El módulo de " + modulo + " todavía no está conectado a esta pantalla.");
    }

    private void resaltar(String clave) {
        botones.values().forEach(b -> b.getStyleClass().remove("sidebar-button-activo"));
        Button activo = botones.get(clave);
        if (activo != null && !activo.getStyleClass().contains("sidebar-button-activo")) {
            activo.getStyleClass().add("sidebar-button-activo");
        }
    }
}
