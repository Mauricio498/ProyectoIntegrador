package com.utng.biblioteca.util;

import com.utng.biblioteca.model.Libro;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Fábrica de tarjetas visuales reutilizadas en distintas pantallas
 * (tarjetas de estadística y tarjetas de libro), evitando duplicar la
 * construcción de estos componentes en cada controlador.
 */
public final class CardFactory {

    private CardFactory() {
    }

    /** Tarjeta de estadística para la pantalla de inicio (número grande + etiqueta + icono). */
    public static VBox crearTarjetaEstadistica(String numero, String etiqueta, String icono) {
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
     * Tarjeta de libro reutilizada en Inicio y Buscador. Muestra un
     * placeholder de portada, título, autor, categoría opcional, badge de
     * disponibilidad y un botón de acción.
     */
    public static VBox crearTarjetaLibro(Libro libro, boolean mostrarCategoria, Consumer<Libro> onSolicitar) {
        StackPane portada = new StackPane();
        portada.getStyleClass().add("portada-placeholder");
        portada.setStyle("-fx-background-color: " + libro.getColorPortada() + ";");
        Label lblIniciales = new Label(libro.getIniciales());
        lblIniciales.getStyleClass().add("portada-iniciales");
        portada.getChildren().add(lblIniciales);
        portada.setPrefSize(160, 130);
        portada.setMaxWidth(Double.MAX_VALUE);

        Label lblTitulo = new Label(libro.getTitulo());
        lblTitulo.getStyleClass().add("libro-titulo");
        lblTitulo.setWrapText(true);

        Label lblAutor = new Label(libro.getAutor());
        lblAutor.getStyleClass().add("libro-autor");
        lblAutor.setWrapText(true);

        VBox infoBox = new VBox(2, lblTitulo, lblAutor);

        if (mostrarCategoria) {
            Label lblCategoria = new Label(libro.getCategoria().getEtiqueta());
            lblCategoria.getStyleClass().add("libro-categoria");
            infoBox.getChildren().add(lblCategoria);
        }

        Label badge = new Label(libro.isDisponible() ? "Disponible" : "Prestado");
        badge.getStyleClass().addAll("badge", libro.isDisponible() ? "badge-disponible" : "badge-prestado");

        Button boton = new Button(libro.isDisponible() ? "Solicitar" : "No disponible");
        boton.getStyleClass().add(libro.isDisponible() ? "btn-primary-sm" : "btn-disabled-sm");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setDisable(!libro.isDisponible());
        if (libro.isDisponible() && onSolicitar != null) {
            boton.setOnAction(e -> onSolicitar.accept(libro));
        }

        HBox estadoBox = new HBox(badge);
        estadoBox.setAlignment(Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(10, portada, infoBox, estadoBox, boton);
        tarjeta.getStyleClass().add("libro-card");
        tarjeta.setPadding(new Insets(16));
        tarjeta.setPrefWidth(230);
        return tarjeta;
    }
}
