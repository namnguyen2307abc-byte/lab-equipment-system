package com.lab.lab_equipment_system.loan.request.controller;

import com.lab.lab_equipment_system.auth.entity.User;
import com.lab.lab_equipment_system.auth.service.UserService;
import com.lab.lab_equipment_system.equipment.entity.Device;
import com.lab.lab_equipment_system.equipment.entity.DeviceStatus;
import com.lab.lab_equipment_system.loan.entity.LoanNotification;
import com.lab.lab_equipment_system.loan.entity.LoanRequest;
import com.lab.lab_equipment_system.loan.entity.LoanStatus;
import com.lab.lab_equipment_system.loan.request.dto.CancelLoanRequestDto;
import com.lab.lab_equipment_system.loan.request.dto.CreateLoanRequestDto;
import com.lab.lab_equipment_system.loan.request.service.StudentLoanRequestService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentLoanController {

    private final StudentLoanRequestService studentLoanRequestService;
    private final UserService userService;

    public StudentLoanController(StudentLoanRequestService studentLoanRequestService,
                                 UserService userService) {
        this.studentLoanRequestService = studentLoanRequestService;
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount() {
        User user = userService.getCurrentUser();
        return (user != null) ? studentLoanRequestService.getUnreadNotificationCount(user) : 0;
    }

    /**
     * UC-EM-03: Loan Request Registration - Hiển thị form đăng ký mượn thiết bị
     */
    @GetMapping("/loans/create")
    public String showCreateLoanForm(@RequestParam(value = "deviceId", required = false) Long deviceId,
                                     Model model) {
        CreateLoanRequestDto dto = new CreateLoanRequestDto();
        if (deviceId != null) {
            dto.setDeviceId(deviceId);
            try {
                Device device = studentLoanRequestService.getDeviceDetails(deviceId);
                model.addAttribute("selectedDevice", device);
            } catch (Exception ignored) {
            }
        }
        // Gợi ý hạn trả mặc định là 3 ngày sau
        dto.setDueAt(LocalDateTime.now().plusDays(3).withHour(17).withMinute(0).withSecond(0).withNano(0));

        model.addAttribute("loanRequestDto", dto);
        model.addAttribute("availableDevices", studentLoanRequestService.searchEquipment(null, null, DeviceStatus.AVAILABLE));
        return "student/loan-create";
    }

    /**
     * UC-EM-03: Loan Request Registration - Xử lý gửi yêu cầu mượn thiết bị
     */
    @PostMapping("/loans/create")
    public String submitLoanRequest(@Valid @ModelAttribute("loanRequestDto") CreateLoanRequestDto dto,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        User student = userService.getCurrentUser();
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để thực hiện chức năng này.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("availableDevices", studentLoanRequestService.searchEquipment(null, null, DeviceStatus.AVAILABLE));
            if (dto.getDeviceId() != null) {
                try {
                    model.addAttribute("selectedDevice", studentLoanRequestService.getDeviceDetails(dto.getDeviceId()));
                } catch (Exception ignored) {
                }
            }
            return "student/loan-create";
        }

        try {
            LoanRequest createdLoan = studentLoanRequestService.createLoanRequest(student, dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký mượn thiết bị thành công! Mã phiếu của bạn là: " + createdLoan.getRequestCode());
            return "redirect:/student/loans/" + createdLoan.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("availableDevices", studentLoanRequestService.searchEquipment(null, null, DeviceStatus.AVAILABLE));
            return "student/loan-create";
        }
    }

    /**
     * UC-EM-04: Personal Loan Tracking - Xem danh sách phiếu mượn cá nhân
     */
    @GetMapping("/loans")
    public String viewPersonalLoans(@RequestParam(value = "status", required = false) LoanStatus status,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        User student = userService.getCurrentUser();
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem phiếu mượn.");
            return "redirect:/login";
        }

        List<LoanRequest> loans = studentLoanRequestService.getPersonalLoanRequests(student, status);
        model.addAttribute("loans", loans);
        model.addAttribute("statuses", LoanStatus.values());
        model.addAttribute("selectedStatus", status);
        return "student/loan-list";
    }

    /**
     * UC-EM-05: Personal Loan Tracking - Xem chi tiết phiếu mượn
     */
    @GetMapping("/loans/{id}")
    public String viewLoanDetails(@PathVariable("id") Long id,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        User student = userService.getCurrentUser();
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem thông tin.");
            return "redirect:/login";
        }

        try {
            LoanRequest loan = studentLoanRequestService.getLoanRequestDetails(id, student);
            model.addAttribute("loan", loan);
            model.addAttribute("cancelDto", new CancelLoanRequestDto(id, ""));
            return "student/loan-detail";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/loans";
        }
    }

    /**
     * UC-EM-07: Cancel Loan Request - Hủy phiếu mượn (khi còn ở trạng thái Pending)
     */
    @PostMapping("/loans/cancel")
    public String cancelLoanRequest(@Valid @ModelAttribute("cancelDto") CancelLoanRequestDto cancelDto,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        User student = userService.getCurrentUser();
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để thực hiện.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError() != null ?
                    bindingResult.getFieldError().getDefaultMessage() : "Dữ liệu không hợp lệ";
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/student/loans/" + cancelDto.getLoanId();
        }

        try {
            LoanRequest updatedLoan = studentLoanRequestService.cancelLoanRequest(student, cancelDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hủy phiếu mượn " + updatedLoan.getRequestCode() + " thành công.");
            return "redirect:/student/loans/" + updatedLoan.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/loans/" + cancelDto.getLoanId();
        }
    }

    /**
     * UC-EM-06: Status Notification Tracking - Màn hình danh sách thông báo trạng thái phiếu mượn
     */
    @GetMapping("/notifications")
    public String viewNotifications(Model model, RedirectAttributes redirectAttributes) {
        User student = userService.getCurrentUser();
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem thông báo.");
            return "redirect:/login";
        }

        List<LoanNotification> notifications = studentLoanRequestService.getStudentNotifications(student);
        model.addAttribute("notifications", notifications);
        return "student/notifications";
    }

    /**
     * Đánh dấu 1 thông báo là đã đọc
     */
    @PostMapping("/notifications/{id}/read")
    public String markNotificationAsRead(@PathVariable("id") Long id) {
        User student = userService.getCurrentUser();
        if (student != null) {
            studentLoanRequestService.markNotificationAsRead(id, student);
        }
        return "redirect:/student/notifications";
    }

    /**
     * Đánh dấu tất cả thông báo là đã đọc
     */
    @PostMapping("/notifications/read-all")
    public String markAllAsRead() {
        User student = userService.getCurrentUser();
        if (student != null) {
            studentLoanRequestService.markAllNotificationsAsRead(student);
        }
        return "redirect:/student/notifications";
    }
}
