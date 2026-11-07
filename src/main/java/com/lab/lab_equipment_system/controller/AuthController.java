package com.lab.lab_equipment_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AuthController {

    @GetMapping("/")
    public String index() {
        return "redirect:/student/equipment";
    }

    @GetMapping("/ui")
    public String ui(@RequestParam(value = "section", required = false) String section) {
        if ("loans".equalsIgnoreCase(section)) {
            return "redirect:/student/loans";
        }
        if ("maintenance".equalsIgnoreCase(section)) {
            return "redirect:/maintenance";
        }
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
        return "auth/login";
    }

}
