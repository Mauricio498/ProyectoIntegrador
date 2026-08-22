package utng.biblioteca.controller.comun;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.Area;
import utng.biblioteca.model.Rol;
import utng.biblioteca.model.Usuario;
import utng.biblioteca.service.UsuarioService;
import utng.biblioteca.util.Alertas;

import java.util.List;

/**
 * Alta de cuenta hecha por el propio usuario.
 *
 * Solo se ofrecen los roles Estudiante y Profesor: la lista sale de
 * VW_RolPublico, no de una lista escrita a mano, y el procedimiento
 * SP_RegistrarCuenta vuelve a validarlo del lado del motor.
 */
public class RegistroController {

    @FXML private ComboBox<Rol> cmbRol;
    @FXML private ComboBox<Area> cmbArea;
    @FXML private Label lblArea;
    @FXML private Label lblMatricula;
    @FXML private TextField txtNumeroControl;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPassword2;
    @FXML private Label lblMensaje;
    @FXML private Button btnRegistrar;

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    public void initialize() {
        ocultarMensaje();

        List<Rol> roles = usuarioService.rolesPublicos();
        cmbRol.getItems().setAll(roles);

        if (!roles.isEmpty()) {
            cmbRol.getSelectionModel().selectFirst();
        } else {
            // Sin catálogo de roles no se puede registrar nada.
            mostrarMensaje("No se pudieron cargar los tipos de cuenta. "
                         + "Revisa la conexión con la base de datos.", false);
            btnRegistrar.setDisable(true);
        }

        // El área solo se ofrece si ya se ejecutó la migración 05_area.sql.
        List<Area> areas = usuarioService.areas();
        boolean hayAreas = !areas.isEmpty();

        cmbArea.getItems().setAll(areas);
        cmbArea.setVisible(hayAreas);
        cmbArea.setManaged(hayAreas);
        lblArea.setVisible(hayAreas);
        lblArea.setManaged(hayAreas);

        // La etiqueta del identificador cambia según el tipo de cuenta.
        cmbRol.getSelectionModel().selectedItemProperty()
              .addListener((obs, anterior, actual) -> ajustarEtiquetas(actual));
        ajustarEtiquetas(cmbRol.getValue());

        txtPassword2.setOnAction(e -> registrar());

        Platform.runLater(() -> txtNombre.requestFocus());
    }

    private void ajustarEtiquetas(Rol rol) {
        boolean esProfesor = rol != null && "Profesor".equalsIgnoreCase(rol.getNombre());

        lblMatricula.setText(esProfesor ? "Número de empleado" : "Número de control");
        txtNumeroControl.setPromptText(esProfesor ? "DOC-0000" : "A00123456");
    }

    @FXML
    private void registrar() {
        ocultarMensaje();

        Rol rol = cmbRol.getValue();

        Usuario usuario = new Usuario();
        usuario.setNombre(texto(txtNombre));
        usuario.setNumeroControl(texto(txtNumeroControl));
        usuario.setCorreo(texto(txtCorreo));
        usuario.setTelefono(texto(txtTelefono));
        usuario.setUsuario(texto(txtUsuario));
        usuario.setArea(cmbArea.getValue());

        btnRegistrar.setDisable(true);
        try {
            Resultado resultado = usuarioService.registrarCuenta(
                    usuario,
                    txtPassword.getText(),
                    txtPassword2.getText(),
                    rol == null ? null : rol.getNombre());

            if (!resultado.isExito()) {
                mostrarMensaje(resultado.getMensaje(), false);
                return;
            }

            Alertas.info("Cuenta creada",
                    resultado.getMensaje() + System.lineSeparator() + System.lineSeparator()
                  + "Tu usuario es: " + usuario.getUsuario());

            Router.mostrarLogin();
        } finally {
            btnRegistrar.setDisable(false);
        }
    }

    @FXML
    private void volver() {
        Router.mostrarLogin();
    }

    private String texto(TextField campo) {
        String valor = campo.getText();
        return valor == null ? null : valor.trim();
    }

    private void mostrarMensaje(String mensaje, boolean exito) {
        lblMensaje.setText(mensaje);
        lblMensaje.getStyleClass().removeAll("login-error", "login-exito");
        lblMensaje.getStyleClass().add(exito ? "login-exito" : "login-error");
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void ocultarMensaje() {
        lblMensaje.setText("");
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }
}
