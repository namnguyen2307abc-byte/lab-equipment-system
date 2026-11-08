package com.lab.lab_equipment_system.features.auth.service;

import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.features.auth.dto.*;
import com.lab.lab_equipment_system.shared.enums.UserRole;

import java.util.List;

/** Quản lý tài khoản và phân quyền người dùng (chỉ ADMIN được gọi). */
public interface AccountService {
    List<UserResponse> search(String keyword, UserRole role);
    UserResponse get(Long id);
    UserResponse create(CreateAccountRequest request);
    UserResponse update(Long id, UpdateAccountRequest request, String actingUsername);
    UserResponse setActive(Long id, boolean active, String actingUsername);
    void resetPassword(Long id, ResetPasswordRequest request);
}
