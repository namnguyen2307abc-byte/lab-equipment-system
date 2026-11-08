package com.lab.lab_equipment_system.features.auth.service;

import com.lab.lab_equipment_system.shared.validation.PasswordRules;

import com.lab.lab_equipment_system.shared.mapper.UserMapper;
import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.features.auth.dto.*;
import com.lab.lab_equipment_system.features.auth.repository.UserRepository;
import com.lab.lab_equipment_system.shared.entity.User;
import com.lab.lab_equipment_system.shared.enums.UserRole;
import com.lab.lab_equipment_system.shared.exception.AccountRuleException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AccountSessionService sessions;

    public AccountServiceImpl(UserRepository users, PasswordEncoder passwordEncoder, AccountSessionService sessions) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.sessions = sessions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> search(String keyword, UserRole role) {
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return users.searchUsers(kw, role).stream().map(UserMapper::toUserResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserMapper.toUserResponse(find(id));
    }

    @Override
    @Transactional
    public UserResponse create(CreateAccountRequest r) {
        PasswordRules.requireValid(r.getPassword());
        String username = r.getUsername().trim();
        String email = r.getEmail().trim();
        if (users.existsByUsernameIgnoreCase(username)) throw new AccountRuleException("Tên đăng nhập đã tồn tại.");
        if (users.existsByEmailIgnoreCase(email)) throw new AccountRuleException("Email đã được sử dụng.");
        User user = new User(username, passwordEncoder.encode(r.getPassword()), r.getFullName().trim(),
            email, blankToNull(r.getPhone()), r.getRole());
        return UserMapper.toUserResponse(users.save(user));
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateAccountRequest r, String actingUsername) {
        User user = find(id);
        String email = r.getEmail().trim();
        users.findByEmailIgnoreCase(email).filter(o -> !o.getId().equals(id))
            .ifPresent(o -> { throw new AccountRuleException("Email đã được sử dụng."); });
        if (user.getRole() != r.getRole()) {
            if (isSelf(user, actingUsername)) throw new AccountRuleException("Không thể tự đổi vai trò của chính mình.");
            guardLastAdmin(user);
        }
        boolean revoke = user.getRole() != r.getRole()
            || (user.getUsername() == null && !user.getEmail().equalsIgnoreCase(email));
        user.setFullName(r.getFullName().trim());
        user.setEmail(email);
        user.setPhone(blankToNull(r.getPhone()));
        user.setRole(r.getRole());
        if (revoke) sessions.revokeUserSessions(id);
        return UserMapper.toUserResponse(users.save(user));
    }

    @Override
    @Transactional
    public UserResponse setActive(Long id, boolean active, String actingUsername) {
        User user = find(id);
        if (!active) {
            if (isSelf(user, actingUsername)) throw new AccountRuleException("Không thể tự khóa tài khoản của chính mình.");
            guardLastAdmin(user);
        }
        if (user.isActive() != active) sessions.revokeUserSessions(id);
        user.setActive(active);
        return UserMapper.toUserResponse(users.save(user));
    }

    @Override
    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest r) {
        User user = find(id);
        PasswordRules.requireValid(r.getNewPassword());
        user.setPasswordHash(passwordEncoder.encode(r.getNewPassword()));
        sessions.revokeUserSessions(id);
        users.save(user);
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> new AccountRuleException("Không tìm thấy tài khoản."));
    }

    private boolean isSelf(User user, String actingUsername) {
        return actingUsername != null && (actingUsername.equalsIgnoreCase(user.getUsername())
            || actingUsername.equalsIgnoreCase(user.getEmail()));
    }

    /** Luôn phải còn ít nhất một ADMIN đang hoạt động. */
    private void guardLastAdmin(User user) {
        if (user.getRole() == UserRole.ADMIN && user.isActive()
                && users.countByRoleAndActiveTrue(UserRole.ADMIN) <= 1) {
            throw new AccountRuleException("Hệ thống phải còn ít nhất một quản trị viên đang hoạt động.");
        }
    }

    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
