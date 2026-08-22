package utng.biblioteca.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import utng.biblioteca.dto.MaterialCatalogo;
import utng.biblioteca.dto.PrestamoDetalle;
import utng.biblioteca.dto.SolicitudPrestamoDetalle;

import java.util.function.Consumer;

/**
 * Fabrica de tarjetas de la interfaz de consulta (heredada de utng2), ahora
 * alimentada con los datos reales de VW_CatalogoMaterial y VW_PrestamoDetalle.
 */
public final class Tarjetas {

    private Tarjetas() {
    }

    /** Tarjeta de estadistica: icono, numero grande y etiqueta. */
    public static VBox estadistica(String numero, String etiqueta, String icono) {
        Label lblIcono = new Label(icono);
        lblIcono.getStyleClass().add("stat-icon");

        Label lblNumero = new Label(numero);
        lblNumero.getStyleClass().add("stat-number");

        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.getStyleClass().add("stat-label");

        VBox tarjeta = new VBox(6, lblIcono, lblNumero, lblEtiqueta);
        tarjeta.getStyleClass().add("stat-card");
        tarjeta.setPadding(new Insets(22));
        HBox.setHgrow(tarjeta, Priority.ALWAYS);
        return tarjeta;
    }

    /**
     * Tarjeta de material del catalogo.
     *
     * El boton queda deshabilitado cuando no hay ejemplares libres o cuando el
     * tipo de material no es prestable (por ejemplo, obras de consulta).
     */
    public static VBox material(MaterialCatalogo material, boolean puedeSolicitar,
                                Consumer<MaterialCatalogo> alSolicitar) {

        StackPane portada = new StackPane();
        portada.getStyleClass().add("portada-placeholder");
        portada.setStyle("-fx-background-color: " + material.getColorPortada() + ";");

        Label lblIniciales = new Label(material.getIniciales());
        lblIniciales.getStyleClass().add("portada-iniciales");
        portada.getChildren().add(lblIniciales);
        portada.setPrefSize(160, 130);
        portada.setMaxWidth(Double.MAX_VALUE);

        Label lblTitulo = new Label(material.getTitulo());
        lblTitulo.getStyleClass().add("libro-titulo");
        lblTitulo.setWrapText(true);

        Label lblAutor = new Label(vacioSiNulo(material.getAutores(), "Autor no registrado"));
        lblAutor.getStyleClass().add("libro-autor");
        lblAutor.setWrapText(true);

        Label lblTipo = new Label(material.getTipoMaterial());
        lblTipo.getStyleClass().add("libro-categoria");

        VBox info = new VBox(2, lblTitulo, lblAutor, lblTipo);

        // La clasificación es la signatura topográfica: dice en qué estante
        // está el ejemplar, así que es lo que el usuario necesita para ir por él.
        if (material.getClasificacion() != null && !material.getClasificacion().isBlank()) {
            Label lblUbicacion = new Label("📍 " + material.getClasificacion());
            lblUbicacion.getStyleClass().add("libro-clasificacion");
            lblUbicacion.setWrapText(true);
            info.getChildren().add(lblUbicacion);
        }

        Label badge = new Label(material.getEtiquetaDisponibilidad());
        badge.getStyleClass().addAll("badge", material.getClaseBadge());

        Label ejemplares = new Label(material.getEjemplaresDisponibles()
                                   + " de " + material.getTotalEjemplares() + " libres");
        ejemplares.getStyleClass().add("libro-ejemplares");

        HBox estado = new HBox(10, badge, ejemplares);
        estado.setAlignment(Pos.CENTER_LEFT);

        boolean habilitado = material.isDisponible() && puedeSolicitar;

        Button boton = new Button(textoBoton(material, puedeSolicitar));
        boton.getStyleClass().add(habilitado ? "btn-primary-sm" : "btn-disabled-sm");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setDisable(!habilitado);
        if (habilitado && alSolicitar != null) {
            boton.setOnAction(e -> alSolicitar.accept(material));
        }

        VBox tarjeta = new VBox(10, portada, info, estado, boton);
        tarjeta.getStyleClass().add("libro-card");
        tarjeta.setPadding(new Insets(16));
        tarjeta.setPrefWidth(240);
        return tarjeta;
    }

