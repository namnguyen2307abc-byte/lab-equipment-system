package com.lab.lab_equipment_system.auth.controller;

import com.lab.lab_equipment_system.auth.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/student/equipment";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Đã đăng xuất thành công.");
        }
        model.addAttribute("users", userRepository.findAll());
        return "auth/login";
    }

    /**
     * Tiện ích chuyển đổi người dùng nhanh khi test / demo trong phòng lab
     */
    @GetMapping("/switch-user/{username}")
    public String switchUser(@PathVariable("username") String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        });
        return "redirect:/student/equipment";
    }
}
