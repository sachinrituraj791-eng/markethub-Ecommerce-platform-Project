package model.enums;

/**
 * OrderStatus Enum
 * Tracks the complete lifecycle of customer orders.
 * Supported state transitions:
 * PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED (or CANCELLED)
 */
public enum OrderStatus {
    PENDING("Order received, pending payment verification"),
    CONFIRMED("Payment verified, order awaiting warehouse fulfillment"),
    PROCESSING("Items are being packaged by seller"),
    SHIPPED("Package handed over to carrier with tracking"),
    DELIVERED("Successfully delivered to buyer"),
    CANCELLED("Order voided and inventory restored");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        if (this == DELIVERED || this == CANCELLED) {
            return false; // Terminal states
        }
        if (newStatus == CANCELLED) {
            return this != SHIPPED;
        }
        return true;
    }

    public static OrderStatus fromString(String statusStr) {
        if (statusStr == null) return PENDING;
        for (OrderStatus s : OrderStatus.values()) {
            if (s.name().equalsIgnoreCase(statusStr.trim())) {
                return s;
            }
        }
        return PENDING;
    }
}
