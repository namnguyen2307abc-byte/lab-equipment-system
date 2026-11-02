package com.lab.lab_equipment_system.loan.request.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class CreateLoanRequestDto {

    @NotNull(message = "Vui lòng chọn thiết bị muốn mượn")
    private Long deviceId;

    @NotNull(message = "Vui lòng chọn thời gian dự kiến trả")
    @Future(message = "Thời gian dự kiến trả phải sau thời điểm hiện tại")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueAt;

    @NotBlank(message = "Vui lòng nhập mục đích mượn thiết bị")
    @Size(max = 500, message = "Mục đích mượn không vượt quá 500 ký tự")
    private String purpose;

    @Size(max = 500, message = "Ghi chú không vượt quá 500 ký tự")
    private String notes;

    public CreateLoanRequestDto() {
    }

    public CreateLoanRequestDto(Long deviceId, LocalDateTime dueAt, String purpose, String notes) {
        this.deviceId = deviceId;
        this.dueAt = dueAt;
        this.purpose = purpose;
        this.notes = notes;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
