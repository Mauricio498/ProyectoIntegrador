package com.utng.biblioteca.controller;

import com.utng.biblioteca.model.EstadoPrestamo;
import com.utng.biblioteca.model.Prestamo;
import com.utng.biblioteca.model.Usuario;
import com.utng.biblioteca.service.PrestamoService;
import com.utng.biblioteca.service.UsuarioService;
import com.utng.biblioteca.util.AlertUtil;
import com.utng.biblioteca.util.TableFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Controlador de la pantalla Perfil: tarjeta de usuario y pestañas de
 * préstamos activos, historial y datos personales.
 */
public class PerfilController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MMM/yy", new Locale("es", "MX"));

    @FXML private Label lblIniciales;
    @FXML private Label lblNombre;
    @FXML private Label lblMatricula;
    @FXML private Label lblCorreo;
    @FXML private Button btnEditarPerfil;

    @FXML private TabPane tabPane;
    @FXML private Tab tabDatosPersonales;

    @FXML private TableView<Prestamo> tablaActivos;
    @FXML private TableColumn<Prestamo, String> colActivoLibro;
    @FXML private TableColumn<Prestamo, String> colActivoPrestamo;
    @FXML private TableColumn<Prestamo, String> colActivoDevolucion;
    @FXML private TableColumn<Prestamo, EstadoPrestamo> colActivoEstado;
    @FXML private TableColumn<Prestamo, Void> colActivoAcciones;

    @FXML private TableView<Prestamo> tablaHistorial;
    @FXML private TableColumn<Prestamo, String> colHistLibro;
    @FXML private TableColumn<Prestamo, String> colHistPrestamo;
    @FXML private TableColumn<Prestamo, String> colHistDevolucion;
    @FXML private TableColumn<Prestamo, EstadoPrestamo> colHistEstado;

    @FXML private TextField txtNombreCompleto;
    @FXML private TextField txtMatricula;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCarrera;
    @FXML private TextField txtGrupo;
    @FXML private Button btnGuardarCambios;

    private PrestamoService prestamoService;
    private UsuarioService usuarioService;

    public void init(PrestamoService prestamoService, UsuarioService usuarioService) {
        this.prestamoService = prestamoService;
        this.usuarioService = usuarioService;

        cargarTarjetaUsuario();
        configurarTablaActivos();
        configurarTablaHistorial();
        cargarDatosPersonales();
    }

    private void cargarTarjetaUsuario() {
        Usuario usuario = usuarioService.obtenerUsuarioActual();
        lblIniciales.setText(usuario.getIniciales());
        lblNombre.setText(usuario.getNombre());
        lblMatricula.setText("Matrícula: " + usuario.getMatricula());
        lblCorreo.setText(usuario.getCorreo());
    }

    private void configurarTablaActivos() {
        colActivoLibro.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLibro().getTitulo()));
        colActivoPrestamo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaPrestamo().format(FORMATO_FECHA)));
        colActivoDevolucion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaDevolucion().format(FORMATO_FECHA)));
        colActivoEstado.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getEstado()));
        TableFactory.configurarColumnaBadge(colActivoEstado);

        colActivoAcciones.setCellFactory(col -> new TableCell<>() {
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
                        tablaActivos.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });

        tablaActivos.getItems().setAll(prestamoService.obtenerActivos());
    }

    private void configurarTablaHistorial() {
        colHistLibro.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLibro().getTitulo()));
        colHistPrestamo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaPrestamo().format(FORMATO_FECHA)));
        colHistDevolucion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaDevolucion().format(FORMATO_FECHA)));
        colHistEstado.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getEstado()));
        TableFactory.configurarColumnaBadge(colHistEstado);

        tablaHistorial.getItems().setAll(prestamoService.obtenerHistorial());
    }

    private void cargarDatosPersonales() {
        Usuario usuario = usuarioService.obtenerUsuarioActual();
        txtNombreCompleto.setText(usuario.getNombre());
        txtMatricula.setText(usuario.getMatricula());
        txtCorreo.setText(usuario.getCorreo());
        txtTelefono.setText(usuario.getTelefono());
        txtCarrera.setText(usuario.getCarrera());
        txtGrupo.setText(usuario.getGrupo());
    }

    @FXML
    private void onEditarPerfil() {
        tabPane.getSelectionModel().select(tabDatosPersonales);
    }

    @FXML
    private void onGuardarCambios() {
        String error = usuarioService.actualizarDatosPersonales(
                txtCorreo.getText(), txtTelefono.getText(), txtCarrera.getText(), txtGrupo.getText());

        if (error != null) {
            AlertUtil.error(error);
            return;
        }

        AlertUtil.exito("Tus datos personales se actualizaron correctamente.");
        lblCorreo.setText(usuarioService.obtenerUsuarioActual().getCorreo());
    }
}
