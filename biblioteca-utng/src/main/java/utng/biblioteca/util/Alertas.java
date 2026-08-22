package utng.biblioteca.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/** Dialogos estandar de la aplicacion. */
public final class Alertas {

    private Alertas() {
    }

    public static void info(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    public static void error(String titulo, String mensaje) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje);
    }

    public static void advertencia(String titulo, String mensaje) {
        mostrar(Alert.AlertType.WARNING, titulo, mensaje);
    }

    public static boolean confirmar(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.CANCEL, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        return alerta.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    public static Optional<String> pedirTexto(String titulo, String mensaje, String valorInicial) {
        TextInputDialog dialogo = new TextInputDialog(valorInicial);
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(null);
        dialogo.setContentText(mensaje);
        return dialogo.showAndWait();
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
