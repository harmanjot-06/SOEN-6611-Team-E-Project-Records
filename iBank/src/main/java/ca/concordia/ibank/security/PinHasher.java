package ca.concordia.ibank.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashes and verifies PINs with PBKDF2-HMAC-SHA256 and a random salt, reusing the Java
 * Cryptography Architecture. PINs are never stored or logged in clear text.
 */
public final class PinHasher {

  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final int ITERATIONS = 120_000;
  private static final int SALT_BYTES = 16;
  private static final int KEY_BITS = 256;
  private static final SecureRandom RANDOM = new SecureRandom();

  private PinHasher() {
  }

  /** Returns {@code iterations:salt:hash}, with salt and hash in Base64. */
  public static String hash(char[] pin) {
    byte[] salt = new byte[SALT_BYTES];
    RANDOM.nextBytes(salt);
    byte[] hash = derive(pin, salt, ITERATIONS);
    Base64.Encoder encoder = Base64.getEncoder();
    return ITERATIONS + ":" + encoder.encodeToString(salt) + ":" + encoder.encodeToString(hash);
  }

  /** Convenience for test data and seeding; avoid in production paths that hold char arrays. */
  public static String hash(String pin) {
    return hash(pin.toCharArray());
  }

  /** Returns true if {@code pin} matches {@code stored}; the comparison takes constant time. */
  public static boolean verify(char[] pin, String stored) {
    String[] parts = stored.split(":");
    if (parts.length != 3) {
      return false;
    }
    Base64.Decoder decoder = Base64.getDecoder();
    byte[] salt = decoder.decode(parts[1]);
    byte[] expected = decoder.decode(parts[2]);
    byte[] actual = derive(pin, salt, Integer.parseInt(parts[0]));
    return MessageDigest.isEqual(expected, actual);
  }

  private static byte[] derive(char[] pin, byte[] salt, int iterations) {
    PBEKeySpec spec = new PBEKeySpec(pin, salt, iterations, KEY_BITS);
    try {
      return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("PBKDF2 is not available in this Java runtime", e);
    } finally {
      spec.clearPassword();
    }
  }

  /** Returns a short fingerprint of a value for logs, never the value itself. */
  public static String fingerprint(String value) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(value.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(digest).substring(0, 8);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
