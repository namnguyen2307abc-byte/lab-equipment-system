package com.lab.lab_equipment_system.features.maintenance.service;

import com.lab.lab_equipment_system.features.maintenance.dto.CurrentUserResponse;
import com.lab.lab_equipment_system.features.maintenance.repository.MaintenanceUserRepository;
import com.lab.lab_equipment_system.shared.entity.User;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!ui-preview")
public class MaintenanceIdentityServiceImpl implements MaintenanceIdentityService {
    private final MaintenanceUserRepository users;

    public MaintenanceIdentityServiceImpl(MaintenanceUserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Vui lòng đăng nhập để sử dụng chức năng bảo trì.");
        }
        User user = users.findByUsernameIgnoreCase(authentication.getName())
            .or(() -> users.findByEmailIgnoreCase(authentication.getName()))
            .orElseThrow(() -> new AccessDeniedException("Tài khoản đăng nhập chưa được liên kết với hồ sơ người dùng."));
        if (!user.isActive()) {
            throw new AccessDeniedException("Tài khoản đã bị ngừng hoạt động.");
        }
        return toDto(user);
    }

    private CurrentUserResponse toDto(User user) {
        CurrentUserResponse response = new CurrentUserResponse();
        response.setId(user.getId());
        response.setName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}
