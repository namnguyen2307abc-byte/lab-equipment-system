package com.lab.lab_equipment_system.loan.entity;

public enum LoanStatus {
    PENDING("Chờ duyệt", "badge bg-warning text-dark"),
    APPROVED("Đã duyệt", "badge bg-info text-dark"),
    HANDED_OVER("Đã bàn giao", "badge bg-primary"),
    RETURNED("Đã trả", "badge bg-success"),
    REJECTED("Từ chối", "badge bg-danger"),
    CANCELLED("Đã hủy", "badge bg-secondary");

    private final String displayName;
    private final String badgeClass;

    LoanStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
