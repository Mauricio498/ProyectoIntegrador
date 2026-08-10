package com.utng.biblioteca.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

import java.util.Optional;

/**
 * Utilidad para mostrar mensajes amigables de confirmación, éxito y error,
 * con estilos consistentes con el resto de la aplicación.
 */
public final class AlertUtil {

    private AlertUtil() {
    }

    public static boolean confirmar(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(contenido);
        estilizar(alert);
        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    public static void exito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Listo");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        estilizar(alert);
        alert.showAndWait();
    }

    public static void error(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No se pudo completar la acción");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        estilizar(alert);
        alert.showAndWait();
    }

    private static void estilizar(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(AlertUtil.class.getResource("/css/style.css").toExternalForm());
        pane.getStyleClass().add("dialogo-app");
    }
}
