package com.lab.lab_equipment_system.shared.enums;
public enum EquipmentStatus {
    AVAILABLE("Sẵn sàng"),
    ON_LOAN("Đang cho mượn"),
    UNDER_MAINTENANCE("Đang bảo trì"),
    RETIRED("Ngừng sử dụng");
    private final String label;
    EquipmentStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}

