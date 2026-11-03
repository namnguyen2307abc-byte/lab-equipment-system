package com.lab.lab_equipment_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CancelLoanRequestDto {

    @NotNull(message = "ID phiếu mượn không hợp lệ")
    private Long loanId;

    @NotBlank(message = "Vui lòng nhập lý do hủy phiếu mượn")
    @Size(max = 500, message = "Lý do hủy không vượt quá 500 ký tự")
    private String cancelReason;

    public CancelLoanRequestDto() {
    }

    public CancelLoanRequestDto(Long loanId, String cancelReason) {
        this.loanId = loanId;
        this.cancelReason = cancelReason;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
