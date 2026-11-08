package com.lab.lab_equipment_system.features.auth.dto;

import com.lab.lab_equipment_system.shared.validation.Utf8ByteLength;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank(message = "Vui lòng nhập mật khẩu mới.")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự.")
    @Utf8ByteLength
    private String newPassword;

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
