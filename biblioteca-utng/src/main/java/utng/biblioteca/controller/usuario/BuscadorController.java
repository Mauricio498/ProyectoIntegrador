package utng.biblioteca.controller.usuario;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import utng.biblioteca.dto.MaterialCatalogo;
import utng.biblioteca.model.TipoMaterial;
import utng.biblioteca.service.CatalogoService;
import utng.biblioteca.service.PrestamoService;
import utng.biblioteca.session.Sesion;
import utng.biblioteca.util.Alertas;
import utng.biblioteca.util.Tarjetas;

import java.util.List;

/**
 * Buscador del catalogo para el usuario final.
 *
 * Cada busqueda con texto se guarda en la tabla Busqueda a traves del
 * servicio, incluso cuando la hace un invitado.
 */
public class BuscadorController {

    @FXML private TextField txtBusqueda;
    @FXML private ComboBox<TipoMaterial> cmbTipo;
    @FXML private CheckBox chkSoloDisponibles;
    @FXML private Label lblResultados;
    @FXML private TilePane contenedorResultados;

    private final CatalogoService catalogoService = new CatalogoService();
    private final PrestamoService prestamoService = new PrestamoService();

    @FXML
    public void initialize() {
        TipoMaterial todos = new TipoMaterial();
        todos.setIdTipoMaterial(0);
        todos.setNombre("Todos los tipos");

        cmbTipo.getItems().add(todos);
        cmbTipo.getItems().addAll(catalogoService.tiposMaterial());
        cmbTipo.getSelectionModel().selectFirst();

        txtBusqueda.setOnAction(e -> buscar());

        buscar();
    }

    @FXML
    private void buscar() {
        TipoMaterial tipo = cmbTipo.getValue();
        Integer idTipo = (tipo == null || tipo.getIdTipoMaterial() == 0) ? null : tipo.getIdTipoMaterial();

        List<MaterialCatalogo> resultados = catalogoService.buscar(
                txtBusqueda.getText(), idTipo, chkSoloDisponibles.isSelected());

        pintar(resultados);
    }

    @FXML
    private void limpiar() {
        txtBusqueda.clear();
        cmbTipo.getSelectionModel().selectFirst();
        chkSoloDisponibles.setSelected(false);
        buscar();
    }

    private void pintar(List<MaterialCatalogo> resultados) {
        lblResultados.setText(resultados.isEmpty()
                ? "No se encontraron materiales con esos criterios."
                : resultados.size() + " material(es) encontrado(s)");

        boolean puedeSolicitar = Sesion.puedeSolicitarPrestamo();

        contenedorResultados.getChildren().clear();
        for (MaterialCatalogo material : resultados) {
            contenedorResultados.getChildren()
                    .add(Tarjetas.material(material, puedeSolicitar, this::solicitar));
        }
    }

    /**
     * Envia una solicitud de prestamo. El material no se aparta todavia: el
     * personal de biblioteca revisa la solicitud y, al aprobarla, asigna el
     * ejemplar y genera el prestamo.
     */
    private void solicitar(MaterialCatalogo material) {
        boolean confirmado = Alertas.confirmar("Solicitar prestamo",
                "Se enviara una solicitud de: " + material.getTitulo()
              + System.lineSeparator() + System.lineSeparator()
              + "Queda pendiente hasta que el personal de biblioteca la apruebe.");

        if (!confirmado) {
            return;
        }

        var resultado = prestamoService.solicitarMaterial(material.getIdMaterial());

        if (resultado.isExito()) {
            Alertas.info("Solicitud enviada", resultado.getMensaje());
        } else {
            Alertas.advertencia("No se pudo enviar", resultado.getMensaje());
        }
        buscar();
    }
}
