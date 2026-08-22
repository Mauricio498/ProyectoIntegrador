package utng.biblioteca.controller.admin;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import utng.biblioteca.dto.RegistroImportacion;
import utng.biblioteca.dto.ResultadoImportacion;
import utng.biblioteca.util.ImportadorExcel;

import java.io.File;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import utng.biblioteca.dto.MaterialCatalogo;
import utng.biblioteca.dto.Resultado;
import utng.biblioteca.model.Autor;
import utng.biblioteca.model.Biblioteca;
import utng.biblioteca.model.Editorial;
import utng.biblioteca.model.Ejemplar;
import utng.biblioteca.model.Material;
import utng.biblioteca.model.TipoMaterial;
import utng.biblioteca.service.CatalogoService;
import utng.biblioteca.util.Alertas;
import utng.biblioteca.util.Formato;
import utng.biblioteca.util.Paginador;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestión del acervo: alta, edición y baja de materiales, y administración
 * de los ejemplares físicos de cada uno.
 *
 * La distinción importante es que el MATERIAL es la obra (un título, un ISBN)
 * y el EJEMPLAR es la copia física que se presta. Dar de baja un material no
 * borra su historial de préstamos: solo lo saca del catálogo.
 */
public class MaterialesController {

    // ---- Filtros y tabla de materiales
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<TipoMaterial> cmbTipo;
    @FXML private ComboBox<String> cmbDisponibilidad;
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

    @FXML private TableView<MaterialCatalogo> tblMateriales;
    @FXML private TableColumn<MaterialCatalogo, String> colIsbn;
    @FXML private TableColumn<MaterialCatalogo, String> colTitulo;
    @FXML private TableColumn<MaterialCatalogo, String> colAutores;
    @FXML private TableColumn<MaterialCatalogo, String> colTipo;
    @FXML private TableColumn<MaterialCatalogo, String> colEditorial;
    @FXML private TableColumn<MaterialCatalogo, String> colEjemplares;
    @FXML private TableColumn<MaterialCatalogo, String> colEstado;

    // ---- Formulario del material
    @FXML private Label lblFormulario;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtIsbn;
    @FXML private TextField txtAnio;
    @FXML private TextField txtClasificacion;
    @FXML private ComboBox<TipoMaterial> cmbTipoForm;
    @FXML private ComboBox<Editorial> cmbEditorial;
    @FXML private ListView<Autor> lstAutores;
    @FXML private Button btnBaja;

    // ---- Ejemplares
    @FXML private Label lblEjemplares;
    @FXML private TableView<Ejemplar> tblEjemplares;
    @FXML private TableColumn<Ejemplar, String> colCodigo;
    @FXML private TableColumn<Ejemplar, String> colBiblioteca;
    @FXML private TableColumn<Ejemplar, String> colAdquisicion;
    @FXML private TableColumn<Ejemplar, String> colIngreso;
    @FXML private TableColumn<Ejemplar, String> colEstadoEjemplar;

    @FXML private TextField txtCodigoEjemplar;
    @FXML private TextField txtNumeroAdquisicion;
    @FXML private ComboBox<Biblioteca> cmbBiblioteca;
    @FXML private DatePicker dpIngreso;

    private final CatalogoService catalogoService = new CatalogoService();

    private Paginador<MaterialCatalogo> paginador;

    /** Proporción del divisor cuando el formulario está visible. */
    private static final double DIVISOR_ABIERTO = 0.68;

    /** Material que se está editando; null cuando el formulario es un alta. */
    private MaterialCatalogo enEdicion;

