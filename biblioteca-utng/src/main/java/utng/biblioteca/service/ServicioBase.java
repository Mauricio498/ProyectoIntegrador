package utng.biblioteca.service;

import utng.biblioteca.dto.Resultado;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

/**
 * Utilidades comunes a todos los servicios.
 *
 * La capa de servicio es la frontera donde SQLException deja de propagarse:
 * los controladores de JavaFX reciben listas vacias o un {@link Resultado}
 * con el mensaje de error, nunca una excepcion revisada.
 */
abstract class ServicioBase {

    /** Ejecuta una consulta que devuelve lista; ante un error registra y devuelve vacio. */
    protected <T> List<T> consultar(String descripcion, ConsultaLista<T> consulta) {
        try {
            return consulta.ejecutar();
        } catch (SQLException e) {
            registrar(descripcion, e);
            return List.of();
        }
    }

    /** Ejecuta una operacion que devuelve Resultado; ante un error devuelve Resultado.error. */
    protected Resultado operar(String descripcion, Operacion operacion) {
        try {
            return operacion.ejecutar();
        } catch (SQLException e) {
            registrar(descripcion, e);
            return Resultado.error("Error de base de datos al " + descripcion + ": " + e.getMessage());
        }
    }

    /** Ejecuta una consulta escalar con valor por defecto. */
    protected <T> T valor(String descripcion, ConsultaValor<T> consulta, Supplier<T> porDefecto) {
        try {
            return consulta.ejecutar();
        } catch (SQLException e) {
            registrar(descripcion, e);
            return porDefecto.get();
        }
    }

    private void registrar(String descripcion, SQLException e) {
        System.err.println("[" + getClass().getSimpleName() + "] Fallo al " + descripcion
                         + ": " + e.getMessage());
    }

    @FunctionalInterface
    protected interface ConsultaLista<T> {
        List<T> ejecutar() throws SQLException;
    }

    @FunctionalInterface
    protected interface ConsultaValor<T> {
        T ejecutar() throws SQLException;
    }

    @FunctionalInterface
    protected interface Operacion {
        Resultado ejecutar() throws SQLException;
    }
}
