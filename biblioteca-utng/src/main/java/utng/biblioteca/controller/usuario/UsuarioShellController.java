package utng.biblioteca.controller.usuario;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import utng.biblioteca.controller.comun.Router;
import utng.biblioteca.service.AuthService;
import utng.biblioteca.session.Sesion;
import utng.biblioteca.util.Alertas;
import utng.biblioteca.util.Formato;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Layout de la interfaz de consulta (profesor, estudiante e invitado),
 * heredado del prototipo utng2.
 *
 * El menu se adapta al rol: el invitado no ve Prestamos porque su rol tiene
 * maxMateriales = 0 en la tabla Rol.
 */
public class UsuarioShellController {

    @FXML private StackPane contenido;
    @FXML private Label lblNombre;
    @FXML private Label lblRol;
    @FXML private Label lblIniciales;
    @FXML private Button btnInicio;
    @FXML private Button btnBuscador;
    @FXML private Button btnPrestamos;
    @FXML private Button btnCerrarSesion;

    private final Map<String, Button> botones = new LinkedHashMap<>();
    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        lblNombre.setText(Sesion.getNombre());
        lblRol.setText(Sesion.getNombreRol() + " UTNG");
        lblIniciales.setText(Formato.iniciales(Sesion.getNombre()));

        botones.put("inicio",    btnInicio);
        botones.put("buscador",  btnBuscador);
        botones.put("prestamos", btnPrestamos);

        // Quien entra sin cuenta solo ve el catalogo: no hay idUsuario al que
        // colgarle prestamos, solicitudes ni perfil.
        boolean puedePedir = Sesion.puedeSolicitarPrestamo();

        btnPrestamos.setVisible(puedePedir);
        btnPrestamos.setManaged(puedePedir);
        if (Sesion.esInvitado()) {
            btnCerrarSesion.setText("Iniciar sesión");
        }

        abrirInicio();
    }

    @FXML private void abrirInicio()    { cargar("inicio",   "/fxml/usuario/inicio.fxml"); }
    @FXML private void abrirBuscador()  { cargar("buscador", "/fxml/usuario/buscador.fxml"); }
    @FXML private void abrirPrestamos() { cargar("prestamos","/fxml/usuario/prestamos.fxml"); }

    @FXML
    private void cerrarSesion() {
        if (Sesion.esInvitado()) {
            authService.cerrarSesion();
            Router.mostrarLogin();
            return;
        }
        if (!Alertas.confirmar("Cerrar sesión", "Se cerrará tu sesión. ¿Quieres continuar?")) {
            return;
        }
        authService.cerrarSesion();
        Router.mostrarLogin();
    }

    /** Permite que una pantalla mande a otra (por ejemplo, del inicio al buscador). */
    public void irABuscador() {
        abrirBuscador();
    }

    private void cargar(String clave, String recursoFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(recursoFxml));
            Parent vista = loader.load();

            Object controlador = loader.getController();
            if (controlador instanceof InicioController inicio) {
                inicio.setShell(this);
            }

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
                    "No se pudo abrir el pantalla.\n\n"
                  + causa.getClass().getSimpleName() + ": "
                  + (causa.getMessage() == null ? "sin detalle" : causa.getMessage()));
        }
    }

    private void resaltar(String clave) {
        botones.values().forEach(b -> b.getStyleClass().remove("nav-button-activo"));
        Button activo = botones.get(clave);
        if (activo != null && !activo.getStyleClass().contains("nav-button-activo")) {
            activo.getStyleClass().add("nav-button-activo");
        }
    }
}
