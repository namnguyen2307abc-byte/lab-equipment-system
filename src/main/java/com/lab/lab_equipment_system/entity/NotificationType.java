package com.lab.lab_equipment_system.entity;

public enum NotificationType {
    APPROVAL("Ph\u00ea duy\u1ec7t phi\u1ebfu m\u01b0\u1ee3n", "fas fa-check-circle text-success"),
    REJECTION("T\u1eeb ch\u1ed1i phi\u1ebfu m\u01b0\u1ee3n", "fas fa-times-circle text-danger"),
    PICKUP_READY("S\u1eb5n s\u00e0ng nh\u1eadn thi\u1ebft b\u1ecb", "fas fa-box-open text-primary"),
    DUE_REMINDER("Nh\u1eafc nh\u1edf h\u1ea1n tr\u1ea3 thi\u1ebft b\u1ecb", "fas fa-clock text-warning"),
    CANCELLED("H\u1ee7y phi\u1ebfu m\u01b0\u1ee3n", "fas fa-ban text-secondary"),
    INFO("Th\u00f4ng b\u00e1o h\u1ec7 th\u1ed1ng", "fas fa-info-circle text-info");

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
