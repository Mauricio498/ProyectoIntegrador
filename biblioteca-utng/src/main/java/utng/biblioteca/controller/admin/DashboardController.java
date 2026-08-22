package utng.biblioteca.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import utng.biblioteca.dto.EstadisticasGenerales;
import utng.biblioteca.dto.PrestamoDetalle;
import utng.biblioteca.service.EstadisticaService;
import utng.biblioteca.service.PrestamoService;
import utng.biblioteca.session.Sesion;
import utng.biblioteca.util.Formato;

/**
 * Tablero del personal de biblioteca. Todos los numeros vienen de
 * VW_EstadisticasGenerales, de modo que el conteo se hace en el motor y no
 * recorriendo listas en Java.
 */
public class DashboardController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblMateriales;
    @FXML private Label lblDisponibles;
    @FXML private Label lblPrestamos;
    @FXML private Label lblRetrasos;
    @FXML private Label lblUsuarios;
    @FXML private Label lblSolicitudes;
    @FXML private Label lblSolicitudesPrestamo;
    @FXML private Label lblMultas;
    @FXML private Label lblEstado;

    @FXML private TableView<PrestamoDetalle> tblRetrasos;
    @FXML private TableColumn<PrestamoDetalle, String> colUsuario;
    @FXML private TableColumn<PrestamoDetalle, String> colTitulo;
    @FXML private TableColumn<PrestamoDetalle, String> colCodigo;
    @FXML private TableColumn<PrestamoDetalle, String> colLimite;
    @FXML private TableColumn<PrestamoDetalle, String> colDias;

    private final EstadisticaService estadisticaService = new EstadisticaService();
    private final PrestamoService prestamoService = new PrestamoService();

    @FXML
    public void initialize() {
        lblBienvenida.setText("Hola, " + Sesion.getNombre());

        configurarTabla();
        cargar();
    }

    private void configurarTabla() {
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsuarioNombre()));
        colTitulo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitulo()));
        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigoEjemplar()));
        colLimite.setCellValueFactory(d ->
                new SimpleStringProperty(Formato.fecha(d.getValue().getFechaLimite())));
        colDias.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getDiasRetraso())));

        tblRetrasos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblRetrasos.setPlaceholder(new Label("Sin devoluciones vencidas."));
    }

    @FXML
    private void cargar() {
        EstadisticasGenerales e = estadisticaService.generales();

        lblMateriales.setText(String.valueOf(e.getTotalMateriales()));
        lblDisponibles.setText(String.valueOf(e.getEjemplaresDisponibles()));
        lblPrestamos.setText(String.valueOf(e.getPrestamosActivos()));
        lblRetrasos.setText(String.valueOf(e.getPrestamosRetrasados()));
        lblUsuarios.setText(String.valueOf(e.getUsuariosActivos()));
        lblSolicitudes.setText(String.valueOf(e.getSolicitudesPendientes()));
        lblSolicitudesPrestamo.setText(String.valueOf(e.getSolicitudesPrestamo()));
        lblMultas.setText(Formato.moneda(e.getMultasPendientes()));

        tblRetrasos.setItems(FXCollections.observableArrayList(prestamoService.retrasados()));

        lblEstado.setText("Datos actualizados: " + Formato.fechaHora(java.time.LocalDateTime.now()));
    }
}
