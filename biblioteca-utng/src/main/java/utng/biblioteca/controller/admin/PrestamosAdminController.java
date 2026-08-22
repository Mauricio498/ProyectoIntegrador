package utng.biblioteca.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import utng.biblioteca.dto.PrestamoDetalle;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.dto.SolicitudPrestamoDetalle;
import utng.biblioteca.service.PrestamoService;
import utng.biblioteca.util.Alertas;
import utng.biblioteca.util.Formato;
import utng.biblioteca.util.Paginador;

import java.util.List;

/**
 * Modulo de circulacion del personal de biblioteca.
 *
 * Arriba la bandeja de solicitudes que esperan respuesta; abajo los ejemplares
 * que estan en la calle, con el boton de devolucion. Aprobar una solicitud es
 * lo que genera el prestamo: SP_ResponderSolicitudPrestamo toma un ejemplar
 * libre del material y delega en SP_RegistrarPrestamo.
 */
public class PrestamosAdminController {

    // ---- Solicitudes
    @FXML private CheckBox chkSoloPendientes;
    @FXML private Label lblSolicitudes;
    @FXML private TableView<SolicitudPrestamoDetalle> tblSolicitudes;
    @FXML private TableColumn<SolicitudPrestamoDetalle, String> colSolUsuario;
    @FXML private TableColumn<SolicitudPrestamoDetalle, String> colSolRol;
    @FXML private TableColumn<SolicitudPrestamoDetalle, String> colSolMaterial;
    @FXML private TableColumn<SolicitudPrestamoDetalle, String> colSolDisponibles;
    @FXML private TableColumn<SolicitudPrestamoDetalle, String> colSolCupo;
    @FXML private TableColumn<SolicitudPrestamoDetalle, String> colSolFecha;
    @FXML private TableColumn<SolicitudPrestamoDetalle, String> colSolEstado;
    @FXML private TableColumn<SolicitudPrestamoDetalle, String> colSolAviso;

    @FXML private ComboBox<Integer> cmbSolPorPagina;
    @FXML private Label lblSolPagina;
    @FXML private Button btnSolAnterior;
    @FXML private Button btnSolSiguiente;

    // ---- Prestamos vigentes
    @FXML private TextField txtBuscar;
    @FXML private CheckBox chkSoloSinDevolver;
    @FXML private Label lblPrestamos;
    @FXML private TableView<PrestamoDetalle> tblPrestamos;
    @FXML private TableColumn<PrestamoDetalle, String> colUsuario;
    @FXML private TableColumn<PrestamoDetalle, String> colMaterial;
    @FXML private TableColumn<PrestamoDetalle, String> colEjemplar;
    @FXML private TableColumn<PrestamoDetalle, String> colPrestado;
    @FXML private TableColumn<PrestamoDetalle, String> colLimite;
    @FXML private TableColumn<PrestamoDetalle, String> colRetraso;
    @FXML private TableColumn<PrestamoDetalle, String> colEstado;

    @FXML private ComboBox<Integer> cmbPresPorPagina;
    @FXML private Label lblPresPagina;
    @FXML private Button btnPresAnterior;
    @FXML private Button btnPresSiguiente;

    private final PrestamoService prestamoService = new PrestamoService();

    private Paginador<SolicitudPrestamoDetalle> paginadorSolicitudes;
    private Paginador<PrestamoDetalle> paginadorPrestamos;

    @FXML
    public void initialize() {
        configurarTablaSolicitudes();
        configurarTablaPrestamos();

        paginadorSolicitudes = new Paginador<>(tblSolicitudes, cmbSolPorPagina,
                                               lblSolPagina, btnSolAnterior, btnSolSiguiente);
        paginadorPrestamos = new Paginador<>(tblPrestamos, cmbPresPorPagina,
                                             lblPresPagina, btnPresAnterior, btnPresSiguiente);

        chkSoloPendientes.setSelected(true);
        chkSoloSinDevolver.setSelected(true);
        txtBuscar.setOnAction(e -> cargarPrestamos());

        cargarTodo();
    }

