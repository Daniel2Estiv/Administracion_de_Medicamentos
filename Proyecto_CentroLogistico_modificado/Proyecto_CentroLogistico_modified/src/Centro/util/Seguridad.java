package Centro.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidades de seguridad: hashing de contraseñas con SHA-256.
 * <p>
 * <strong>Nota sobre el algoritmo:</strong> SHA-256 sin sal es un esquema débil
 * para almacenar contraseñas. Se mantiene aquí únicamente por compatibilidad
 * con la base de datos existente. La verificación se hace con comparación
 * constante en el tiempo (resistente a {@code timing attacks}).
 * <p>
 * Para una migración futura recomendada (PBKDF2 / Argon2 / BCrypt) habría que
 * regenerar los hashes existentes; por eso queda fuera del alcance de esta
 * versión, donde se exige no tocar la base de datos.
 */
public final class Seguridad {

    private static final Logger LOGGER = Logger.getLogger(Seguridad.class.getName());

    private Seguridad() {}

    /**
     * Devuelve el hash SHA-256 hexadecimal del texto recibido.
     *
     * @param texto contraseña en claro (no nula)
     * @return hash hex en minúsculas
     * @throws IllegalArgumentException si {@code texto} es {@code null}
     * @throws IllegalStateException    si la JVM no soporta SHA-256 (extremadamente improbable)
     */
    public static String hashSHA256(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("El texto a hashear no puede ser null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 es parte del estándar Java SE — no debería ocurrir nunca.
            LOGGER.log(Level.SEVERE, "Algoritmo SHA-256 no disponible", e);
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }

    /**
     * Verifica si {@code textoPlano} corresponde a {@code hashEsperado} usando
     * comparación de tiempo constante para mitigar ataques de tiempo.
     *
     * @return {@code true} si coinciden, {@code false} si alguno es {@code null}
     *         o no coinciden.
     */
    public static boolean verificar(String textoPlano, String hashEsperado) {
        if (textoPlano == null || hashEsperado == null) return false;
        String hashGenerado;
        try {
            hashGenerado = hashSHA256(textoPlano);
        } catch (RuntimeException e) {
            return false;
        }
        // MessageDigest.isEqual hace una comparación de tiempo constante.
        byte[] a = hashGenerado.getBytes(StandardCharsets.UTF_8);
        byte[] b = hashEsperado.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
