package com.lab.lab_equipment_system.shared.mapper;

import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.shared.entity.User;

public final class UserMapper {
    private UserMapper() { }

    public static UserResponse toUserResponse(User entity) {
        if (entity == null) return null;
        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setUsername(entity.getUsername());
        response.setFullName(entity.getFullName());
        response.setEmail(entity.getEmail());
        response.setPhone(entity.getPhone());
        response.setRole(entity.getRole());
        response.setActive(entity.isActive());
        return response;
    }
}
