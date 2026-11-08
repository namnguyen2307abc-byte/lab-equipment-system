package com.lab.lab_equipment_system.shared.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SharedWebExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public String notFound(ResourceNotFoundException ex, HttpServletResponse response, Model model) {
        response.setStatus(404);
        model.addAttribute("pageTitle", "Không tìm thấy dữ liệu");
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorMessage", ex.getMessage());
        if (!model.containsAttribute("activeMenu")) model.addAttribute("activeMenu", "overview");
        return "error/message";
    }
}
