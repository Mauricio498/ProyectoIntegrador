package utng.biblioteca.dto;

/**
 * Respuesta de una operacion de negocio.
 *
 * Los procedimientos almacenados devuelven un mensaje de exito o de error en
 * un parametro OUTPUT; esta clase transporta ese resultado hasta la interfaz
 * sin obligar a los controladores a manejar SQLException.
 */
public class Resultado {

    private final boolean exito;
    private final String mensaje;
    private final Integer idGenerado;

    private Resultado(boolean exito, String mensaje, Integer idGenerado) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.idGenerado = idGenerado;
    }

    public static Resultado ok(String mensaje) {
        return new Resultado(true, mensaje, null);
    }

    public static Resultado ok(String mensaje, Integer idGenerado) {
        return new Resultado(true, mensaje, idGenerado);
    }

    public static Resultado error(String mensaje) {
        return new Resultado(false, mensaje, null);
    }

    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public Integer getIdGenerado() { return idGenerado; }
}
