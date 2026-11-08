package com.lab.lab_equipment_system.features.auth.service;

import com.lab.lab_equipment_system.features.auth.dto.CreateAccountRequest;
import com.lab.lab_equipment_system.features.auth.dto.UpdateAccountRequest;
import com.lab.lab_equipment_system.repository.UserRepository;
import com.lab.lab_equipment_system.shared.entity.User;
import com.lab.lab_equipment_system.shared.enums.UserRole;
import com.lab.lab_equipment_system.shared.exception.AccountRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTests {
    @Mock private UserRepository users;
    @Mock private PasswordEncoder encoder;
    private AccountServiceImpl service;
    private User admin;

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl(users, encoder);
        admin = new User("admin", "hash", "Quản trị viên", "admin@fpt.edu.vn", null, UserRole.ADMIN);
        admin.setId(1L);
    }

    private CreateAccountRequest createRequest() {
        CreateAccountRequest r = new CreateAccountRequest();
        r.setUsername("newuser"); r.setFullName("Người Mới"); r.setEmail("new@fpt.edu.vn");
        r.setRole(UserRole.STUDENT); r.setPassword("123456");
        return r;
    }

    @Test @DisplayName("Tạo tài khoản: mật khẩu được mã hóa BCrypt trước khi lưu")
    void createEncodesPassword() {
        when(encoder.encode("123456")).thenReturn("ENCODED");
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        service.create(createRequest());
        verify(users).save(argThat(u -> "ENCODED".equals(u.getPasswordHash()) && u.getRole() == UserRole.STUDENT));
    }

    @Test @DisplayName("Tạo tài khoản: trùng tên đăng nhập bị từ chối")
    void createRejectsDuplicateUsername() {
        when(users.existsByUsernameIgnoreCase("newuser")).thenReturn(true);
        assertThrows(AccountRuleException.class, () -> service.create(createRequest()));
        verify(users, never()).save(any());
    }

    @Test @DisplayName("Không thể tự khóa tài khoản của chính mình")
    void cannotDeactivateSelf() {
        when(users.findById(1L)).thenReturn(Optional.of(admin));
        assertThrows(AccountRuleException.class, () -> service.setActive(1L, false, "admin"));
    }

    @Test @DisplayName("Không thể khóa quản trị viên đang hoạt động cuối cùng")
    void cannotDeactivateLastAdmin() {
        when(users.findById(1L)).thenReturn(Optional.of(admin));
        when(users.countByRoleAndActiveTrue(UserRole.ADMIN)).thenReturn(1L);
        assertThrows(AccountRuleException.class, () -> service.setActive(1L, false, "other"));
    }

    @Test @DisplayName("Đổi vai trò người dùng khác thành công")
    void changeRoleOfAnotherUser() {
        User student = new User("sv", "h", "SV", "sv@fpt.edu.vn", null, UserRole.STUDENT);
        student.setId(2L);
        when(users.findById(2L)).thenReturn(Optional.of(student));
        when(users.findByEmailIgnoreCase("sv@fpt.edu.vn")).thenReturn(Optional.of(student));
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        UpdateAccountRequest r = new UpdateAccountRequest();
        r.setFullName("SV"); r.setEmail("sv@fpt.edu.vn"); r.setRole(UserRole.STAFF);
        service.update(2L, r, "admin");
        assertEquals(UserRole.STAFF, student.getRole());
    }

    @Test @DisplayName("Không thể tự đổi vai trò của chính mình")
    void cannotChangeOwnRole() {
        when(users.findById(1L)).thenReturn(Optional.of(admin));
        when(users.findByEmailIgnoreCase("admin@fpt.edu.vn")).thenReturn(Optional.of(admin));
        UpdateAccountRequest r = new UpdateAccountRequest();
        r.setFullName("Quản trị viên"); r.setEmail("admin@fpt.edu.vn"); r.setRole(UserRole.STUDENT);
        assertThrows(AccountRuleException.class, () -> service.update(1L, r, "admin"));
    }
}