    @FXML
    private void importarExcel() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Seleccionar archivo de Excel");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivos Excel (*.xlsx)",
                        "*.xlsx"
                )
        );

        Window ventana =
                txtBuscar.getScene().getWindow();

        File archivo =
                fileChooser.showOpenDialog(ventana);

        if (archivo == null) {
            return;
        }

        try {

            // ============================================================
            // 1. LEER Y VALIDAR EXCEL
            // ============================================================

            ImportadorExcel importador =
                    new ImportadorExcel();

            List<RegistroImportacion> registros =
                    importador.leer(archivo);

            if (registros.isEmpty()) {

                mostrarAlerta(
                        "Importación",
                        "El archivo no contiene registros."
                );

                return;
            }

            // ============================================================
            // 2. MOSTRAR RESUMEN ANTES DE INSERTAR
            // ============================================================

            long libros = registros.stream()
                    .filter(r -> "Libro".equals(
                            r.getTipoDetectado()))
                    .count();

            long tesis = registros.stream()
                    .filter(r -> "Tesis".equals(
                            r.getTipoDetectado()))
                    .count();

            long consulta = registros.stream()
                    .filter(r -> "Consulta".equals(
                            r.getTipoDetectado()))
                    .count();

            long otros = registros.stream()
                    .filter(r -> "Otros".equals(
                            r.getTipoDetectado()))
                    .count();

            String mensaje =
                    "Se encontraron "
                    + registros.size()
                    + " registros.\n\n"

                    + "Libro: "
                    + libros
                    + "\n"

                    + "Tesis: "
                    + tesis
                    + "\n"

                    + "Consulta: "
                    + consulta
                    + "\n"

                    + "Otros: "
                    + otros
                    + "\n\n"

                    + "¿Deseas continuar con la importación?";

            boolean continuar =
                    confirmar(
                            "Importar Excel",
                            mensaje
                    );

            if (!continuar) {
                return;
            }

            // ============================================================
            // 3. INSERTAR EN SQL SERVER
            // ============================================================

            ResultadoImportacion resultado =
                    catalogoService.importarExcel(
                            registros
                    );

            // ============================================================
            // 4. ACTUALIZAR LA VISTA
            // ============================================================

            buscar();

            mostrarAlerta(
                    "Importación completada",
                    "La importación se realizó correctamente.\n\n"

                    + "Filas procesadas: "
                    + resultado.getFilasProcesadas()
                    + "\n"

                    + "Materiales nuevos: "
                    + resultado.getMaterialesCreados()
                    + "\n"

                    + "Materiales reutilizados: "
                    + resultado.getMaterialesReutilizados()
                    + "\n"

                    + "Ejemplares creados: "
                    + resultado.getEjemplaresCreados()
            );

        } catch (Exception e) {

            e.printStackTrace();

            mostrarAlerta(
                    "Error en la importación",
                    "No se pudo importar el archivo.\n\n"
                    + e.getMessage()
                    + "\n\n"
                    + "No se realizaron cambios en la base de datos."
            );
        }
    }
    private void mostrarAlerta(
        String titulo,
        String mensaje) {

    Alert alerta = new Alert(
            Alert.AlertType.INFORMATION
    );

    alerta.setTitle(titulo);
    alerta.setHeaderText(null);
    alerta.setContentText(mensaje);

    alerta.showAndWait();
}

