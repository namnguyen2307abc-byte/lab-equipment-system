package com.lab.lab_equipment_system.features.maintenance.dto;
import jakarta.validation.constraints.*;
public class MaintenanceTicketActionRequest {
    @NotNull
    @PositiveOrZero
    private Long version;
    @NotBlank(message = "Vui lòng nhập ghi chú xử lý.")
    @Size(min = 10, max = 1000, message = "Ghi chú cần từ 10 đến 1000 ký tự.")
    private String note;
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

