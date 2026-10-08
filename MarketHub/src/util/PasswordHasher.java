package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordHasher Utility
 * Demonstrates secure SHA-256 cryptographic hashing with application-level salting.
 * Prevents plain-text credential persistence.
 */
public class PasswordHasher {

    private static final String SALT = "MarketHubSecretSalt_2026";

    private PasswordHasher() {}

    public static String hash(String plainPassword) {
        if (plainPassword == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salted = plainPassword + SALT;
            byte[] encodedHash = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm missing from JVM", e);
        }
    }

    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        String hashed = hash(plainPassword);
        return hashed.equals(storedHash);
    }
}
