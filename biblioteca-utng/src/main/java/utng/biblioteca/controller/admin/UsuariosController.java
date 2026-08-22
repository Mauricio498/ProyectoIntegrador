package utng.biblioteca.controller.admin;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.Area;
import utng.biblioteca.model.Rol;
import utng.biblioteca.model.Usuario;
import utng.biblioteca.service.MultaService;
import utng.biblioteca.service.PrestamoService;
import utng.biblioteca.service.UsuarioService;
import utng.biblioteca.util.Alertas;
import utng.biblioteca.util.Formato;
import utng.biblioteca.util.Paginador;

import java.util.List;

/**
 * Alta, edicion y baja de usuarios.
 *
 * Los limites de prestamo no se editan aqui: viven en la tabla Rol, asi que
 * cambiar el rol de una persona cambia automaticamente cuantos materiales
 * puede llevarse y por cuantos dias.
 */
public class UsuariosController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Rol> cmbFiltroRol;
    @FXML private CheckBox chkSoloActivos;
    @FXML private Label lblResumen;

    // ---- Paginación
    @FXML private ComboBox<Integer> cmbPorPagina;
    @FXML private Label lblPagina;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;

    // ---- Panel plegable del formulario
    @FXML private SplitPane divisor;
    @FXML private ScrollPane panelFormulario;
    @FXML private Button btnPanel;
    @FXML private Button btnNuevo;

    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colNumeroControl;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colCorreo;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colArea;
    @FXML private TableColumn<Usuario, String> colLimite;
    @FXML private TableColumn<Usuario, String> colEstado;

    // ---- Formulario
    @FXML private Label lblFormulario;
    @FXML private TextField txtNumeroControl;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private ComboBox<Area> cmbArea;
    @FXML private Label lblArea;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblDetalle;
    @FXML private Button btnBaja;

    private final UsuarioService usuarioService = new UsuarioService();
    private final PrestamoService prestamoService = new PrestamoService();
    private final MultaService multaService = new MultaService();

    private Paginador<Usuario> paginador;

    /** Proporción del divisor cuando el formulario está visible. */
    private static final double DIVISOR_ABIERTO = 0.68;

    /** Usuario que se esta editando; null cuando el formulario es un alta. */
    private Usuario enEdicion;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarRoles();

        paginador = new Paginador<>(tblUsuarios, cmbPorPagina, lblPagina,
                                    btnAnterior, btnSiguiente);

        txtBuscar.setOnAction(e -> buscar());
        tblUsuarios.getSelectionModel().selectedItemProperty()
                   .addListener((obs, anterior, actual) -> mostrarEnFormulario(actual));

        nuevo();
        buscar();

        // El formulario arranca oculto: la pantalla se abre mostrando el padrón.
        cerrarPanel();
    }

    // ------------------------------------------------- panel plegable

    /** Muestra u oculta el formulario lateral. */
    @FXML
    private void alternarPanel() {
        if (panelAbierto()) {
            cerrarPanel();
        } else {
            abrirPanel();
        }
    }

    private void abrirPanel() {
        if (!divisor.getItems().contains(panelFormulario)) {
            divisor.getItems().add(panelFormulario);
        }
        // El divisor se coloca después de que el SplitPane recalcula su
        // distribución; si se hace en el mismo pulso, JavaFX lo ignora.
        Platform.runLater(() -> divisor.setDividerPositions(DIVISOR_ABIERTO));
        btnPanel.setText("Ocultar formulario");
    }

    private void cerrarPanel() {
        // En un SplitPane no basta con ocultar el nodo: hay que sacarlo de
        // los elementos, o sigue reservando su espacio y su divisor.
        divisor.getItems().remove(panelFormulario);
        btnPanel.setText("Mostrar formulario");
    }

    private boolean panelAbierto() {
        return divisor.getItems().contains(panelFormulario);
    }

    private void configurarTabla() {
        colNumeroControl.setCellValueFactory(d -> texto(d.getValue().getNumeroControl()));
        colNombre.setCellValueFactory(d -> texto(d.getValue().getNombre()));
        colCorreo.setCellValueFactory(d -> texto(d.getValue().getCorreo()));
        colUsuario.setCellValueFactory(d -> texto(d.getValue().getUsuario()));
        colRol.setCellValueFactory(d -> texto(d.getValue().getRol().getNombre()));
        colArea.setCellValueFactory(d -> texto(d.getValue().getNombreArea()));
        colLimite.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRol().getMaxMateriales() + " mat. / "
              + d.getValue().getRol().getPlazoTexto()));
        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isEstado() ? "Activo" : "Baja"));

        tblUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblUsuarios.setPlaceholder(new Label("Sin usuarios que coincidan con el filtro."));
    }

    private void cargarRoles() {
        List<Rol> roles = usuarioService.roles();

        Rol todos = new Rol();
        todos.setIdRol(0);
        todos.setNombre("Todos los roles");

        cmbFiltroRol.getItems().add(todos);
        cmbFiltroRol.getItems().addAll(roles);
        cmbFiltroRol.getSelectionModel().selectFirst();

        cmbRol.getItems().addAll(roles);

        // El área solo aparece si ya se ejecutó la migración 05_area.sql.
        List<Area> areas = usuarioService.areas();
        boolean hayAreas = !areas.isEmpty();

        cmbArea.getItems().setAll(areas);
        cmbArea.setVisible(hayAreas);
        cmbArea.setManaged(hayAreas);
        lblArea.setVisible(hayAreas);
        lblArea.setManaged(hayAreas);

        colArea.setVisible(hayAreas);
    }

    @FXML
    private void buscar() {
        Rol rol = cmbFiltroRol.getValue();
        Integer idRol = (rol == null || rol.getIdRol() == 0) ? null : rol.getIdRol();

        List<Usuario> usuarios =
                usuarioService.listar(txtBuscar.getText(), idRol, chkSoloActivos.isSelected());

        paginador.setDatos(usuarios);
        lblResumen.setText(usuarios.size() + " usuario(s)");
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroRol.getSelectionModel().selectFirst();
        chkSoloActivos.setSelected(false);
        buscar();
    }

    /** Deja el formulario listo para capturar un usuario nuevo. */
    @FXML
    private void nuevo() {
        enEdicion = null;
        tblUsuarios.getSelectionModel().clearSelection();

        abrirPanel();
        lblFormulario.setText("Nuevo usuario");
        txtNumeroControl.clear();
        txtNombre.clear();
        txtCorreo.clear();
        txtTelefono.clear();
        txtUsuario.clear();
        txtPassword.clear();
        txtPassword.setPromptText("Obligatoria si defines un usuario de acceso");
        cmbRol.getSelectionModel().clearSelection();
        cmbArea.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        lblDetalle.setText("");
        btnBaja.setDisable(true);
    }

    private void mostrarEnFormulario(Usuario usuario) {
        if (usuario == null) {
            return;
        }

        enEdicion = usuario;

        abrirPanel();
        lblFormulario.setText("Editando: " + usuario.getNombre());
        txtNumeroControl.setText(usuario.getNumeroControl());
        txtNombre.setText(usuario.getNombre());
        txtCorreo.setText(usuario.getCorreo());
        txtTelefono.setText(usuario.getTelefono());
        txtUsuario.setText(usuario.getUsuario());
        txtPassword.clear();
        txtPassword.setPromptText("Dejar vacio para conservar la actual");
        cmbRol.getSelectionModel().select(buscarRol(usuario.getRol().getIdRol()));
        seleccionarArea(usuario.getArea());
        chkActivo.setSelected(usuario.isEstado());
        btnBaja.setDisable(false);
        btnBaja.setText(usuario.isEstado() ? "Dar de baja" : "Reactivar");

        lblDetalle.setText("Alta: " + Formato.fechaHora(usuario.getFechaRegistro())
                         + "   |   Materiales en su poder: "
                         + prestamoService.materialesEnPoder(usuario.getIdUsuario())
                         + "   |   Adeudo: "
                         + Formato.moneda(multaService.adeudoDe(usuario.getIdUsuario())));
    }

    private void seleccionarArea(Area area) {
        cmbArea.getSelectionModel().clearSelection();
        if (area == null) {
            return;
        }
        cmbArea.getItems().stream()
               .filter(a -> a.getIdArea() == area.getIdArea())
               .findFirst()
               .ifPresent(a -> cmbArea.getSelectionModel().select(a));
    }

    private Rol buscarRol(int idRol) {
        return cmbRol.getItems().stream()
                     .filter(r -> r.getIdRol() == idRol)
                     .findFirst()
                     .orElse(null);
    }

    @FXML
    private void guardar() {
        Rol rol = cmbRol.getValue();
        if (rol == null) {
            Alertas.advertencia("Guardar", "Selecciona el rol del usuario.");
            return;
        }

        Usuario usuario = enEdicion == null ? new Usuario() : enEdicion;
        usuario.setNumeroControl(txtNumeroControl.getText());
        usuario.setNombre(txtNombre.getText());
        usuario.setCorreo(txtCorreo.getText());
        usuario.setTelefono(txtTelefono.getText());
        usuario.setUsuario(txtUsuario.getText());
        usuario.setRol(rol);
        usuario.setArea(cmbArea.getValue());
        usuario.setEstado(chkActivo.isSelected());

        Resultado resultado = usuarioService.guardar(usuario, txtPassword.getText());

        if (resultado.isExito()) {
            Alertas.info("Listo", resultado.getMensaje());
            nuevo();
            buscar();
        } else {
            Alertas.advertencia("No se pudo guardar", resultado.getMensaje());
        }
    }

    @FXML
    private void cambiarEstado() {
        if (enEdicion == null) {
            return;
        }

        boolean activar = !enEdicion.isEstado();
        String texto = activar
                ? "Se reactivará la cuenta de " + enEdicion.getNombre() + "."
                : "Se dará de baja a " + enEdicion.getNombre()
                  + ". No podrá solicitar préstamos ni iniciar sesión.";

        if (!Alertas.confirmar(activar ? "Reactivar usuario" : "Dar de baja", texto)) {
            return;
        }

        Resultado resultado = usuarioService.cambiarEstado(enEdicion.getIdUsuario(), activar);

        if (resultado.isExito()) {
            Alertas.info("Listo", resultado.getMensaje());
            nuevo();
            buscar();
        } else {
            Alertas.advertencia("No se pudo completar", resultado.getMensaje());
        }
    }

    @FXML
    private void verPrestamos() {
        if (enEdicion == null) {
            Alertas.advertencia("Préstamos", "Selecciona un usuario de la tabla.");
            return;
        }

        StringBuilder detalle = new StringBuilder();
        prestamoService.listarDe(enEdicion.getIdUsuario(), true).forEach(p ->
                detalle.append(p.getTitulo())
                       .append("  (").append(p.getCodigoEjemplar()).append(")")
                       .append("  -  vence ").append(Formato.fecha(p.getFechaLimite()))
                       .append("  -  ").append(p.getEtiquetaEstado())
                       .append(System.lineSeparator()));

        Alertas.info("Materiales en poder de " + enEdicion.getNombre(),
                detalle.length() == 0 ? "No tiene materiales sin devolver." : detalle.toString());
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor == null ? "" : valor);
    }
}
