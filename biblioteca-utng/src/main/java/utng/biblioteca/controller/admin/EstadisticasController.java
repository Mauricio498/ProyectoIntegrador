package utng.biblioteca.controller.admin;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import utng.biblioteca.util.ReporteExcel;
import java.io.File;
import java.io.IOException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import utng.biblioteca.dto.Conteo;
import utng.biblioteca.dto.Periodo;
import utng.biblioteca.dto.PuntoSerie;
import utng.biblioteca.dto.FilaInventario;
import utng.biblioteca.dto.ResumenAcervo;
import utng.biblioteca.dto.ResumenRango;
import utng.biblioteca.service.EstadisticaService;
import utng.biblioteca.util.Formato;
import utng.biblioteca.util.Paginador;

import java.time.LocalDate;
import java.util.List;

/**
 * Módulo de reportes.
 *
 * Todo se recalcula contra el rango de fechas y el agrupamiento elegidos.
 * El agrupamiento por día, semana o mes lo hace SQL Server: aquí solo se
 * rotula el eje y se pintan las series.
 */
public class EstadisticasController {

    // ---- Filtros
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private ComboBox<Periodo> cmbPeriodo;
    @FXML private ComboBox<String> cmbAtajo;

    // ---- Resumen
    @FXML private Label lblAccesos;
    @FXML private Label lblUsuarios;
    @FXML private Label lblBusquedas;
    @FXML private Label lblPrestamos;
    @FXML private Label lblDevoluciones;
    @FXML private Label lblCobrado;
    @FXML private Label lblEstado;

    // ---- Gráficas
    @FXML private LineChart<String, Number> chartAccesos;
    @FXML private CategoryAxis ejeAccesosX;
    @FXML private BarChart<String, Number> chartCirculacion;
    @FXML private CategoryAxis ejeCirculacionX;
    @FXML private BarChart<String, Number> chartTerminos;
    @FXML private CategoryAxis ejeTerminosX;
    @FXML private PieChart chartRol;
    @FXML private PieChart chartArea;
    @FXML private Label lblAreaAviso;

    // ---- Tablas
    @FXML private TableView<Conteo> tblTerminos;
    @FXML private TableColumn<Conteo, String> colTermino;
    @FXML private TableColumn<Conteo, String> colTerminoCantidad;
    @FXML private TableColumn<Conteo, String> colTerminoDetalle;

    @FXML private TableView<Conteo> tblMateriales;
    @FXML private TableColumn<Conteo, String> colMaterial;
    @FXML private TableColumn<Conteo, String> colMaterialAutores;
    @FXML private TableColumn<Conteo, String> colMaterialCantidad;

    // ---- Pestaña de acervo
    @FXML private Label lblAcMateriales;
    @FXML private Label lblAcEjemplares;
    @FXML private Label lblAcDisponibles;
    @FXML private Label lblAcPromedio;
    @FXML private Label lblAcSinEjemplar;
    @FXML private Label lblAcCatalogos;

    @FXML private BarChart<String, Number> chartAcTipo;
    @FXML private CategoryAxis ejeAcTipoX;
    @FXML private PieChart chartAcEstado;
    @FXML private BarChart<String, Number> chartAcClasificacion;
    @FXML private CategoryAxis ejeAcClasifX;

    @FXML private TableView<Conteo> tblAcEditorial;
    @FXML private TableColumn<Conteo, String> colEditorialNombre;
    @FXML private TableColumn<Conteo, String> colEditorialTitulos;

    @FXML private CheckBox chkSoloProblemas;
    @FXML private Label lblInventario;
    @FXML private TableView<FilaInventario> tblInventario;
    @FXML private TableColumn<FilaInventario, String> colInvTitulo;
    @FXML private TableColumn<FilaInventario, String> colInvClasif;
    @FXML private TableColumn<FilaInventario, String> colInvTipo;
    @FXML private TableColumn<FilaInventario, String> colInvEditorial;
    @FXML private TableColumn<FilaInventario, String> colInvAnio;
    @FXML private TableColumn<FilaInventario, String> colInvDisponibles;
    @FXML private TableColumn<FilaInventario, String> colInvPrestamos;
    @FXML private TableColumn<FilaInventario, String> colInvObservacion;