private boolean confirmar(
        String titulo,
        String mensaje) {

    Alert alerta = new Alert(
            Alert.AlertType.CONFIRMATION
    );

    alerta.setTitle(titulo);
    alerta.setHeaderText(null);
    alerta.setContentText(mensaje);

    return alerta.showAndWait()
            .filter(
                    respuesta ->
                            respuesta
                                    == ButtonType.OK
            )
            .isPresent();
}

    @FXML
    public void initialize() {
        configurarTablas();
        cargarCatalogos();

        paginador = new Paginador<>(tblMateriales, cmbPorPagina, lblPagina,
                                    btnAnterior, btnSiguiente);

        txtBuscar.setOnAction(e -> buscar());
        tblMateriales.getSelectionModel().selectedItemProperty()
                     .addListener((obs, anterior, actual) -> mostrarEnFormulario(actual));

        nuevo();
        buscar();

        // El formulario arranca oculto: la pantalla se abre mostrando datos,
        // no un formulario vacío.
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

    private void configurarTablas() {
        colIsbn.setCellValueFactory(d -> texto(d.getValue().getIsbn()));
        colTitulo.setCellValueFactory(d -> texto(d.getValue().getTitulo()));
        colAutores.setCellValueFactory(d -> texto(d.getValue().getAutores()));
        colTipo.setCellValueFactory(d -> texto(d.getValue().getTipoMaterial()));
        colEditorial.setCellValueFactory(d -> texto(d.getValue().getEditorial()));
        colEjemplares.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getEjemplaresDisponibles() + " / " + d.getValue().getTotalEjemplares()));
        colEstado.setCellValueFactory(d -> texto(d.getValue().getEtiquetaDisponibilidad()));

        tblMateriales.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblMateriales.setPlaceholder(new Label("Sin materiales que coincidan con el filtro."));

        colCodigo.setCellValueFactory(d -> texto(d.getValue().getCodigoEjemplar()));
        colBiblioteca.setCellValueFactory(d -> texto(
                d.getValue().getBiblioteca() == null ? "" : d.getValue().getBiblioteca().getNombre()));
        colAdquisicion.setCellValueFactory(d -> texto(d.getValue().getNumeroAdquisicion()));
        colIngreso.setCellValueFactory(d ->
                new SimpleStringProperty(Formato.fecha(d.getValue().getFechaIngreso())));
        colEstadoEjemplar.setCellValueFactory(d -> texto(d.getValue().getEstado()));

        tblEjemplares.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblEjemplares.setPlaceholder(new Label("Selecciona un material para ver sus ejemplares."));

        lstAutores.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void cargarCatalogos() {
        TipoMaterial todos = new TipoMaterial();
        todos.setIdTipoMaterial(0);
        todos.setNombre("Todos los tipos");

        List<TipoMaterial> tipos = catalogoService.tiposMaterial();

        cmbTipo.getItems().add(todos);
        cmbTipo.getItems().addAll(tipos);
        cmbTipo.getSelectionModel().selectFirst();

        cmbTipoForm.getItems().setAll(tipos);
        cmbEditorial.getItems().setAll(catalogoService.editoriales());
        cmbBiblioteca.getItems().setAll(catalogoService.bibliotecas());
        lstAutores.getItems().setAll(catalogoService.autores());

        cmbDisponibilidad.getItems().addAll("Todos", "Solo disponibles", "Sin ejemplares libres");
        cmbDisponibilidad.getSelectionModel().selectFirst();
    }

    // ------------------------------------------------------------- búsqueda

    @FXML
    private void buscar() {
        TipoMaterial tipo = cmbTipo.getValue();
        Integer idTipo = (tipo == null || tipo.getIdTipoMaterial() == 0) ? null : tipo.getIdTipoMaterial();

        boolean soloDisponibles = "Solo disponibles".equals(cmbDisponibilidad.getValue());

        List<MaterialCatalogo> resultados =
                catalogoService.buscarParaGestion(txtBuscar.getText(), idTipo, soloDisponibles);

        // Este filtro se resuelve aquí porque es el complemento del que ya
        // aplica la consulta, no un criterio distinto.
        if ("Sin ejemplares libres".equals(cmbDisponibilidad.getValue())) {
            resultados = resultados.stream()
                                   .filter(m -> m.getEjemplaresDisponibles() == 0)
                                   .toList();
        }

        paginador.setDatos(resultados);
        lblResumen.setText(resultados.size() + " material(es) en el resultado.");
    }

    @FXML
    private void limpiar() {
        txtBuscar.clear();
        cmbTipo.getSelectionModel().selectFirst();
        cmbDisponibilidad.getSelectionModel().selectFirst();
        buscar();
    }

    // ------------------------------------------------------------- formulario

    /** Deja el formulario listo para capturar un material nuevo. */
    @FXML
    private void nuevo() {
        enEdicion = null;
        tblMateriales.getSelectionModel().clearSelection();

        abrirPanel();
        lblFormulario.setText("Nuevo material");
        txtTitulo.clear();
        txtIsbn.clear();
        txtAnio.clear();
        txtClasificacion.clear();
        cmbTipoForm.getSelectionModel().clearSelection();
        cmbEditorial.getSelectionModel().clearSelection();
        lstAutores.getSelectionModel().clearSelection();

        btnBaja.setDisable(true);
        tblEjemplares.getItems().clear();
        lblEjemplares.setText("Guarda el material para poder registrarle ejemplares.");
        limpiarFormularioEjemplar();
    }

    private void mostrarEnFormulario(MaterialCatalogo material) {
        if (material == null) {
            return;
        }

        enEdicion = material;

        abrirPanel();
        lblFormulario.setText("Editando: " + material.getTitulo());
        txtTitulo.setText(material.getTitulo());
        txtIsbn.setText(material.getIsbn());
        txtAnio.setText(material.getAnioPublicacion() == null
                ? "" : String.valueOf(material.getAnioPublicacion()));
        txtClasificacion.setText(material.getClasificacion());

        seleccionarTipo(material.getIdTipoMaterial());
        seleccionarEditorial(material.getIdEditorial());
        seleccionarAutores(material.getIdMaterial());

        btnBaja.setDisable(false);
        btnBaja.setText("Baja".equalsIgnoreCase(material.getEstadoMaterial())
                ? "Reactivar material" : "Dar de baja");

        cargarEjemplares();
    }

    private void seleccionarTipo(int idTipo) {
        cmbTipoForm.getItems().stream()
                   .filter(t -> t.getIdTipoMaterial() == idTipo)
                   .findFirst()
                   .ifPresent(t -> cmbTipoForm.getSelectionModel().select(t));
    }

    private void seleccionarEditorial(Integer idEditorial) {
        cmbEditorial.getSelectionModel().clearSelection();
        if (idEditorial == null) {
            return;
        }
        cmbEditorial.getItems().stream()
                    .filter(e -> e.getIdEditorial() == idEditorial)
                    .findFirst()
                    .ifPresent(e -> cmbEditorial.getSelectionModel().select(e));
    }

    private void seleccionarAutores(int idMaterial) {
        lstAutores.getSelectionModel().clearSelection();

        List<Integer> ids = catalogoService.autoresDe(idMaterial).stream()
                                           .map(Autor::getIdAutor)
                                           .toList();

        for (int i = 0; i < lstAutores.getItems().size(); i++) {
            if (ids.contains(lstAutores.getItems().get(i).getIdAutor())) {
                lstAutores.getSelectionModel().select(i);
            }
        }
    }

    @FXML
    private void guardar() {
        TipoMaterial tipo = cmbTipoForm.getValue();

        if (tipo == null) {
            Alertas.advertencia("Guardar", "Selecciona el tipo de material.");
            return;
        }

        Integer anio = null;
        String anioTexto = txtAnio.getText();
        if (anioTexto != null && !anioTexto.isBlank()) {
            try {
                anio = Integer.parseInt(anioTexto.trim());
            } catch (NumberFormatException e) {
                Alertas.advertencia("Guardar", "El año de publicación debe ser un número.");
                return;
            }
            if (anio < 1400 || anio > LocalDate.now().getYear() + 1) {
                Alertas.advertencia("Guardar", "Revisa el año de publicación: " + anio + " no es válido.");
                return;
            }
        }

        Material material = new Material();
        material.setIdMaterial(enEdicion == null ? 0 : enEdicion.getIdMaterial());
        material.setTitulo(txtTitulo.getText() == null ? null : txtTitulo.getText().trim());
        material.setIsbn(vacioANulo(txtIsbn.getText()));
        material.setAnioPublicacion(anio == null ? 0 : anio);
        material.setClasificacion(vacioANulo(txtClasificacion.getText()));
        material.setTipoMaterial(tipo);
        material.setEditorial(cmbEditorial.getValue());
        material.setEstado(enEdicion == null ? "Activo" : enEdicion.getEstadoMaterial());

        List<Integer> idsAutores = lstAutores.getSelectionModel().getSelectedItems().stream()
                                             .map(Autor::getIdAutor)
                                             .toList();

        Resultado resultado = catalogoService.guardarMaterial(material, idsAutores);

        if (!resultado.isExito()) {
            Alertas.advertencia("No se pudo guardar", resultado.getMensaje());
            return;
        }

        Alertas.info("Listo", resultado.getMensaje());

        Integer idGuardado = resultado.getIdGenerado();
        buscar();
        seleccionarEnTabla(idGuardado);
    }

    /** Deja seleccionado el material recién guardado, para poder darle ejemplares. */
    private void seleccionarEnTabla(Integer idMaterial) {
        if (idMaterial == null) {
            nuevo();
            return;
        }
        // Se busca en el total, no solo en la página visible: el material
        // recién guardado puede haber caído en otra página del orden.
        paginador.getDatos().stream()
                 .filter(m -> m.getIdMaterial() == idMaterial)
                 .findFirst()
                 .ifPresentOrElse(paginador::seleccionar, this::nuevo);
    }

    @FXML
    private void cambiarEstadoMaterial() {
        if (enEdicion == null) {
            return;
        }

        boolean estaDeBaja = "Baja".equalsIgnoreCase(enEdicion.getEstadoMaterial());

        String mensaje = estaDeBaja
                ? "Se devolverá \"" + enEdicion.getTitulo() + "\" al catálogo."
                : "Se dará de baja \"" + enEdicion.getTitulo() + "\".\n\n"
                  + "Dejará de aparecer en el buscador, pero se conserva su historial "
                  + "de préstamos y sus ejemplares.";

        if (!Alertas.confirmar(estaDeBaja ? "Reactivar material" : "Dar de baja", mensaje)) {
            return;
        }

        Resultado resultado = estaDeBaja
                ? catalogoService.reactivarMaterial(enEdicion.getIdMaterial())
                : catalogoService.darDeBajaMaterial(enEdicion.getIdMaterial());

        avisar(resultado);
        nuevo();
        buscar();
    }

    // ------------------------------------------------------------- ejemplares

    private void cargarEjemplares() {
        if (enEdicion == null) {
            tblEjemplares.getItems().clear();
            return;
        }

        List<Ejemplar> ejemplares = catalogoService.ejemplaresDe(enEdicion.getIdMaterial());
        tblEjemplares.setItems(FXCollections.observableArrayList(ejemplares));

        long libres = ejemplares.stream()
                                .filter(e -> "Disponible".equalsIgnoreCase(e.getEstado()))
                                .count();

        lblEjemplares.setText(ejemplares.size() + " ejemplar(es), " + libres + " disponible(s)");
    }

    @FXML
    private void agregarEjemplar() {
        if (enEdicion == null) {
            Alertas.advertencia("Ejemplares", "Primero selecciona o guarda un material.");
            return;
        }

        Biblioteca biblioteca = cmbBiblioteca.getValue();
        if (biblioteca == null) {
            Alertas.advertencia("Ejemplares", "Selecciona la biblioteca donde estará el ejemplar.");
            return;
        }

        Material material = new Material();
        material.setIdMaterial(enEdicion.getIdMaterial());

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setMaterial(material);
        ejemplar.setBiblioteca(biblioteca);
        ejemplar.setCodigoEjemplar(txtCodigoEjemplar.getText() == null
                ? null : txtCodigoEjemplar.getText().trim());
        ejemplar.setNumeroAdquisicion(vacioANulo(txtNumeroAdquisicion.getText()));
        ejemplar.setFechaIngreso(dpIngreso.getValue() == null ? LocalDate.now() : dpIngreso.getValue());
        ejemplar.setEstado("Disponible");

        Resultado resultado = catalogoService.guardarEjemplar(ejemplar);

        if (resultado.isExito()) {
            limpiarFormularioEjemplar();
            cargarEjemplares();
            buscar();
            seleccionarEnTabla(enEdicion.getIdMaterial());
        } else {
            Alertas.advertencia("No se pudo registrar", resultado.getMensaje());
        }
    }

    /** Sugiere el siguiente código siguiendo el patrón UTNG-0000-00. */
    @FXML
    private void sugerirCodigo() {
        if (enEdicion == null) {
            Alertas.advertencia("Ejemplares", "Primero selecciona un material.");
            return;
        }

        int siguiente = tblEjemplares.getItems().size() + 1;
        txtCodigoEjemplar.setText(String.format("UTNG-%04d-%02d",
                enEdicion.getIdMaterial(), siguiente));
    }

    @FXML
    private void marcarEjemplarBaja() {
        cambiarEstadoEjemplar("Baja",
                "Se dará de baja el ejemplar %s.\n\nÚsalo cuando la copia física se pierde "
              + "o se deteriora. Dejará de contarse como parte del acervo.");
    }

    @FXML
    private void marcarEjemplarDisponible() {
        cambiarEstadoEjemplar("Disponible",
                "El ejemplar %s volverá a estar disponible para préstamo.");
    }

    @FXML
    private void marcarEjemplarReparacion() {
        cambiarEstadoEjemplar("EnReparacion",
                "El ejemplar %s se marcará en reparación y no se podrá prestar "
              + "hasta que lo devuelvas a disponible.");
    }

    @FXML
    private void marcarEjemplarExtraviado() {
        cambiarEstadoEjemplar("Extraviado",
                "El ejemplar %s se marcará como extraviado.");
    }

    private void cambiarEstadoEjemplar(String estado, String plantillaMensaje) {
        Ejemplar seleccionado = tblEjemplares.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Alertas.advertencia("Ejemplares", "Selecciona un ejemplar de la tabla.");
            return;
        }

        if ("Prestado".equalsIgnoreCase(seleccionado.getEstado())) {
            Alertas.advertencia("Ejemplares",
                    "Ese ejemplar está prestado. Registra primero su devolución "
                  + "desde el módulo de Préstamos.");
            return;
        }

        if (estado.equalsIgnoreCase(seleccionado.getEstado())) {
            Alertas.info("Ejemplares", "El ejemplar ya está en estado " + estado + ".");
            return;
        }

        if (!Alertas.confirmar("Cambiar estado",
                String.format(plantillaMensaje, seleccionado.getCodigoEjemplar()))) {
            return;
        }

        Resultado resultado =
                catalogoService.cambiarEstadoEjemplar(seleccionado.getIdEjemplar(), estado);

        avisar(resultado);
        cargarEjemplares();
        buscar();
        seleccionarEnTabla(enEdicion == null ? null : enEdicion.getIdMaterial());
    }

    private void limpiarFormularioEjemplar() {
        txtCodigoEjemplar.clear();
        txtNumeroAdquisicion.clear();
        dpIngreso.setValue(LocalDate.now());
        cmbBiblioteca.getSelectionModel().selectFirst();
    }

    // ------------------------------------------------------------- catálogos

    @FXML
    private void nuevoAutor() {
        Alertas.pedirTexto("Nuevo autor", "Nombre del autor:", "")
               .filter(n -> !n.isBlank())
               .ifPresent(nombre -> {
                   Resultado resultado = catalogoService.nuevoAutor(nombre);
                   avisar(resultado);
                   if (resultado.isExito()) {
                       lstAutores.getItems().setAll(catalogoService.autores());
                   }
               });
    }

    @FXML
    private void nuevaEditorial() {
        Alertas.pedirTexto("Nueva editorial", "Nombre de la editorial:", "")
               .filter(n -> !n.isBlank())
               .ifPresent(nombre -> {
                   Resultado resultado = catalogoService.nuevaEditorial(nombre);
                   avisar(resultado);
                   if (resultado.isExito()) {
                       cmbEditorial.getItems().setAll(catalogoService.editoriales());
                   }
               });
    }

    private void avisar(Resultado resultado) {
        if (resultado.isExito()) {
            Alertas.info("Listo", resultado.getMensaje());
        } else {
            Alertas.advertencia("No se pudo completar", resultado.getMensaje());
        }
    }

    private static String vacioANulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private static SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor == null ? "" : valor);
    }
}
