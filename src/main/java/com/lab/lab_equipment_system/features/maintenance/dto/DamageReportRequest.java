package com.lab.lab_equipment_system.features.maintenance.dto;
import jakarta.validation.constraints.*;
public class DamageReportRequest {
    @NotNull(message = "Vui lòng chọn phiếu mượn.")
    @Positive
    private Long loanRequestId;
    @NotBlank(message = "Vui lòng mô tả sự cố.")
    @Size(min = 10, max = 1000, message = "Mô tả cần từ 10 đến 1000 ký tự.")
    private String description;
    public Long getLoanRequestId() { return loanRequestId; }
    public void setLoanRequestId(Long loanRequestId) { this.loanRequestId = loanRequestId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

