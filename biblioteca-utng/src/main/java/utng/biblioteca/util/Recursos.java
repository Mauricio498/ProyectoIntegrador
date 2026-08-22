package utng.biblioteca.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

import java.io.InputStream;

/**
 * Carga de recursos gráficos: logotipo y tipografías de la guía de estilo.
 *
 * Todo aquí es tolerante a que el archivo no exista, porque el logotipo
 * definitivo y las fuentes Archivo/Telegraf se integran después: si falta
 * alguno, la aplicación sigue funcionando con el respaldo correspondiente.
 */
public final class Recursos {

    /** Reemplaza este archivo por el logotipo definitivo, mismo nombre y ruta. */
    public static final String RUTA_LOGO = "/images/logo.png";

    /**
     * Archivos de fuente que se intentan registrar al iniciar. Coloca los .ttf
     * en src/main/resources/fonts con estos nombres y el CSS los tomará solo.
     */
    private static final String[] FUENTES = {
            "/fonts/Archivo-Regular.ttf",
            "/fonts/Archivo-Medium.ttf",
            "/fonts/Archivo-SemiBold.ttf",
            "/fonts/Archivo-Bold.ttf",
            "/fonts/Telegraf-Regular.ttf",
            "/fonts/Telegraf-Bold.ttf"
    };

    private static Image logoEnCache;
    private static boolean logoBuscado;

    private Recursos() {
    }

    /** Registra las tipografías presentes. Las que falten se ignoran en silencio. */
    public static void cargarFuentes() {
        int cargadas = 0;

        for (String ruta : FUENTES) {
            try (InputStream in = Recursos.class.getResourceAsStream(ruta)) {
                if (in != null && Font.loadFont(in, 12) != null) {
                    cargadas++;
                }
            } catch (Exception e) {
                // Una fuente que no carga no debe impedir que arranque la aplicación.
            }
        }

        if (cargadas == 0) {
            System.out.println("Tipografías de la guía no encontradas en /fonts; "
                             + "se usará Arial como respaldo.");
        }
    }

    /** Logotipo institucional, o null si todavía no se ha integrado el archivo. */
    public static Image logo() {
        if (!logoBuscado) {
            logoBuscado = true;
            try (InputStream in = Recursos.class.getResourceAsStream(RUTA_LOGO)) {
                if (in != null) {
                    logoEnCache = new Image(in);
                }
            } catch (Exception e) {
                logoEnCache = null;
            }
        }
        return logoEnCache;
    }

    /**
     * Devuelve el logotipo listo para colocar, escalado al alto indicado.
     * Si el archivo aún no existe, el ImageView queda vacío y conserva el
     * espacio reservado en la vista.
     */
    public static ImageView logoView(double alto) {
        ImageView vista = new ImageView();
        Image imagen = logo();

        if (imagen != null) {
            vista.setImage(imagen);
        }

        vista.setFitHeight(alto);
        vista.setPreserveRatio(true);
        vista.setSmooth(true);
        return vista;
    }
}
