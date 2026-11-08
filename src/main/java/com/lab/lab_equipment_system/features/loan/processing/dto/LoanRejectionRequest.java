package com.lab.lab_equipment_system.features.loan.processing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoanRejectionRequest {
    @NotBlank(message = "Vui lòng nhập lý do từ chối.")
    @Size(max = 500, message = "Lý do từ chối tối đa 500 ký tự.")
    private String rejectReason;
    public LoanRejectionRequest() {}
    public LoanRejectionRequest(String rejectReason) { this.rejectReason = rejectReason; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
