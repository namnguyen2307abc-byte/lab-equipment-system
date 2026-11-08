package com.lab.lab_equipment_system.features.auth.controller;

import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.features.auth.dto.*;
import com.lab.lab_equipment_system.features.auth.service.AccountService;
import com.lab.lab_equipment_system.features.auth.service.UserService;
import com.lab.lab_equipment_system.shared.enums.UserRole;
import com.lab.lab_equipment_system.shared.exception.AccountRuleException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Màn hình quản lý tài khoản và phân quyền. Chỉ ADMIN (xem SecurityConfig). */
@Controller
@RequestMapping("/admin/accounts")
public class AccountController {
    private final AccountService accounts;
    private final UserService userService;

    public AccountController(AccountService accounts, UserService userService) {
        this.accounts = accounts;
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public UserResponse currentUser() { return userService.getCurrentUser(); }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "role", required = false) UserRole role, Model model) {
        model.addAttribute("pageTitle", "Quản lý tài khoản");
        model.addAttribute("activeMenu", "accounts");
        model.addAttribute("accounts", accounts.search(q, role));
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("query", q);
        model.addAttribute("selectedRole", role);
        return "admin/account-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("form")) model.addAttribute("form", new CreateAccountRequest());
        return formPage(model, "Tạo tài khoản", null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") CreateAccountRequest form, BindingResult binding,
                         Model model, RedirectAttributes redirect) {
        if (!binding.hasErrors()) {
            try {
                accounts.create(form);
                redirect.addFlashAttribute("successMessage", "Đã tạo tài khoản " + form.getUsername().trim() + ".");
                return "redirect:/admin/accounts";
            } catch (AccountRuleException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }
        return formPage(model, "Tạo tài khoản", null);
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            UserResponse u = accounts.get(id);
            if (!model.containsAttribute("form")) {
                UpdateAccountRequest f = new UpdateAccountRequest();
                f.setFullName(u.getFullName()); f.setEmail(u.getEmail());
                f.setPhone(u.getPhone()); f.setRole(u.getRole());
                model.addAttribute("form", f);
            }
            return formPage(model, "Sửa tài khoản", u);
        } catch (AccountRuleException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/accounts";
        }
    }

    @PostMapping("/{id:\\d+}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") UpdateAccountRequest form,
                         BindingResult binding, Principal principal, Model model, RedirectAttributes redirect) {
        try {
            if (!binding.hasErrors()) {
                accounts.update(id, form, principal.getName());
                redirect.addFlashAttribute("successMessage", "Đã cập nhật tài khoản.");
                return "redirect:/admin/accounts";
            }
        } catch (AccountRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }
        try {
            return formPage(model, "Sửa tài khoản", accounts.get(id));
        } catch (AccountRuleException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/accounts";
        }
    }

    @PostMapping("/{id:\\d+}/active")
    public String setActive(@PathVariable Long id, @RequestParam("active") boolean active,
                            Principal principal, RedirectAttributes redirect) {
        try {
            accounts.setActive(id, active, principal.getName());
            redirect.addFlashAttribute("successMessage", active ? "Đã mở khóa tài khoản." : "Đã khóa tài khoản.");
        } catch (AccountRuleException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/{id:\\d+}/reset-password")
    public String resetPassword(@PathVariable Long id, @Valid ResetPasswordRequest request,
                                BindingResult binding, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("errorMessage", binding.getFieldError().getDefaultMessage());
            return "redirect:/admin/accounts/" + id + "/edit";
        }
        try {
            accounts.resetPassword(id, request);
            redirect.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu.");
        } catch (AccountRuleException ex) {
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/accounts";
        }
        return "redirect:/admin/accounts/" + id + "/edit";
    }

    private String formPage(Model model, String title, UserResponse editing) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("activeMenu", "accounts");
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("editing", editing);
        return "admin/account-form";
    }
}
