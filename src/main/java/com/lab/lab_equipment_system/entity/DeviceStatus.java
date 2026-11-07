package com.lab.lab_equipment_system.entity;

public enum DeviceStatus {
    AVAILABLE("Kh\u1ea3 d\u1ee5ng"),
    BORROWED("\u0110ang m\u01b0\u1ee3n"),
    MAINTENANCE("\u0110ang b\u1ea3o tr\u00ec"),
    RETIRED("Ng\u1eebng s\u1eed d\u1ee5ng");

    private final String displayName;

    DeviceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLabel() {
        return displayName;
    }
}
