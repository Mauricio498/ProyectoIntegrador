package utng.biblioteca.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto único de acceso a la base de datos BibliotecaUTNG.
 *
 * Mantiene un pool de conexiones (HikariCP). Todos los DAO piden su conexión
 * aquí con {@link #obtener()} y la cierran con try-with-resources: al cerrarla
 * no se destruye, regresa al pool.
 *
 * <b>De dónde sale la configuración.</b> Se busca en este orden y se usa la
 * primera que exista:
 * <ol>
 *   <li>La ruta indicada al arrancar: {@code -Dbiblioteca.config=C:\ruta\database.properties}</li>
 *   <li>{@code config/database.properties} junto al ejecutable</li>
 *   <li>El archivo incluido dentro del JAR (valores de desarrollo)</li>
 * </ol>
 *
 * Ese orden es lo que permite instalar la aplicación en varias computadoras:
 * cada una edita su archivo externo para apuntar a su servidor, sin tener que
 * recompilar nada.
 */
public final class Conexion {

    private static final String RECURSO_INTERNO = "/config/database.properties";
    private static final String PROPIEDAD_RUTA  = "biblioteca.config";
    private static final Path RUTA_EXTERNA = Path.of("config", "database.properties");

    private static HikariDataSource dataSource;
    private static Properties propiedades;

    /** De dónde se leyó la configuración; se muestra si falla la conexión. */
    private static String origenConfiguracion = "(sin cargar)";

    private Conexion() {
    }

    /** Carga la configuracion y levanta el pool. Idempotente. */
    private static synchronized void inicializar() {
        if (dataSource != null) {
            return;
        }

        propiedades = cargarPropiedades();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(construirUrl());
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        if (!autenticacionWindows()) {
            config.setUsername(propiedades.getProperty("db.usuario", ""));
            config.setPassword(propiedades.getProperty("db.password", ""));
        }

        config.setMaximumPoolSize(entero("db.poolMaximo", 10));
        config.setMinimumIdle(entero("db.poolMinimo", 2));
        config.setConnectionTimeout(entero("db.timeoutConexionMs", 15000));
        config.setPoolName("BibliotecaUTNG-Pool");

        dataSource = new HikariDataSource(config);
    }

    /**
     * Busca la configuración en las tres ubicaciones, en orden de prioridad.
     */
    private static Properties cargarPropiedades() {
        Properties p = new Properties();

        // 1. Ruta explícita al arrancar
        String rutaIndicada = System.getProperty(PROPIEDAD_RUTA);
        if (rutaIndicada != null && !rutaIndicada.isBlank()) {
            Path ruta = Path.of(rutaIndicada.trim());
            if (Files.isReadable(ruta)) {
                return leerArchivo(p, ruta);
            }
            System.err.println("No se pudo leer la configuración indicada en -D"
                             + PROPIEDAD_RUTA + ": " + ruta.toAbsolutePath());
        }

        // 2. Archivo externo junto al ejecutable
        if (Files.isReadable(RUTA_EXTERNA)) {
            return leerArchivo(p, RUTA_EXTERNA);
        }

        // 3. Valores incluidos en el JAR
        try (InputStream in = Conexion.class.getResourceAsStream(RECURSO_INTERNO)) {
            if (in == null) {
                throw new IllegalStateException(
                        "No hay configuración de base de datos. Crea el archivo "
                      + RUTA_EXTERNA.toAbsolutePath() + " con los datos de tu servidor.");
            }
            p.load(in);
            origenConfiguracion = "valores incluidos en la aplicación";
            return p;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + RECURSO_INTERNO, e);
        }
    }

    private static Properties leerArchivo(Properties p, Path ruta) {
        try (InputStream in = Files.newInputStream(ruta)) {
            p.load(in);
            origenConfiguracion = ruta.toAbsolutePath().toString();
            return p;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ruta.toAbsolutePath(), e);
        }
    }

    /** Ruta del archivo de configuración que se está usando. */
    public static String getOrigenConfiguracion() {
        return origenConfiguracion;
    }

    /** Servidor y base a los que se está apuntando, para mensajes de error. */
    public static String getDestino() {
        if (propiedades == null) {
            return "(sin configurar)";
        }
        return propiedades.getProperty("db.servidor", "?")
             + " / " + propiedades.getProperty("db.base", "?");
    }

    private static String construirUrl() {
        StringBuilder url = new StringBuilder("jdbc:sqlserver://")
                .append(propiedades.getProperty("db.servidor", "localhost"));

        String puerto = propiedades.getProperty("db.puerto", "").trim();
        if (!puerto.isEmpty()) {
            url.append(":").append(puerto);
        }

        url.append(";databaseName=").append(propiedades.getProperty("db.base", "BibliotecaUTNG"))
           .append(";encrypt=").append(propiedades.getProperty("db.encrypt", "true"))
           .append(";trustServerCertificate=")
           .append(propiedades.getProperty("db.trustServerCertificate", "true"));

        if (autenticacionWindows()) {
            url.append(";integratedSecurity=true");
        }

        return url.toString();
    }

    private static boolean autenticacionWindows() {
        return Boolean.parseBoolean(propiedades.getProperty("db.autenticacionWindows", "false"));
    }

    private static int entero(String clave, int porDefecto) {
        try {
            return Integer.parseInt(propiedades.getProperty(clave, String.valueOf(porDefecto)).trim());
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    /**
     * Devuelve una conexión del pool. Usar siempre dentro de try-with-resources.
     */
    public static Connection obtener() throws SQLException {
        inicializar();
        return dataSource.getConnection();
    }

    /**
     * Comprueba que la base responde. Se llama antes de mostrar el login para
     * dar un mensaje claro en lugar de fallar mas adelante.
     */
    public static boolean probar() {
        try (Connection c = obtener()) {
            return c.isValid(5);
        } catch (Exception e) {
            System.err.println("No se pudo conectar a SQL Server: " + e.getMessage());
            return false;
        }
    }

    /** Cierra el pool al terminar la aplicacion. */
    public static synchronized void cerrar() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        dataSource = null;
    }
}
