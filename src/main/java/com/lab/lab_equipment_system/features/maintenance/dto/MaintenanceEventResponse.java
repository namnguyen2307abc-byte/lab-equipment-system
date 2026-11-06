package com.lab.lab_equipment_system.features.maintenance.dto;

import com.lab.lab_equipment_system.shared.enums.MaintenanceEventType;
import com.lab.lab_equipment_system.shared.enums.MaintenanceStatus;

public class MaintenanceEventResponse {
    private String actorName;
    private MaintenanceEventType type;
    private MaintenanceStatus status;
    private String note;
    private String createdAt;

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public MaintenanceEventType getType() {
        return type;
    }

    public void setType(MaintenanceEventType type) {
        this.type = type;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
