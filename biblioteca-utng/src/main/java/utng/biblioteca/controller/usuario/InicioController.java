package utng.biblioteca.controller.usuario;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import utng.biblioteca.dto.EstadisticasGenerales;
import utng.biblioteca.dto.MaterialCatalogo;
import utng.biblioteca.service.CatalogoService;
import utng.biblioteca.service.EstadisticaService;
import utng.biblioteca.service.PrestamoService;
import utng.biblioteca.session.Sesion;
import utng.biblioteca.util.Alertas;
import utng.biblioteca.util.Formato;
import utng.biblioteca.util.Tarjetas;

import java.util.List;

/** Pantalla de bienvenida del usuario final: resumen personal y destacados. */
public class InicioController {

    @FXML private Label lblSaludo;
    @FXML private Label lblSubtitulo;
    @FXML private HBox contenedorEstadisticas;
    @FXML private TilePane contenedorDestacados;

    private final CatalogoService catalogoService = new CatalogoService();
    private final PrestamoService prestamoService = new PrestamoService();
    private final EstadisticaService estadisticaService = new EstadisticaService();

    private UsuarioShellController shell;

    public void setShell(UsuarioShellController shell) {
        this.shell = shell;
    }

    @FXML
    public void initialize() {
        lblSaludo.setText("Hola, " + Sesion.getNombre());

        if (Sesion.puedeSolicitarPrestamo()) {
            lblSubtitulo.setText("Consulta el catálogo, solicita préstamos y da seguimiento "
                               + "a tus materiales desde un solo lugar. Las solicitudes las "
                               + "autoriza el personal de biblioteca.");
        } else if (Sesion.esInvitado()) {
            lblSubtitulo.setText("Estas navegando sin cuenta: puedes explorar todo el catálogo. "
                               + "Para solicitar préstamos inicia sesión con tu cuenta UTNG.");
        } else {
            lblSubtitulo.setText("Tu rol tiene acceso de consulta: puedes explorar todo el "
                               + "catálogo, aunque no solicitar préstamos.");
        }

        cargarEstadisticas();
        cargarDestacados();
    }

    @FXML
    private void irAlBuscador() {
        if (shell != null) {
            shell.irABuscador();
        }
    }

    private void cargarEstadisticas() {
        EstadisticasGenerales generales = estadisticaService.generales();

        if (Sesion.esInvitado()) {
            contenedorEstadisticas.getChildren().setAll(
                    Tarjetas.estadistica(String.valueOf(generales.getTotalMateriales()),
                            "Materiales en el catálogo", "📚"),
                    Tarjetas.estadistica(String.valueOf(generales.getEjemplaresDisponibles()),
                            "Ejemplares disponibles", "📗"),
                    Tarjetas.estadistica(String.valueOf(generales.getTotalEjemplares()),
                            "Ejemplares en total", "🏷")
            );
            contenedorEstadisticas.getChildren()
                    .forEach(nodo -> HBox.setHgrow(nodo, Priority.ALWAYS));
            return;
        }

        contenedorEstadisticas.getChildren().setAll(
                Tarjetas.estadistica(String.valueOf(generales.getEjemplaresDisponibles()),
                        "Ejemplares disponibles", "📗"),
                Tarjetas.estadistica(String.valueOf(prestamoService.misPrestamos(true).size()),
                        "Mis préstamos activos", "⏳"),
                Tarjetas.estadistica(String.valueOf(prestamoService.misSolicitudes().stream()
                                .filter(sol -> sol.estaPendiente()).count()),
                        "Solicitudes por aprobar", "📨"),
                Tarjetas.estadistica(String.valueOf(prestamoService.cupoDisponible()),
                        "Materiales que puedo llevar", "🎒"),
                Tarjetas.estadistica(Formato.moneda(prestamoService.miAdeudo()),
                        "Adeudo pendiente", "💰")
        );

        contenedorEstadisticas.getChildren()
                .forEach(nodo -> HBox.setHgrow(nodo, Priority.ALWAYS));
    }

    private void cargarDestacados() {
        List<MaterialCatalogo> destacados = catalogoService.destacados(8);

        contenedorDestacados.getChildren().clear();
        boolean puedeSolicitar = Sesion.puedeSolicitarPrestamo();

        for (MaterialCatalogo material : destacados) {
            contenedorDestacados.getChildren()
                    .add(Tarjetas.material(material, puedeSolicitar, this::solicitar));
        }
    }

    private void solicitar(MaterialCatalogo material) {
        var resultado = prestamoService.solicitarMaterial(material.getIdMaterial());

        if (resultado.isExito()) {
            Alertas.info("Solicitud enviada", resultado.getMensaje());
            cargarEstadisticas();
            cargarDestacados();
        } else {
            Alertas.advertencia("No se pudo enviar", resultado.getMensaje());
        }
    }
}
