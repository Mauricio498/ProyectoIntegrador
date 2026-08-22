package utng.biblioteca.controller.comun;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import utng.biblioteca.session.Sesion;

import java.io.IOException;

/**
 * Cambia entre las tres escenas principales de la aplicacion y decide, segun
 * el rol del usuario autenticado, cual interfaz corresponde.
 *
 * Este es el punto donde se unen los dos prototipos previos:
 *   - SuperAdministrador y Administrador -> interfaz de gestion (utng1)
 *   - Profesor, Estudiante e Invitado    -> interfaz de consulta (utng2)
 */
public final class Router {

    private static final String CSS_ADMIN   = "/css/admin.css";
    private static final String CSS_USUARIO = "/css/usuario.css";

    private static Stage escenario;

    private Router() {
    }

    public static void iniciar(Stage stage) {
        escenario = stage;
        escenario.setTitle("Biblioteca UTNG");
        escenario.setMinWidth(1100);
        escenario.setMinHeight(700);
    }

    /** Área utilizable de la pantalla, sin contar la barra de tareas. */
    private static Rectangle2D areaPantalla() {
        return Screen.getPrimary().getVisualBounds();
    }

    public static Stage getEscenario() {
        return escenario;
    }

    /** Alta de cuenta para estudiantes y profesores. */
    public static void mostrarRegistro() {
        cambiar("/fxml/comun/registro.fxml", CSS_USUARIO, 1000, 780, true);
        escenario.setTitle("Biblioteca UTNG - Crear cuenta");
    }

    public static void mostrarLogin() {
        cambiar("/fxml/comun/login.fxml", CSS_USUARIO, 1000, 680, true);
        escenario.setTitle("Biblioteca UTNG - Acceso");
    }

    /**
     * Envia al usuario recien autenticado a la interfaz que le corresponde.
     * Se llama desde LoginController despues de un inicio de sesion valido.
     */
    public static void mostrarSegunRol() {
        // Las pantallas de trabajo abren ocupando toda la pantalla: son tablas
        // y formularios que necesitan el ancho completo desde el primer momento.
        Rectangle2D pantalla = areaPantalla();

        if (Sesion.esPersonalBiblioteca()) {
            cambiar("/fxml/admin/shell.fxml", CSS_ADMIN,
                    pantalla.getWidth(), pantalla.getHeight(), true);
            escenario.setTitle("Biblioteca UTNG - Panel de gestión");
        } else {
            cambiar("/fxml/usuario/shell.fxml", CSS_USUARIO,
                    pantalla.getWidth(), pantalla.getHeight(), true);
            escenario.setTitle("Biblioteca Digital UTNG");
        }
    }

    private static void cambiar(String recursoFxml, String hojaEstilos,
                                double ancho, double alto, boolean maximizar) {
        try {
            FXMLLoader loader = new FXMLLoader(Router.class.getResource(recursoFxml));
            Parent raiz = loader.load();

            // La escena se crea ya con el tamaño final. Si se crea pequeña y
            // luego se maximiza, JavaFX reparte el espacio con el tamaño viejo
            // y las tablas nacen encogidas.
            Scene escena = new Scene(raiz, ancho, alto);
            escena.getStylesheets().add(Router.class.getResource(hojaEstilos).toExternalForm());

            escenario.setScene(escena);

            if (maximizar) {
                Rectangle2D pantalla = areaPantalla();
                escenario.setX(pantalla.getMinX());
                escenario.setY(pantalla.getMinY());
                escenario.setWidth(pantalla.getWidth());
                escenario.setHeight(pantalla.getHeight());
                escenario.setMaximized(true);
            } else {
                escenario.setMaximized(false);
                escenario.sizeToScene();
                escenario.centerOnScreen();
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la vista " + recursoFxml, e);
        }
    }
}
