package utng.biblioteca.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.SolicitudAdquisicion;
import utng.biblioteca.service.SolicitudService;
import utng.biblioteca.util.Alertas;
import utng.biblioteca.util.Formato;
import utng.biblioteca.util.Paginador;

import java.util.List;

/**
 * Bandeja de solicitudes de adquisición: los títulos que la comunidad pide
 * que la biblioteca compre.
 *
 * Aprobar aquí no da de alta el material; deja constancia de la decisión.
 * El alta se hace después en el módulo de Acervo, cuando el libro llega.
 */
public class SolicitudesController {

    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblResumen;

    // ---- Paginación
    @FXML private ComboBox<Integer> cmbPorPagina;
    @FXML private Label lblPagina;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;

    @FXML private TableView<SolicitudAdquisicion> tblSolicitudes;
    @FXML private TableColumn<SolicitudAdquisicion, String> colSolicitante;
    @FXML private TableColumn<SolicitudAdquisicion, String> colTitulo;
    @FXML private TableColumn<SolicitudAdquisicion, String> colAutor;
    @FXML private TableColumn<SolicitudAdquisicion, String> colEditorial;
    @FXML private TableColumn<SolicitudAdquisicion, String> colIsbn;
    @FXML private TableColumn<SolicitudAdquisicion, String> colFecha;
    @FXML private TableColumn<SolicitudAdquisicion, String> colEstado;

    @FXML private Label lblDetalleTitulo;
    @FXML private Label lblDetalleSolicitante;
    @FXML private TextArea txtDescripcion;
    @FXML private TextArea txtRespuesta;

    private final SolicitudService solicitudService = new SolicitudService();

    private Paginador<SolicitudAdquisicion> paginador;

    @FXML
    public void initialize() {
        configurarTabla();

        paginador = new Paginador<>(tblSolicitudes, cmbPorPagina, lblPagina,
                                    btnAnterior, btnSiguiente);

        cmbEstado.getItems().setAll("Pendientes", "Aprobadas", "Rechazadas", "Todas");
        cmbEstado.getSelectionModel().selectFirst();
        cmbEstado.setOnAction(e -> cargar());

        tblSolicitudes.getSelectionModel().selectedItemProperty()
                      .addListener((obs, anterior, actual) -> mostrarDetalle(actual));

        cargar();
    }

    private void configurarTabla() {
        colSolicitante.setCellValueFactory(d -> texto(
                d.getValue().getUsuario() == null ? "" : d.getValue().getUsuario().getNombre()));
        colTitulo.setCellValueFactory(d -> texto(d.getValue().getTitulo()));
        colAutor.setCellValueFactory(d -> texto(d.getValue().getAutor()));
        colEditorial.setCellValueFactory(d -> texto(d.getValue().getEditorial()));
        colIsbn.setCellValueFactory(d -> texto(d.getValue().getIsbn()));
        colFecha.setCellValueFactory(d ->
                new SimpleStringProperty(Formato.fechaHora(d.getValue().getFechaSolicitud())));
        colEstado.setCellValueFactory(d -> texto(d.getValue().getEstado()));

        tblSolicitudes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblSolicitudes.setPlaceholder(new Label("No hay solicitudes que mostrar."));
    }

    @FXML
    private void cargar() {
        String estado = switch (cmbEstado.getValue() == null ? "" : cmbEstado.getValue()) {
            case "Pendientes" -> "Pendiente";
            case "Aprobadas"  -> "Aprobada";
            case "Rechazadas" -> "Rechazada";
            default           -> null;
        };

        List<SolicitudAdquisicion> solicitudes = solicitudService.bandeja(estado);

        paginador.setDatos(solicitudes);
        lblResumen.setText(solicitudes.size() + " solicitud(es)");

        limpiarDetalle();
    }

    private void mostrarDetalle(SolicitudAdquisicion solicitud) {
        if (solicitud == null) {
            limpiarDetalle();
            return;
        }

        lblDetalleTitulo.setText(solicitud.getTitulo());

        String solicitante = solicitud.getUsuario() == null
                ? "Desconocido" : solicitud.getUsuario().getNombre();

        StringBuilder pie = new StringBuilder("Solicitado por ").append(solicitante)
                .append(" el ").append(Formato.fechaHora(solicitud.getFechaSolicitud()));

        if (solicitud.getFechaRespuesta() != null) {
            pie.append("   |   Respondida el ")
               .append(Formato.fechaHora(solicitud.getFechaRespuesta()));

            if (solicitud.getAdministrador() != null) {
                pie.append(" por ").append(solicitud.getAdministrador().getNombre());
            }
        }

        lblDetalleSolicitante.setText(pie.toString());
        txtDescripcion.setText(solicitud.getDescripcion() == null
                ? "(Sin justificación capturada)" : solicitud.getDescripcion());
        txtRespuesta.setText(solicitud.getRespuesta() == null ? "" : solicitud.getRespuesta());

        // Una solicitud ya atendida se consulta, no se vuelve a responder.
        txtRespuesta.setEditable("Pendiente".equalsIgnoreCase(solicitud.getEstado()));
    }

    private void limpiarDetalle() {
        lblDetalleTitulo.setText("Selecciona una solicitud");
        lblDetalleSolicitante.setText("");
        txtDescripcion.clear();
        txtRespuesta.clear();
    }

    @FXML
    private void aprobar() {
        responder(true);
    }

    @FXML
    private void rechazar() {
        responder(false);
    }

    private void responder(boolean aprobar) {
        SolicitudAdquisicion solicitud = tblSolicitudes.getSelectionModel().getSelectedItem();

        if (solicitud == null) {
            Alertas.advertencia("Solicitudes", "Selecciona una solicitud de la tabla.");
            return;
        }

        if (!"Pendiente".equalsIgnoreCase(solicitud.getEstado())) {
            Alertas.advertencia("Solicitudes",
                    "Esa solicitud ya fue atendida (" + solicitud.getEstado() + ").");
            return;
        }

        String respuesta = txtRespuesta.getText();

        if (!aprobar && (respuesta == null || respuesta.isBlank())) {
            Alertas.advertencia("Rechazar",
                    "Escribe el motivo del rechazo para que el solicitante sepa por qué.");
            txtRespuesta.requestFocus();
            return;
        }

        String mensaje = aprobar
                ? "Se marcará como aprobada la compra de \"" + solicitud.getTitulo() + "\".\n\n"
                  + "Recuerda que el alta del material se hace en el módulo de Acervo "
                  + "cuando el ejemplar llegue físicamente."
                : "Se rechazará la solicitud de \"" + solicitud.getTitulo() + "\".";

        if (!Alertas.confirmar(aprobar ? "Aprobar solicitud" : "Rechazar solicitud", mensaje)) {
            return;
        }

        Resultado resultado = aprobar
                ? solicitudService.aprobar(solicitud.getIdSolicitud(),
                        respuesta == null || respuesta.isBlank()
                                ? "Aprobada para adquisición" : respuesta)
                : solicitudService.rechazar(solicitud.getIdSolicitud(), respuesta);

        if (resultado.isExito()) {
            Alertas.info("Listo", resultado.getMensaje());
        } else {
            Alertas.advertencia("No se pudo completar", resultado.getMensaje());
        }

        cargar();
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor == null ? "" : valor);
    }
}
