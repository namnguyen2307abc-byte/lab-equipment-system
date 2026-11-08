package com.lab.lab_equipment_system.features.equipment.dto;

import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;
import jakarta.validation.constraints.NotNull;

public class DeviceStatusUpdateRequest {

    @NotNull(message = "Vui lòng chọn tình trạng thiết bị")
    private EquipmentStatus status;

    private String note;

    public DeviceStatusUpdateRequest() {
    }

    public DeviceStatusUpdateRequest(EquipmentStatus status) {
        this.status = status;
    }

    public DeviceStatusUpdateRequest(EquipmentStatus status, String note) {
        this.status = status;
        this.note = note;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
