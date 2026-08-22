package utng.biblioteca;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utng.biblioteca.config.Conexion;
import utng.biblioteca.controller.comun.Router;
import utng.biblioteca.util.Recursos;

/**
 * Punto de entrada del sistema Biblioteca UTNG.
 *
 * Arranca comprobando la conexión a SQL Server: si la base no responde no
 * tiene sentido mostrar el login, así que se avisa con un mensaje claro en
 * lugar de dejar que la aplicación falle más adelante.
 */
public class App extends Application {

    @Override
    public void init() {
        // Se registran las tipografías de la guía de estilo antes de construir
        // la interfaz; si los archivos no están, el CSS cae en Arial.
        Recursos.cargarFuentes();
    }

    @Override
    public void start(Stage stage) {
        Router.iniciar(stage);

        Image icono = Recursos.logo();
        if (icono != null) {
            stage.getIcons().add(icono);
        }

        if (!Conexion.probar()) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Biblioteca UTNG");
            alerta.setHeaderText("No hay conexión con la base de datos");
            // En otra computadora lo primero que falla es la configuración,
            // así que se dice exactamente qué archivo se leyó y a dónde apunta.
            alerta.setContentText(
                    "Configuración leída de:\n" + Conexion.getOrigenConfiguracion()
                  + "\n\nIntentando conectar a: " + Conexion.getDestino()
                  + "\n\nRevisa que:\n"
                  + "  • El servicio de SQL Server esté en ejecución.\n"
                  + "  • TCP/IP esté habilitado y el puerto 1433 abierto.\n"
                  + "  • El usuario y la contraseña sean correctos.\n"
                  + "  • Ya se hayan ejecutado los scripts de la carpeta sql/.\n\n"
                  + "Puedes editar config/database.properties junto al ejecutable "
                  + "y volver a abrir la aplicación.");
            alerta.showAndWait();
        }

        Router.mostrarLogin();
        stage.show();
    }

    @Override
    public void stop() {
        Conexion.cerrar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
