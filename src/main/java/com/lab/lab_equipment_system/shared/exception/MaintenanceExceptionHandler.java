package com.lab.lab_equipment_system.shared.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.lab.lab_equipment_system.features.maintenance.controller.MaintenanceController;

@ControllerAdvice(assignableTypes = MaintenanceController.class)
public class MaintenanceExceptionHandler {
    @ExceptionHandler(MaintenanceNotFoundException.class)
    String notFound(MaintenanceNotFoundException e, HttpServletResponse response, Model model) {
        return error(404, e.getMessage(), response, model);
    }
    @ExceptionHandler(AccessDeniedException.class)
    String denied(AccessDeniedException e, HttpServletResponse response, Model model) {
        return error(403, e.getMessage(), response, model);
    }
    @ExceptionHandler(MaintenanceRuleException.class)
    String conflict(MaintenanceRuleException e, HttpServletResponse response, Model model) {
        return error(409, e.getMessage(), response, model);
    }
    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, CannotAcquireLockException.class})
    String concurrentUpdate(Exception e, HttpServletResponse response, Model model) {
        return error(409, "Phiếu đang được cập nhật ở nơi khác. Vui lòng tải lại và thử lại.", response, model);
    }
    private String error(int status, String message, HttpServletResponse response, Model model) {
        response.setStatus(status);
        model.addAttribute("pageTitle", "Không thể thực hiện thao tác");
        model.addAttribute("activeMenu", "maintenance");
        model.addAttribute("errorCode", status);
        model.addAttribute("errorMessage", message);
        return "maintenance/error";
    }
}
