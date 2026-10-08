package util;

import exception.ValidationException;
import java.util.regex.Pattern;

/**
 * ValidationUtils
 * Centralizes input sanitation and validation across the Controller and Service layers.
 */
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,30}$"
    );

    private ValidationUtils() {}

    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required and cannot be empty.");
        }
    }

    public static void validateEmail(String email) {
        validateNotBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void validateUsername(String username) {
        validateNotBlank(username, "Username");
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new ValidationException("Username must be 3-30 characters alphanumeric or underscore.");
        }
    }

    public static void validatePassword(String password) {
        validateNotBlank(password, "Password");
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters in length.");
        }
    }

    public static void validatePositive(double number, String fieldName) {
        if (number <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    public static void validateNonNegative(int number, String fieldName) {
        if (number < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }
}
