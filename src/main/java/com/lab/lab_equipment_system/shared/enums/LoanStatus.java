package com.lab.lab_equipment_system.shared.enums;
public enum LoanStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Từ chối"),
    CANCELLED("Đã hủy"),
    BORROWED("Đang mượn"),
    RETURN_REQUESTED("Yêu cầu trả"),
    RETURNED("Đã trả");
    private final String label;
    LoanStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}