    @FXML private ComboBox<Integer> cmbInvPorPagina;
    @FXML private Label lblInvPagina;
    @FXML private Button btnInvAnterior;
    @FXML private Button btnInvSiguiente;

    private final EstadisticaService estadisticaService = new EstadisticaService();

    private Paginador<FilaInventario> paginadorInventario;

    /** Evita recargar seis consultas mientras se aplica un atajo de fechas. */
    private boolean aplicandoAtajo;

    @FXML
    public void initialize() {
        configurarFiltros();
        configurarTablas();
        configurarTablasAcervo();

        paginadorInventario = new Paginador<>(tblInventario, cmbInvPorPagina,
                                              lblInvPagina, btnInvAnterior, btnInvSiguiente);

        cargar();
        cargarAcervo();
    }

    private void configurarFiltros() {
        cmbPeriodo.getItems().setAll(Periodo.values());
        cmbPeriodo.getSelectionModel().select(Periodo.DIA);

        cmbAtajo.getItems().setAll(
                "Últimos 7 días", "Últimos 30 días", "Últimos 3 meses",
                "Este año", "Personalizado");
        cmbAtajo.getSelectionModel().select("Últimos 30 días");

        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().minusDays(30));

        // Elegir fechas a mano cambia el atajo a "Personalizado".
        dpDesde.valueProperty().addListener((obs, a, b) -> alCambiarFecha());
        dpHasta.valueProperty().addListener((obs, a, b) -> alCambiarFecha());