    private static String textoBoton(MaterialCatalogo material, boolean puedeSolicitar) {
        if (!material.isEsPrestable()) {
            return "Solo en sala";
        }
        if (material.getEjemplaresDisponibles() == 0) {
            return "Sin ejemplares";
        }
        return puedeSolicitar ? "Solicitar" : "Consulta unicamente";
    }

    /** Renglon de un prestamo en la pantalla del usuario final. */
    public static HBox prestamo(PrestamoDetalle detalle) {
        Label lblTitulo = new Label(detalle.getTitulo());
        lblTitulo.getStyleClass().add("libro-titulo");
        lblTitulo.setWrapText(true);

        Label lblCodigo = new Label("Ejemplar " + detalle.getCodigoEjemplar()
                                  + "  -  " + detalle.getBiblioteca());
        lblCodigo.getStyleClass().add("libro-autor");

        Label lblFechas = new Label("Prestado el " + Formato.fechaHora(detalle.getFechaPrestamo())
                                  + "  -  Devolver antes del " + Formato.fecha(detalle.getFechaLimite()));
        lblFechas.getStyleClass().add("libro-ejemplares");

        VBox info = new VBox(4, lblTitulo, lblCodigo, lblFechas);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label badge = new Label(detalle.getEtiquetaEstado());
        badge.getStyleClass().addAll("badge", detalle.getClaseBadge());

        VBox derecha = new VBox(6, badge);
        derecha.setAlignment(Pos.CENTER_RIGHT);

        if (detalle.estaPendiente()) {
            long dias = detalle.getDiasRestantes();
            Label lblDias = new Label(dias >= 0
                    ? "Quedan " + dias + " dia(s)"
                    : "Vencido hace " + Math.abs(dias) + " dia(s)");
            lblDias.getStyleClass().add("libro-ejemplares");
            derecha.getChildren().add(lblDias);
        }

        HBox fila = new HBox(16, info, derecha);
        fila.getStyleClass().add("prestamo-card");
        fila.setPadding(new Insets(16));
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    /**
     * Renglon de una solicitud de prestamo en la pantalla del usuario final.
     * El boton de cancelar solo aparece mientras la solicitud siga pendiente.
     */
    public static HBox solicitud(SolicitudPrestamoDetalle solicitud,
                                 Consumer<SolicitudPrestamoDetalle> alCancelar) {

        Label lblTitulo = new Label(solicitud.getTitulo());
        lblTitulo.getStyleClass().add("libro-titulo");
        lblTitulo.setWrapText(true);

        Label lblAutor = new Label(vacioSiNulo(solicitud.getAutores(), "Autor no registrado"));
        lblAutor.getStyleClass().add("libro-autor");

        Label lblFecha = new Label("Solicitado el " + Formato.fechaHora(solicitud.getFechaSolicitud()));
        lblFecha.getStyleClass().add("libro-ejemplares");

        VBox info = new VBox(4, lblTitulo, lblAutor, lblFecha);
        HBox.setHgrow(info, Priority.ALWAYS);

        if (solicitud.getObservaciones() != null && !solicitud.getObservaciones().isBlank()) {
            Label lblRespuesta = new Label("Respuesta: " + solicitud.getObservaciones());
            lblRespuesta.getStyleClass().add("libro-ejemplares");
            lblRespuesta.setWrapText(true);
            info.getChildren().add(lblRespuesta);
        }

        Label badge = new Label(solicitud.getEstado());
        badge.getStyleClass().addAll("badge", solicitud.getClaseBadge());

        VBox derecha = new VBox(6, badge);
        derecha.setAlignment(Pos.CENTER_RIGHT);

        if (solicitud.estaPendiente() && alCancelar != null) {
            Button cancelar = new Button("Cancelar");
            cancelar.getStyleClass().add("btn-secondary");
            cancelar.setOnAction(e -> alCancelar.accept(solicitud));
            derecha.getChildren().add(cancelar);
        }

        HBox fila = new HBox(16, info, derecha);
        fila.getStyleClass().add("prestamo-card");
        fila.setPadding(new Insets(16));
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private static String vacioSiNulo(String valor, String alternativa) {
        return (valor == null || valor.isBlank()) ? alternativa : valor;
    }
}
