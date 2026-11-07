package com.lab.lab_equipment_system.features.equipment.entity;

/**
 * Tình trạng / trạng thái của thiết bị vật lý trong phòng lab.
 */
public enum EquipmentStatus {
    AVAILABLE("Sẵn sàng", "badge-available", "Thiết bị sẵn sàng cho sinh viên mượn"),
    BORROWED("Đang mượn", "badge-borrowed", "Thiết bị đang được sinh viên mượn"),
    UNDER_MAINTENANCE("Đang bảo trì", "badge-maintenance", "Thiết bị đang bảo dưỡng hoặc kiểm tra kỹ thuật"),
    MAINTENANCE("Đang bảo trì", "badge-maintenance", "Thiết bị đang bảo dưỡng hoặc kiểm tra kỹ thuật"),
    DAMAGED("Hỏng hóc", "badge-damaged", "Thiết bị gặp sự cố cần sửa chữa"),
    RETIRED("Ngừng sử dụng", "badge-retired", "Thiết bị đã hỏng không sửa được hoặc thanh lý"),
    LOST("Thất lạc", "badge-damaged", "Thiết bị bị thất lạc");

    private final String displayName;
    private final String badgeClass;
    private final String description;

    EquipmentStatus(String displayName, String badgeClass, String description) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Danh sách các trạng thái hiển thị trên giao diện dropdown để người dùng chọn.
     */
    public static EquipmentStatus[] displayableValues() {
        return new EquipmentStatus[]{
                AVAILABLE,
                BORROWED,
                UNDER_MAINTENANCE,
                DAMAGED,
                RETIRED,
                LOST
        };
    }
}
