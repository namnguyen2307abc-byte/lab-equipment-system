package com.lab.lab_equipment_system.entity;

public enum NotificationType {
    APPROVAL("Phê duyệt phiếu mượn", "fas fa-check-circle text-success"),
    REJECTION("Từ chối phiếu mượn", "fas fa-times-circle text-danger"),
    PICKUP_READY("Sẵn sàng nhận thiết bị", "fas fa-box-open text-primary"),
    DUE_REMINDER("Nhắc nhở hạn trả thiết bị", "fas fa-clock text-warning"),
    CANCELLED("Hủy phiếu mượn", "fas fa-ban text-secondary"),
    INFO("Thông báo hệ thống", "fas fa-info-circle text-info");

    private final String defaultTitle;
    private final String iconClass;

    NotificationType(String defaultTitle, String iconClass) {
        this.defaultTitle = defaultTitle;
        this.iconClass = iconClass;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public String getIconClass() {
        return iconClass;
    }
}
