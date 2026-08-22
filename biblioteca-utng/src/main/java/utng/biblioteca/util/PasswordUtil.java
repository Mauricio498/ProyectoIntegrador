package utng.biblioteca.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash de contrasenas.
 *
 * NOTA PARA LA ENTREGA: se usa SHA-256 sin sal porque es lo que guarda el
 * script de datos iniciales y mantiene el prototipo simple. Para un sistema
 * en produccion habria que cambiar a BCrypt o Argon2 con sal por usuario;
 * el unico punto que habria que tocar es esta clase.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /** Devuelve el SHA-256 en hexadecimal minusculas. */
    public static String hash(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }

    public static boolean coincide(String textoPlano, String hashGuardado) {
        if (hashGuardado == null) {
            return false;
        }
        return hashGuardado.equalsIgnoreCase(hash(textoPlano));
    }
}
