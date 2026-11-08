package com.lab.lab_equipment_system.features.auth.service;

import com.lab.lab_equipment_system.shared.dto.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserResponse getCurrentUser();
}
