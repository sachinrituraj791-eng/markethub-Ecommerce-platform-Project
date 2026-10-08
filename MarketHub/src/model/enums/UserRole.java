package model.enums;

/**
 * UserRole Enum
 * Represents the three core actor roles evaluated in MarketHub.
 * Part of Core Java Concepts (10 Marks).
 */
public enum UserRole {
    ADMIN("Administrator with platform-wide governance rights"),
    SELLER("Merchant with catalog and inventory management rights"),
    BUYER("Consumer with browsing, purchasing, and review rights");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromString(String roleStr) {
        if (roleStr == null) return BUYER;
        for (UserRole role : UserRole.values()) {
            if (role.name().equalsIgnoreCase(roleStr.trim())) {
                return role;
            }
        }
        return BUYER;
    }
}
