package com.lab.lab_equipment_system.features.auth.dto;

import com.lab.lab_equipment_system.shared.enums.UserRole;
import jakarta.validation.constraints.*;

public class CreateAccountRequest {
    @NotBlank(message = "Vui lòng nhập tên đăng nhập.")
    @Size(max = 100, message = "Tên đăng nhập tối đa 100 ký tự.")
    @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "Tên đăng nhập chỉ gồm chữ, số, dấu chấm, gạch dưới, gạch ngang.")
    private String username;

    @NotBlank(message = "Vui lòng nhập họ tên.")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự.")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập email.")
    @Email(message = "Email không hợp lệ.")
    @Size(max = 255, message = "Email tối đa 255 ký tự.")
    private String email;

    @Pattern(regexp = "^[0-9+ ]{0,20}$", message = "Số điện thoại không hợp lệ.")
    private String phone;

    @NotNull(message = "Vui lòng chọn vai trò.")
    private UserRole role = UserRole.STUDENT;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    @Size(min = 6, max = 72, message = "Mật khẩu từ 6 đến 72 ký tự.")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
