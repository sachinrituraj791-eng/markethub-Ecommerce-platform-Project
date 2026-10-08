package model.enums;

/**
 * PaymentStatus Enum
 * Tracks financial settlement state for transactions.
 */
public enum PaymentStatus {
    PENDING("Awaiting payment capture"),
    PAID("Payment authorized and settled successfully"),
    FAILED("Payment declined by payment gateway");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentStatus fromString(String val) {
        if (val == null) return PENDING;
        for (PaymentStatus p : PaymentStatus.values()) {
            if (p.name().equalsIgnoreCase(val.trim())) {
                return p;
            }
        }
        return PENDING;
    }
}
