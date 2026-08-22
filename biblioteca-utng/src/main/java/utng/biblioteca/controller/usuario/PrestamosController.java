package utng.biblioteca.controller.usuario;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import utng.biblioteca.dto.PrestamoDetalle;
import utng.biblioteca.dto.SolicitudPrestamoDetalle;
import utng.biblioteca.service.PrestamoService;
import utng.biblioteca.session.Sesion;
import utng.biblioteca.util.Alertas;
import utng.biblioteca.util.Formato;
import utng.biblioteca.util.Tarjetas;

import java.util.List;

/** Historial de prestamos del usuario en sesion. */
public class PrestamosController {

    @FXML private Label lblResumen;
    @FXML private Label lblCupo;
    @FXML private Label lblAdeudo;
    @FXML private CheckBox chkSoloActivos;
    @FXML private VBox contenedorPrestamos;
    @FXML private VBox contenedorSolicitudes;

    private final PrestamoService prestamoService = new PrestamoService();

    @FXML
    public void initialize() {
        chkSoloActivos.setSelected(true);
        cargar();
    }

    @FXML
    private void cargar() {
        List<PrestamoDetalle> prestamos = prestamoService.misPrestamos(chkSoloActivos.isSelected());

        contenedorPrestamos.getChildren().clear();
        if (prestamos.isEmpty()) {
            Label vacio = new Label(chkSoloActivos.isSelected()
                    ? "No tienes materiales en tu poder ahora mismo."
                    : "Todavía no tienes préstamos registrados.");
            vacio.getStyleClass().add("libro-autor");
            contenedorPrestamos.getChildren().add(vacio);
        } else {
            prestamos.forEach(p -> contenedorPrestamos.getChildren().add(Tarjetas.prestamo(p)));
        }

        int activos = (int) prestamos.stream().filter(PrestamoDetalle::estaPendiente).count();
        var rol = Sesion.getUsuario() == null ? null : Sesion.getUsuario().getRol();
        int limite = rol == null ? 0 : rol.getMaxMateriales();
        String plazo = rol == null ? "" : rol.getPlazoTexto();

        lblResumen.setText(prestamos.size() + " registro(s) - " + activos + " sin devolver");
        lblCupo.setText("Límite de tu rol: " + limite + " materiales  ·  Plazo: " + plazo
                      + "  ·  Puedes llevar " + prestamoService.cupoDisponible() + " más");
        lblAdeudo.setText("Adeudo pendiente: " + Formato.moneda(prestamoService.miAdeudo()));

        cargarSolicitudes();
    }

    private void cargarSolicitudes() {
        List<SolicitudPrestamoDetalle> solicitudes = prestamoService.misSolicitudes();

        contenedorSolicitudes.getChildren().clear();
        if (solicitudes.isEmpty()) {
            Label vacio = new Label("No has enviado solicitudes de préstamo.");
            vacio.getStyleClass().add("libro-autor");
            contenedorSolicitudes.getChildren().add(vacio);
            return;
        }

        solicitudes.forEach(sol ->
                contenedorSolicitudes.getChildren().add(Tarjetas.solicitud(sol, this::cancelar)));
    }

    private void cancelar(SolicitudPrestamoDetalle solicitud) {
        boolean confirmado = Alertas.confirmar("Cancelar solicitud",
                "Se retirará tu solicitud de: " + solicitud.getTitulo());
        if (!confirmado) {
            return;
        }

        var resultado = prestamoService.cancelarSolicitud(solicitud.getIdSolicitudPrestamo());
        if (!resultado.isExito()) {
            Alertas.advertencia("No se pudo cancelar", resultado.getMensaje());
        }
        cargar();
    }
}
