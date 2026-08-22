package utng.biblioteca.controller.comun;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utng.biblioteca.model.Usuario;
import utng.biblioteca.service.AuthService;
import utng.biblioteca.session.Sesion;
import utng.biblioteca.util.Alertas;

import java.util.Optional;

/**
 * Pantalla de acceso. Es la unica entrada al sistema: a partir del rol que
 * devuelve la base, el {@link Router} decide que interfaz mostrar.
 */
public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMensaje;
    @FXML private Button btnEntrar;
    @FXML private Button btnInvitado;
    @FXML private Button btnSalir;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        lblMensaje.setText("");
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);

        // Enter en cualquiera de los dos campos intenta entrar.
        txtUsuario.setOnAction(e -> entrar());
        txtPassword.setOnAction(e -> entrar());

        Platform.runLater(() -> txtUsuario.requestFocus());
    }

    @FXML
    private void entrar() {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario == null || usuario.isBlank()) {
            mostrarError("Ingresa tu usuario.");
            txtUsuario.requestFocus();
            return;
        }
        if (password == null || password.isEmpty()) {
            mostrarError("Ingresa tu contraseña.");
            txtPassword.requestFocus();
            return;
        }

        btnEntrar.setDisable(true);
        try {
            Optional<Usuario> autenticado = authService.iniciarSesion(usuario, password);

            if (autenticado.isEmpty()) {
                mostrarError("Usuario o contraseña incorrectos.");
                txtPassword.clear();
                txtPassword.requestFocus();
                return;
            }

            Router.mostrarSegunRol();
        } finally {
            btnEntrar.setDisable(false);
        }
    }

    /** Lleva a la pantalla de alta de cuenta. */
    @FXML
    private void irARegistro() {
        Router.mostrarRegistro();
    }

    /**
     * Acceso sin credenciales. Abre la biblioteca digital en modo consulta:
     * el catalogo completo, pero sin prestamos, favoritos ni perfil.
     */
    @FXML
    private void entrarSinCuenta() {
        Sesion.iniciarComoInvitado();
        Router.mostrarSegunRol();
    }

    /**
     * Cierra la aplicación por completo.
     *
     * Se usa Platform.exit() y no System.exit(): el primero dispara App.stop(),
     * que cierra el pool de conexiones y libera las que estén abiertas contra
     * SQL Server. Con System.exit() esas conexiones quedarían colgadas hasta
     * que el servidor las descarte por tiempo.
     */
    @FXML
    private void salir() {
        if (Alertas.confirmar("Salir", "¿Cerrar la aplicación?")) {
            Platform.exit();
        }
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setText(mensaje);
        // managed junto con visible para que el hueco no ocupe espacio vacio.
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }
}
