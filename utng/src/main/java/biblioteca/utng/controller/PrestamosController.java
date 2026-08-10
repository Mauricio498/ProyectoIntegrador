package biblioteca.utng.controller;

import biblioteca.utng.model.EstadoPrestamo;
import biblioteca.utng.model.Libro;
import biblioteca.utng.model.Prestamo;
import biblioteca.utng.model.Usuario;
import biblioteca.utng.service.LibroService;
import biblioteca.utng.service.PrestamoService;
import biblioteca.utng.service.PrestamoService.ResultadoSolicitud;
import biblioteca.utng.service.UsuarioService;
import biblioteca.utng.util.AlertUtil;
import biblioteca.utng.util.TableFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Controlador de la pantalla Préstamos: información del libro elegido,
 * formulario de solicitud con validaciones, y tabla de préstamos activos.
 */
public class PrestamosController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MMM/yy", Locale.of("es", "MX"));

    @FXML private ComboBox<Libro> comboLibro;
    @FXML private StackPane portadaGrande;
    @FXML private Label lblIniciales;
    @FXML private Label lblTitulo;
    @FXML private Label lblAutor;
    @FXML private Label lblBadge;
    @FXML private Label lblDescripcion;

    @FXML private TextField txtNombre;
    @FXML private TextField txtMatricula;
    @FXML private DatePicker dpFechaPrestamo;
    @FXML private Label lblFechaLimite;
    @FXML private CheckBox chkReglamento;
    @FXML private Button btnConfirmar;

    @FXML private TableView<Prestamo> tablaActivos;
    @FXML private TableColumn<Prestamo, String> colLibro;
    @FXML private TableColumn<Prestamo, String> colPrestamo;
    @FXML private TableColumn<Prestamo, String> colDevolucion;
    @FXML private TableColumn<Prestamo, EstadoPrestamo> colEstado;
    @FXML private TableColumn<Prestamo, Void> colAcciones;

    private LibroService libroService;
    private PrestamoService prestamoService;
    private Usuario usuarioActual;

    public void init(LibroService libroService, PrestamoService prestamoService,
                      UsuarioService usuarioService, Libro libroPreseleccionado) {
        this.libroService = libroService;
        this.prestamoService = prestamoService;
        this.usuarioActual = usuarioService.obtenerUsuarioActual();

        configurarFormulario();
        inicializarComboLibro();
        poblarComboLibro(libroPreseleccionado);
        configurarTabla();
        cargarPrestamosActivos();
    }

    private void configurarFormulario() {
        txtNombre.setText(usuarioActual.getNombre());
        txtMatricula.setText(usuarioActual.getMatricula());
        dpFechaPrestamo.setValue(LocalDate.now());
        actualizarFechaLimite();
        dpFechaPrestamo.valueProperty().addListener((obs, viejo, nuevo) -> actualizarFechaLimite());
    }

    private void actualizarFechaLimite() {
        LocalDate fechaPrestamo = dpFechaPrestamo.getValue();
        if (fechaPrestamo == null) {
            lblFechaLimite.setText("--");
            return;
        }
        LocalDate limite = prestamoService.calcularFechaLimite(fechaPrestamo);
        lblFechaLimite.setText(limite.format(FORMATO_FECHA));
    }

    private void inicializarComboLibro() {
        comboLibro.setCellFactory(lv -> crearCeldaLibro());
        comboLibro.setButtonCell(crearCeldaLibro());
        comboLibro.valueProperty().addListener((obs, viejo, nuevo) -> mostrarInfoLibro(nuevo));
    }

    private void poblarComboLibro(Libro libroPreseleccionado) {
        List<Libro> disponibles = libroService.obtenerTodos().stream()
                .filter(Libro::isDisponible)
                .toList();

        comboLibro.getItems().setAll(disponibles);

        if (libroPreseleccionado != null && disponibles.contains(libroPreseleccionado)) {
            comboLibro.getSelectionModel().select(libroPreseleccionado);
        } else if (!disponibles.isEmpty()) {
            comboLibro.getSelectionModel().select(0);
        } else {
            mostrarInfoLibro(null);
        }
    }

    private javafx.scene.control.ListCell<Libro> crearCeldaLibro() {
        return new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Libro libro, boolean empty) {
                super.updateItem(libro, empty);
                setText(empty || libro == null ? null : libro.getTitulo() + " — " + libro.getAutor());
            }
        };
    }

    private void mostrarInfoLibro(Libro libro) {
        if (libro == null) {
            lblTitulo.setText("No hay libros disponibles");
            lblAutor.setText("");
            lblDescripcion.setText("");
            lblBadge.setText("");
            lblIniciales.setText("");
            btnConfirmar.setDisable(true);
            return;
        }
        portadaGrande.setStyle("-fx-background-color: " + libro.getColorPortada() + ";");
        lblIniciales.setText(libro.getIniciales());
        lblTitulo.setText(libro.getTitulo());
        lblAutor.setText(libro.getAutor());
        lblDescripcion.setText(libro.getDescripcion());
        lblBadge.setText("Disponible");
        lblBadge.getStyleClass().setAll("badge", "badge-disponible");
        btnConfirmar.setDisable(false);
    }

    private void configurarTabla() {
        colLibro.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLibro().getTitulo()));
        colPrestamo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaPrestamo().format(FORMATO_FECHA)));
        colDevolucion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaDevolucion().format(FORMATO_FECHA)));
        colEstado.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEstado()));
        TableFactory.configurarColumnaBadge(colEstado);

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("Ver detalle");
            private final Button btnRenovar = new Button("Renovar");
            private final HBox contenedor = new HBox(8, btnVer, btnRenovar);

            {
                btnVer.getStyleClass().add("btn-accion");
                btnRenovar.getStyleClass().add("btn-accion-primaria");
                btnVer.setOnAction(e -> {
                    Prestamo prestamo = getTableView().getItems().get(getIndex());
                    AlertUtil.exito("Libro: " + prestamo.getLibro().getTitulo()
                            + "\nPréstamo: " + prestamo.getFechaPrestamo().format(FORMATO_FECHA)
                            + "\nDevolución: " + prestamo.getFechaDevolucion().format(FORMATO_FECHA)
                            + "\nEstado: " + prestamo.getEstado().getEtiqueta());
                });
                btnRenovar.setOnAction(e -> {
                    Prestamo prestamo = getTableView().getItems().get(getIndex());
                    if (prestamoService.renovar(prestamo)) {
                        AlertUtil.exito("El préstamo de \"" + prestamo.getLibro().getTitulo() + "\" fue renovado.");
                        cargarPrestamosActivos();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private void cargarPrestamosActivos() {
        tablaActivos.getItems().setAll(prestamoService.obtenerActivos());
    }

    @FXML
    private void onConfirmarPrestamo() {
        Libro libro = comboLibro.getValue();

        boolean confirmar = AlertUtil.confirmar("¿Confirmar préstamo?",
                libro == null ? "Selecciona un libro primero."
                        : "Estás solicitando el libro " + libro.getTitulo() + ".");
        if (!confirmar) {
            return;
        }

        ResultadoSolicitud resultado = prestamoService.solicitarPrestamo(
                libro,
                usuarioActual,
                txtNombre.getText(),
                txtMatricula.getText(),
                dpFechaPrestamo.getValue(),
                prestamoService.calcularFechaLimite(dpFechaPrestamo.getValue() == null ? LocalDate.now() : dpFechaPrestamo.getValue()),
                chkReglamento.isSelected()
        );

        if (!resultado.exito()) {
            AlertUtil.error(resultado.mensaje());
            return;
        }

        AlertUtil.exito(resultado.mensaje());
        chkReglamento.setSelected(false);
        poblarComboLibro(null);
        cargarPrestamosActivos();
    }
}
