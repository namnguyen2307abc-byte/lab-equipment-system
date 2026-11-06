package com.lab.lab_equipment_system.shared.enums;
public enum UserRole {
    STUDENT("Sinh viên"),
    STAFF("Nhân viên"),
    ADMIN("Quản trị viên");
    private final String label;
    UserRole(String label) { this.label = label; }
    public String getLabel() { return label; }
    public boolean isStaff() { return this == STAFF || this == ADMIN; }
}