        cmbPeriodo.setOnAction(e -> cargar());
        cmbAtajo.setOnAction(e -> aplicarAtajo());
    }

    private void alCambiarFecha() {
        if (aplicandoAtajo) {
            return;
        }
        if (!"Personalizado".equals(cmbAtajo.getValue())) {
            aplicandoAtajo = true;
            cmbAtajo.getSelectionModel().select("Personalizado");
            aplicandoAtajo = false;
        }
        cargar();
    }

    private void aplicarAtajo() {
        String atajo = cmbAtajo.getValue();
        if (atajo == null || "Personalizado".equals(atajo)) {
            return;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate inicio = switch (atajo) {
            case "Últimos 7 días"   -> hoy.minusDays(7);
            case "Últimos 3 meses"  -> hoy.minusMonths(3);
            case "Este año"         -> hoy.withDayOfYear(1);
            default                 -> hoy.minusDays(30);
        };

        // El agrupamiento se ajusta solo al tamaño del rango, para que la
        // gráfica no quede con cientos de columnas ilegibles.
        Periodo sugerido = switch (atajo) {
            case "Últimos 3 meses" -> Periodo.SEMANA;
            case "Este año"        -> Periodo.MES;
            default                -> Periodo.DIA;
        };

        aplicandoAtajo = true;
        dpDesde.setValue(inicio);
        dpHasta.setValue(hoy);
        cmbPeriodo.getSelectionModel().select(sugerido);
        aplicandoAtajo = false;

        cargar();
    }

    private void configurarTablas() {
        colTermino.setCellValueFactory(d -> texto(d.getValue().getEtiqueta()));
        colTerminoCantidad.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));
        colTerminoDetalle.setCellValueFactory(d -> texto(d.getValue().getDetalle()));

        colMaterial.setCellValueFactory(d -> texto(d.getValue().getEtiqueta()));
        colMaterialAutores.setCellValueFactory(d -> texto(d.getValue().getDetalle()));
        colMaterialCantidad.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));

        tblTerminos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblMateriales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblTerminos.setPlaceholder(new Label("Sin búsquedas registradas en el periodo."));
        tblMateriales.setPlaceholder(new Label("Sin préstamos registrados en el periodo."));
    }

    @FXML
    private void cargar() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        Periodo periodo = cmbPeriodo.getValue();

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            lblEstado.setText("La fecha inicial es posterior a la final; se invirtió el rango.");
        } else {
            lblEstado.setText("Periodo: " + Formato.fecha(desde) + " al " + Formato.fecha(hasta));
        }

        cargarResumen(desde, hasta);
        cargarAccesos(desde, hasta, periodo);
        cargarCirculacion(desde, hasta, periodo);
        cargarTerminos(desde, hasta);
        cargarMateriales(desde, hasta);
        cargarDistribuciones(desde, hasta);
    }

    @FXML
    private void exportarExcel() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Guardar reporte de Excel");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivo Excel (*.xlsx)",
                        "*.xlsx"
                )
        );

        fileChooser.setInitialFileName(
                "Reporte_Biblioteca.xlsx"
        );

        Window ventana = dpDesde.getScene().getWindow();

        File archivo = fileChooser.showSaveDialog(ventana);

        if (archivo == null) {
            return;
        }

        try {

            ReporteExcel reporte = new ReporteExcel();

            reporte.generar(
                    archivo,
                    dpDesde.getValue(),
                    dpHasta.getValue(),
                    cmbPeriodo.getValue(),
                    estadisticaService
            );

            lblEstado.setText(
                    "Reporte de Excel generado correctamente."
            );

        } catch (IOException e) {

            e.printStackTrace();

            lblEstado.setText(
                    "Error al generar el reporte de Excel."
            );
        }
    }

    private void cargarResumen(LocalDate desde, LocalDate hasta) {
        ResumenRango r = estadisticaService.resumen(desde, hasta);

        lblAccesos.setText(String.valueOf(r.getAccesos()));
        lblUsuarios.setText(String.valueOf(r.getUsuariosDistintos()));
        lblBusquedas.setText(String.valueOf(r.getBusquedas()));
        lblPrestamos.setText(String.valueOf(r.getPrestamos()));
        lblDevoluciones.setText(String.valueOf(r.getDevoluciones()));
        lblCobrado.setText(Formato.moneda(r.getMontoCobrado()));
    }

    private void cargarAccesos(LocalDate desde, LocalDate hasta, Periodo periodo) {
        List<PuntoSerie> puntos = estadisticaService.accesos(desde, hasta, periodo);

        XYChart.Series<String, Number> exitosos = new XYChart.Series<>();
        exitosos.setName("Accesos correctos");

        XYChart.Series<String, Number> fallidos = new XYChart.Series<>();
        fallidos.setName("Intentos fallidos");

        for (PuntoSerie p : puntos) {
            String etiqueta = periodo.formatear(p.getPeriodo());
            exitosos.getData().add(new XYChart.Data<>(etiqueta, p.getValor()));
            fallidos.getData().add(new XYChart.Data<>(etiqueta, p.getValorSecundario()));
        }

        chartAccesos.getData().setAll(List.of(exitosos, fallidos));
        ejeAccesosX.setTickLabelRotation(puntos.size() > 12 ? -45 : 0);
        instalarTooltips(exitosos, "accesos");
        instalarTooltips(fallidos, "intentos fallidos");
    }

    private void cargarCirculacion(LocalDate desde, LocalDate hasta, Periodo periodo) {
        List<PuntoSerie> puntos = estadisticaService.circulacion(desde, hasta, periodo);

        XYChart.Series<String, Number> prestamos = new XYChart.Series<>();
        prestamos.setName("Préstamos");

        XYChart.Series<String, Number> devoluciones = new XYChart.Series<>();
        devoluciones.setName("Devoluciones");

        for (PuntoSerie p : puntos) {
            String etiqueta = periodo.formatear(p.getPeriodo());
            prestamos.getData().add(new XYChart.Data<>(etiqueta, p.getValor()));
            devoluciones.getData().add(new XYChart.Data<>(etiqueta, p.getValorSecundario()));
        }

        chartCirculacion.getData().setAll(List.of(prestamos, devoluciones));
        ejeCirculacionX.setTickLabelRotation(puntos.size() > 12 ? -45 : 0);
        instalarTooltips(prestamos, "préstamos");
        instalarTooltips(devoluciones, "devoluciones");
    }

    private void cargarTerminos(LocalDate desde, LocalDate hasta) {
        List<Conteo> terminos = estadisticaService.terminosMasBuscados(desde, hasta, 10);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Veces buscado");

        for (Conteo c : terminos) {
            serie.getData().add(new XYChart.Data<>(c.getEtiquetaCorta(), c.getCantidad()));
        }

        chartTerminos.getData().setAll(List.of(serie));
        ejeTerminosX.setTickLabelRotation(terminos.size() > 5 ? -30 : 0);
        instalarTooltips(serie, "búsquedas");

        tblTerminos.setItems(FXCollections.observableArrayList(terminos));
    }

    private void cargarMateriales(LocalDate desde, LocalDate hasta) {
        tblMateriales.setItems(FXCollections.observableArrayList(
                estadisticaService.materialesMasPrestados(desde, hasta, 10)));
    }

    private void cargarDistribuciones(LocalDate desde, LocalDate hasta) {
        chartRol.setData(FXCollections.observableArrayList(
                estadisticaService.prestamosPorRol(desde, hasta).stream()
                        .map(c -> new PieChart.Data(
                                c.getEtiqueta() + " (" + c.getCantidad() + ")", c.getCantidad()))
                        .toList()));

        List<Conteo> areas = estadisticaService.prestamosPorArea(desde, hasta);

        // El área es opcional: si no se ejecutó la migración 05_area.sql,
        // se oculta la gráfica en vez de mostrar un recuadro vacío.
        boolean hayAreas = !areas.isEmpty();
        chartArea.setVisible(hayAreas);
        chartArea.setManaged(hayAreas);
        lblAreaAviso.setVisible(!hayAreas);
        lblAreaAviso.setManaged(!hayAreas);

        if (hayAreas) {
            chartArea.setData(FXCollections.observableArrayList(
                    areas.stream()
                         .map(c -> new PieChart.Data(
                                 c.getEtiqueta() + " (" + c.getCantidad() + ")", c.getCantidad()))
                         .toList()));
        }
    }

    /** Muestra el valor exacto al pasar el cursor sobre un punto o barra. */
    private void instalarTooltips(XYChart.Series<String, Number> serie, String unidad) {
        for (XYChart.Data<String, Number> dato : serie.getData()) {
            if (dato.getNode() != null) {
                Tooltip.install(dato.getNode(), new Tooltip(
                        dato.getXValue() + ": " + dato.getYValue() + " " + unidad));
            }
        }
    }

    @FXML
    private void restablecer() {
        cmbAtajo.getSelectionModel().select("Últimos 30 días");
        aplicarAtajo();
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor == null ? "" : valor);
    }

    // ================================================================ acervo

    private void configurarTablasAcervo() {
        colEditorialNombre.setCellValueFactory(d -> texto(d.getValue().getEtiqueta()));
        colEditorialTitulos.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));

        colInvTitulo.setCellValueFactory(d -> texto(d.getValue().getTitulo()));
        colInvClasif.setCellValueFactory(d -> texto(d.getValue().getClasificacion()));
        colInvTipo.setCellValueFactory(d -> texto(d.getValue().getTipo()));
        colInvEditorial.setCellValueFactory(d -> texto(d.getValue().getEditorial()));
        colInvAnio.setCellValueFactory(d -> texto(d.getValue().getAnioTexto()));
        colInvDisponibles.setCellValueFactory(d -> texto(d.getValue().getDisponibilidad()));
        colInvPrestamos.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getVecesPrestado())));
        colInvObservacion.setCellValueFactory(d -> texto(d.getValue().getObservacion()));

        tblAcEditorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblInventario.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblAcEditorial.setPlaceholder(new Label("Sin editoriales registradas."));
        tblInventario.setPlaceholder(new Label("Sin materiales que mostrar."));
    }

    /** Recarga toda la pestaña de acervo. */
    @FXML
    private void cargarAcervo() {
        ResumenAcervo r = estadisticaService.resumenAcervo();

        lblAcMateriales.setText(String.valueOf(r.getMaterialesActivos()));
        lblAcEjemplares.setText(String.valueOf(r.getEjemplaresVigentes()));
        lblAcDisponibles.setText(String.valueOf(r.getEjemplaresDisponibles()));
        lblAcPromedio.setText(String.valueOf(r.getEjemplaresPorMaterial()));
        lblAcSinEjemplar.setText(String.valueOf(r.getMaterialesSinEjemplar()));
        lblAcCatalogos.setText(r.getAutores() + " / " + r.getEditoriales());

        cargarGraficaTipo();
        cargarGraficaEstado();
        cargarGraficaClasificacion();

        tblAcEditorial.setItems(FXCollections.observableArrayList(
                estadisticaService.acervoPorEditorial(10)));

        cargarInventario();
    }

    private void cargarGraficaTipo() {
        List<Conteo> tipos = estadisticaService.acervoPorTipo();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Materiales");

        for (Conteo c : tipos) {
            XYChart.Data<String, Number> dato =
                    new XYChart.Data<>(c.getEtiquetaCorta(), c.getCantidad());
            serie.getData().add(dato);
        }

        chartAcTipo.getData().setAll(List.of(serie));
        ejeAcTipoX.setTickLabelRotation(0);

        // El detalle trae el conteo de ejemplares, que no cabe en la barra.
        for (int i = 0; i < serie.getData().size(); i++) {
            XYChart.Data<String, Number> dato = serie.getData().get(i);
            if (dato.getNode() != null && i < tipos.size()) {
                Tooltip.install(dato.getNode(), new Tooltip(
                        tipos.get(i).getEtiqueta() + ": " + tipos.get(i).getCantidad()
                      + " materiales (" + tipos.get(i).getDetalle() + ")"));
            }
        }
    }

    private void cargarGraficaEstado() {
        chartAcEstado.setData(FXCollections.observableArrayList(
                estadisticaService.acervoPorEstadoEjemplar().stream()
                        .map(c -> new PieChart.Data(
                                c.getEtiqueta() + " (" + c.getCantidad() + ")", c.getCantidad()))
                        .toList()));
    }

    private void cargarGraficaClasificacion() {
        List<Conteo> clasificaciones = estadisticaService.acervoPorClasificacion(12);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Títulos");

        for (Conteo c : clasificaciones) {
            serie.getData().add(new XYChart.Data<>(c.getEtiquetaCorta(), c.getCantidad()));
        }

        chartAcClasificacion.getData().setAll(List.of(serie));
        ejeAcClasifX.setTickLabelRotation(clasificaciones.size() > 6 ? -30 : 0);
        instalarTooltips(serie, "títulos");
    }

    @FXML
    private void cargarInventario() {
        boolean soloProblemas = chkSoloProblemas.isSelected();
        List<FilaInventario> filas = estadisticaService.inventario(soloProblemas);

        paginadorInventario.setDatos(filas);

        long conObservacion = filas.stream().filter(FilaInventario::tieneObservacion).count();

        lblInventario.setText(soloProblemas
                ? filas.size() + " material(es) por revisar"
                : filas.size() + " material(es)  ·  " + conObservacion + " con observación");
    }
}
