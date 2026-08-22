package utng.biblioteca.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formateo consistente de fechas y montos en toda la interfaz. */
public final class Formato {

    private static final Locale MX = Locale.of("es", "MX");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", MX);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", MX);

    private Formato() {
    }

    public static String fecha(LocalDate f) {
        return f == null ? "" : FECHA.format(f);
    }

    public static String fechaHora(LocalDateTime f) {
        return f == null ? "" : FECHA_HORA.format(f);
    }

    public static String moneda(BigDecimal monto) {
        return monto == null ? "$0.00" : String.format(MX, "$%,.2f", monto);
    }

    /** Iniciales para los avatares circulares (maximo dos letras). */
    public static String iniciales(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return "?";
        }
        StringBuilder sb = new StringBuilder();
        for (String parte : nombreCompleto.trim().split("\\s+")) {
            if (!parte.isBlank() && sb.length() < 2) {
                sb.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        return sb.toString();
    }
}
