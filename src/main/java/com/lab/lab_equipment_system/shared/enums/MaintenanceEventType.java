package com.lab.lab_equipment_system.shared.enums;
public enum MaintenanceEventType {
    CREATED("Tạo phiếu"),
    ASSESSED("Tiếp nhận phiếu"),
    PROGRESS_UPDATED("Cập nhật tiến độ"),
    COMPLETED("Hoàn tất bảo trì");
    private final String label;
    MaintenanceEventType(String label) { this.label = label; }
    public String getLabel() { return label; }
}

