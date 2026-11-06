package com.lab.lab_equipment_system.shared.enums;
public enum MaintenanceStatus {
    OPEN("Chờ tiếp nhận"),
    IN_PROGRESS("Đang xử lý"),
    RESOLVED("Đã sửa xong"),
    UNREPAIRABLE("Không sửa được");
    private final String label;
    MaintenanceStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    public boolean isClosed() { return this == RESOLVED || this == UNREPAIRABLE; }
}

