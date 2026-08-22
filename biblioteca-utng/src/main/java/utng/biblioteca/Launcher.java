package utng.biblioteca;

/**
 * Punto de arranque para el ejecutable empaquetado.
 *
 * ¿Por qué existe si {@link App} ya tiene main? Porque cuando se ejecuta un
 * JAR único, la JVM revisa la clase principal ANTES de arrancar: si esa clase
 * hereda de javafx.application.Application y JavaFX no está en el module-path,
 * aborta con "JavaFX runtime components are missing".
 *
 * Esta clase no hereda de Application, así que la verificación no se dispara y
 * JavaFX se inicializa normalmente desde el classpath.
 *
 * En resumen: para desarrollo se usa App (mvn javafx:run) y para el ejecutable
 * se usa Launcher. Las dos hacen lo mismo.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        App.main(args);
    }
}
