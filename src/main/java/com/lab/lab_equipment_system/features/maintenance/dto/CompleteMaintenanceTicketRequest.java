package com.lab.lab_equipment_system.features.maintenance.dto;
import jakarta.validation.constraints.*;
import com.lab.lab_equipment_system.shared.enums.MaintenanceStatus;
public class CompleteMaintenanceTicketRequest {
    @NotNull
    @PositiveOrZero
    private Long version;
    @NotNull(message = "Vui lòng chọn kết quả bảo trì.")
    private MaintenanceStatus outcome;
    @NotBlank(message = "Vui lòng ghi kết quả xử lý.")
    @Size(min = 10, max = 1000, message = "Kết quả cần từ 10 đến 1000 ký tự.")
    private String note;
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public MaintenanceStatus getOutcome() { return outcome; }
    public void setOutcome(MaintenanceStatus outcome) { this.outcome = outcome; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