    private void configurarTablaSolicitudes() {
        colSolUsuario.setCellValueFactory(d -> texto(d.getValue().getUsuarioNombre()));
        colSolRol.setCellValueFactory(d -> texto(d.getValue().getUsuarioRol()));
        colSolMaterial.setCellValueFactory(d -> texto(d.getValue().getTitulo()));
        colSolDisponibles.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getEjemplaresDisponibles())));
        colSolCupo.setCellValueFactory(d -> texto(d.getValue().getCupoTexto()));
        colSolFecha.setCellValueFactory(d ->
                new SimpleStringProperty(Formato.fechaHora(d.getValue().getFechaSolicitud())));
        colSolEstado.setCellValueFactory(d -> texto(d.getValue().getEstado()));
        colSolAviso.setCellValueFactory(d -> texto(d.getValue().getAdvertencia()));

        tblSolicitudes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblSolicitudes.setPlaceholder(new Label("No hay solicitudes que mostrar."));
    }

    private void configurarTablaPrestamos() {
        colUsuario.setCellValueFactory(d -> texto(d.getValue().getUsuarioNombre()));
        colMaterial.setCellValueFactory(d -> texto(d.getValue().getTitulo()));
        colEjemplar.setCellValueFactory(d -> texto(d.getValue().getCodigoEjemplar()));
        colPrestado.setCellValueFactory(d ->
                new SimpleStringProperty(Formato.fechaHora(d.getValue().getFechaPrestamo())));
        colLimite.setCellValueFactory(d ->
                new SimpleStringProperty(Formato.fecha(d.getValue().getFechaLimite())));
        colRetraso.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDiasRetraso() > 0
                        ? d.getValue().getDiasRetraso() + " día(s)" : "-"));
        colEstado.setCellValueFactory(d -> texto(d.getValue().getEtiquetaEstado()));

        tblPrestamos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblPrestamos.setPlaceholder(new Label("Sin préstamos que coincidan con el filtro."));
    }

    @FXML
    private void cargarTodo() {
        cargarSolicitudes();
        cargarPrestamos();
    }

    @FXML
    private void cargarSolicitudes() {
        String estado = chkSoloPendientes.isSelected() ? "Pendiente" : null;
        List<SolicitudPrestamoDetalle> solicitudes = prestamoService.bandejaSolicitudes(estado);

        paginadorSolicitudes.setDatos(solicitudes);

        // Se nombra el filtro aplicado: si todas las solicitudes están
        // pendientes, marcar la casilla no cambia la lista y sin este texto
        // parece que el filtro no hizo nada.
        lblSolicitudes.setText(solicitudes.size()
                + (chkSoloPendientes.isSelected()
                        ? " solicitud(es) pendiente(s)"
                        : " solicitud(es) en total"));
    }

    @FXML
    private void cargarPrestamos() {
        List<PrestamoDetalle> prestamos =
                prestamoService.listarTodos(txtBuscar.getText(), chkSoloSinDevolver.isSelected());

        paginadorPrestamos.setDatos(prestamos);

        long retrasados = prestamos.stream().filter(p -> p.getDiasRetraso() > 0).count();

        lblPrestamos.setText(prestamos.size()
                + (chkSoloSinDevolver.isSelected() ? " sin devolver" : " en total")
                + "  ·  " + retrasados + " con retraso");
    }

    @FXML
    private void aprobar() {
        SolicitudPrestamoDetalle solicitud = seleccionPendiente();
        if (solicitud == null) {
            return;
        }

        if (solicitud.getEjemplaresDisponibles() == 0) {
            Alertas.advertencia("Sin ejemplares",
                    "No quedá ningún ejemplar libre de \"" + solicitud.getTitulo() + "\".");
            return;
        }

        String aviso = solicitud.getAdvertencia();
        String mensaje = "Se generará el préstamo de \"" + solicitud.getTitulo() + "\" para "
                       + solicitud.getUsuarioNombre() + ".";
        if (!aviso.isBlank()) {
            mensaje += System.lineSeparator() + System.lineSeparator() + "Atención: " + aviso + ".";
        }

        if (!Alertas.confirmar("Aprobar solicitud", mensaje)) {
            return;
        }

        Resultado resultado = prestamoService.aprobarSolicitud(
                solicitud.getIdSolicitudPrestamo(), "Aprobada en ventanilla");

        avisar(resultado);
        cargarTodo();
    }

    @FXML
    private void rechazar() {
        SolicitudPrestamoDetalle solicitud = seleccionPendiente();
        if (solicitud == null) {
            return;
        }

        Alertas.pedirTexto("Rechazar solicitud", "Motivo del rechazo:", "")
               .ifPresent(motivo -> {
                   Resultado resultado = prestamoService.rechazarSolicitud(
                           solicitud.getIdSolicitudPrestamo(),
                           motivo.isBlank() ? "Sin motivo especificado" : motivo);
                   avisar(resultado);
                   cargarTodo();
               });
    }

    @FXML
    private void devolver() {
        registrarCierre(false);
    }

    @FXML
    private void marcarExtraviado() {
        registrarCierre(true);
    }

    private void registrarCierre(boolean perdido) {
        PrestamoDetalle detalle = tblPrestamos.getSelectionModel().getSelectedItem();

        if (detalle == null) {
            Alertas.advertencia("Devolución", "Selecciona un préstamo de la tabla.");
            return;
        }
        if (!detalle.estaPendiente()) {
            Alertas.advertencia("Devolución", "Ese ejemplar ya fue cerrado.");
            return;
        }

        String texto = perdido
                ? "Se registrará el ejemplar " + detalle.getCodigoEjemplar()
                  + " como extraviado y se generará la multa correspondiente."
                : "Se registrará la devolución de " + detalle.getCodigoEjemplar()
                  + (detalle.getDiasRetraso() > 0
                        ? System.lineSeparator() + System.lineSeparator()
                          + "Lleva " + detalle.getDiasRetraso()
                          + " día(s) de retraso, se generará una multa."
                        : "");

        if (!Alertas.confirmar(perdido ? "Marcar extraviado" : "Registrar devolución", texto)) {
            return;
        }

        Resultado resultado = prestamoService.devolver(detalle.getIdDetallePrestamo(), perdido);
        avisar(resultado);
        cargarTodo();
    }

    private SolicitudPrestamoDetalle seleccionPendiente() {
        SolicitudPrestamoDetalle solicitud = tblSolicitudes.getSelectionModel().getSelectedItem();

        if (solicitud == null) {
            Alertas.advertencia("Solicitudes", "Selecciona una solicitud de la tabla.");
            return null;
        }
        if (!solicitud.estaPendiente()) {
            Alertas.advertencia("Solicitudes",
                    "Esa solicitud ya fue atendida (" + solicitud.getEstado() + ").");
            return null;
        }
        return solicitud;
    }

    private void avisar(Resultado resultado) {
        if (resultado.isExito()) {
            Alertas.info("Listo", resultado.getMensaje());
        } else {
            Alertas.advertencia("No se pudo completar", resultado.getMensaje());
        }
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor == null ? "" : valor);
    }
}
