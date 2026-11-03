package com.lab.lab_equipment_system.entity;

public enum DeviceStatus {
    AVAILABLE("Khả dụng"),
    BORROWED("Đang mượn"),
    MAINTENANCE("Đang bảo trì"),
    RETIRED("Ngừng sử dụng");

    private final String displayName;

    DeviceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
