package com.lab.lab_equipment_system.features.loan.processing.exception;

import com.lab.lab_equipment_system.features.loan.processing.controller.LoanProcessingController;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = LoanProcessingController.class)
public class LoanProcessingExceptionHandler {
    @ExceptionHandler(LoanProcessingRuleException.class)
    public String conflict(LoanProcessingRuleException ex, HttpServletResponse response, Model model) {
        return error(409, ex.getMessage(), response, model);
    }
    @ExceptionHandler(LoanProcessingValidationException.class)
    public String invalid(LoanProcessingValidationException ex, HttpServletResponse response, Model model) {
        return error(400, ex.getMessage(), response, model);
    }
    @ExceptionHandler({CannotAcquireLockException.class, ObjectOptimisticLockingFailureException.class})
    public String concurrent(Exception ex, HttpServletResponse response, Model model) {
        return error(409, "Phiếu đang được xử lý ở nơi khác. Vui lòng tải lại trang.", response, model);
    }
    private String error(int code, String message, HttpServletResponse response, Model model) {
        response.setStatus(code);
        model.addAttribute("pageTitle", "Không thể xử lý phiếu");
        model.addAttribute("activeMenu", "loan-processing");
        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message);
        return "error/message";
    }
}
