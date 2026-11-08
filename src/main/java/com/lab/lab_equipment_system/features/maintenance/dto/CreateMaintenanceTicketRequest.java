package com.lab.lab_equipment_system.features.maintenance.dto;
import jakarta.validation.constraints.*;
public class CreateMaintenanceTicketRequest {
    @NotNull(message = "Vui lòng chọn thiết bị.")
    @Positive
    private Long equipmentId;
    @Positive
    private Long loanRequestId;
    @NotBlank(message = "Vui lòng mô tả sự cố.")
    @Size(min = 10, max = 1000, message = "Mô tả cần từ 10 đến 1000 ký tự.")
    private String description;
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public Long getLoanRequestId() { return loanRequestId; }
    public void setLoanRequestId(Long loanRequestId) { this.loanRequestId = loanRequestId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

