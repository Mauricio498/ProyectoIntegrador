package utng.biblioteca.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Agrupamiento de las series de tiempo. El nombre que viaja a SQL Server es
 * {@link #getClaveSql()}, que es lo que espera dbo.FN_InicioPeriodo.
 */
public enum Periodo {

    DIA("Dia", "Por día", "dd/MM"),
    SEMANA("Semana", "Por semana", "dd/MM"),
    MES("Mes", "Por mes", "MMM yyyy");

    private static final Locale MX = Locale.of("es", "MX");

    private final String claveSql;
    private final String etiqueta;
    private final String patronFecha;

    Periodo(String claveSql, String etiqueta, String patronFecha) {
        this.claveSql = claveSql;
        this.etiqueta = etiqueta;
        this.patronFecha = patronFecha;
    }

    public String getClaveSql() {
        return claveSql;
    }

    /** Cómo se rotula el eje X de la gráfica para este agrupamiento. */
    public String formatear(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        if (this == SEMANA) {
            return "Sem " + DateTimeFormatter.ofPattern(patronFecha, MX).format(fecha);
        }
        return DateTimeFormatter.ofPattern(patronFecha, MX).format(fecha);
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
