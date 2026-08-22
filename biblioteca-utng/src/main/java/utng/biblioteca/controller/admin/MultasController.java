package utng.biblioteca.controller.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.service.MultaService;
import utng.biblioteca.session.Sesion;
import utng.biblioteca.util.Alertas;
import utng.biblioteca.util.Formato;
import utng.biblioteca.util.Paginador;

import java.math.BigDecimal;
import java.util.List;

/**
 * Multas y pagos.
 *
 * Cada fila viene del DAO como un arreglo plano
 * [idMulta, usuario, titulo, ejemplar, diasRetraso, monto, estado], que es
 * justo lo que la tabla necesita mostrar sin cargar el objeto completo.
 *
 * El pago se registra con SP_RegistrarPago: el trigger del esquema impide
 * pagar de mas y el procedimiento marca la multa como Pagada al saldarla.
 */
public class MultasController {

    @FXML private CheckBox chkSoloPendientes;
    @FXML private Label lblResumen;
    @FXML private Label lblTotal;

    // ---- Paginación
    @FXML private ComboBox<Integer> cmbPorPagina;
    @FXML private Label lblPagina;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;

    @FXML private TableView<String[]> tblMultas;
    @FXML private TableColumn<String[], String> colId;
    @FXML private TableColumn<String[], String> colUsuario;
    @FXML private TableColumn<String[], String> colMaterial;
    @FXML private TableColumn<String[], String> colEjemplar;
    @FXML private TableColumn<String[], String> colDias;
    @FXML private TableColumn<String[], String> colMonto;
    @FXML private TableColumn<String[], String> colEstado;

    private final MultaService multaService = new MultaService();

    private Paginador<String[]> paginador;

    @FXML
    public void initialize() {
        configurarTabla();

        paginador = new Paginador<>(tblMultas, cmbPorPagina, lblPagina,
                                    btnAnterior, btnSiguiente);

        chkSoloPendientes.setSelected(true);
        cargar();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(d -> columna(d.getValue(), 0));
        colUsuario.setCellValueFactory(d -> columna(d.getValue(), 1));
        colMaterial.setCellValueFactory(d -> columna(d.getValue(), 2));
        colEjemplar.setCellValueFactory(d -> columna(d.getValue(), 3));
        colDias.setCellValueFactory(d -> columna(d.getValue(), 4));
        colMonto.setCellValueFactory(d ->
                new SimpleStringProperty(Formato.moneda(aMonto(d.getValue()[5]))));
        colEstado.setCellValueFactory(d -> columna(d.getValue(), 6));

        tblMultas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblMultas.setPlaceholder(new Label("No hay multas que mostrar."));
    }

    @FXML
    private void cargar() {
        List<String[]> multas = multaService.listarParaGestion(chkSoloPendientes.isSelected());

        paginador.setDatos(multas);

        BigDecimal total = multas.stream()
                .filter(f -> "Pendiente".equals(f[6]))
                .map(f -> aMonto(f[5]))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        lblResumen.setText(multas.size()
                + (chkSoloPendientes.isSelected() ? " multa(s) pendiente(s)" : " multa(s) en total"));
        lblTotal.setText("Por cobrar: " + Formato.moneda(total));
    }

    @FXML
    private void registrarPago() {
        String[] fila = seleccionPendiente();
        if (fila == null) {
            return;
        }

        BigDecimal monto = aMonto(fila[5]);

        Alertas.pedirTexto("Registrar pago",
                        "Multa de " + fila[1] + " por " + Formato.moneda(monto)
                      + System.lineSeparator() + "Monto que se recibe:",
                        monto.toPlainString())
               .ifPresent(texto -> {
                   BigDecimal recibido;
                   try {
                       recibido = new BigDecimal(texto.trim().replace(",", ""));
                   } catch (NumberFormatException e) {
                       Alertas.advertencia("Monto invalido", "Escribe una cantidad, por ejemplo 50.00");
                       return;
                   }

                   Resultado resultado =
                           multaService.registrarPago(Integer.parseInt(fila[0]), recibido);
                   avisar(resultado);
                   cargar();
               });
    }

    @FXML
    private void condonar() {
        String[] fila = seleccionPendiente();
        if (fila == null) {
            return;
        }

        if (!Sesion.esSuperAdministrador()) {
            Alertas.advertencia("Condonar",
                    "Solo el superadministrador puede condonar una multa.");
            return;
        }

        if (!Alertas.confirmar("Condonar multa",
                "Se cancelará la multa de " + fila[1] + " por " + Formato.moneda(aMonto(fila[5]))
              + ". Esta acción no se puede deshacer.")) {
            return;
        }

        Resultado resultado = multaService.condonar(Integer.parseInt(fila[0]));
        avisar(resultado);
        cargar();
    }

    private String[] seleccionPendiente() {
        String[] fila = tblMultas.getSelectionModel().getSelectedItem();

        if (fila == null) {
            Alertas.advertencia("Multas", "Selecciona una multa de la tabla.");
            return null;
        }
        if (!"Pendiente".equals(fila[6])) {
            Alertas.advertencia("Multas", "Esa multa ya está " + fila[6].toLowerCase() + ".");
            return null;
        }
        return fila;
    }

    private void avisar(Resultado resultado) {
        if (resultado.isExito()) {
            Alertas.info("Listo", resultado.getMensaje());
        } else {
            Alertas.advertencia("No se pudo completar", resultado.getMensaje());
        }
    }

    private static BigDecimal aMonto(String valor) {
        try {
            return new BigDecimal(valor);
        } catch (RuntimeException e) {
            return BigDecimal.ZERO;
        }
    }

    private static SimpleStringProperty columna(String[] fila, int indice) {
        return new SimpleStringProperty(
                fila != null && indice < fila.length && fila[indice] != null ? fila[indice] : "");
    }
}
