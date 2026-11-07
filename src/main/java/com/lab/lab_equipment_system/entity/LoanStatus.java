package com.lab.lab_equipment_system.entity;

public enum LoanStatus {
    PENDING("Ch\u1edd duy\u1ec7t", "badge bg-warning text-dark"),
    APPROVED("\u0110\u00e3 duy\u1ec7t", "badge bg-info text-dark"),
    HANDED_OVER("\u0110\u00e3 b\u00e0n giao", "badge bg-primary"),
    RETURNED("\u0110\u00e3 tr\u1ea3", "badge bg-success"),
    REJECTED("T\u1eeb ch\u1ed1i", "badge bg-danger"),
    CANCELLED("\u0110\u00e3 h\u1ee7y", "badge bg-secondary");

    private final String displayName;
    private final String badgeClass;

    LoanStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLabel() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
