package com.lab.lab_equipment_system.features.loan.request.controller;

import com.lab.lab_equipment_system.features.equipment.service.StudentEquipmentService;
import com.lab.lab_equipment_system.features.loan.request.dto.CancelLoanRequestDto;
import com.lab.lab_equipment_system.features.loan.request.dto.CreateLoanRequestDto;
import com.lab.lab_equipment_system.shared.enums.*;
import com.lab.lab_equipment_system.features.loan.request.service.StudentLoanService;
import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.features.equipment.dto.StudentEquipmentResponse;
import com.lab.lab_equipment_system.features.loan.request.dto.StudentLoanResponse;
import com.lab.lab_equipment_system.features.loan.request.dto.StudentNotificationResponse;
import com.lab.lab_equipment_system.features.auth.service.UserService;
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

    private final StudentEquipmentService studentEquipmentService;
    private final StudentLoanService studentLoanService;
    private final UserService userService;

    public StudentLoanController(StudentEquipmentService studentEquipmentService, StudentLoanService studentLoanService,
                                 UserService userService) {
        this.studentEquipmentService = studentEquipmentService;
        this.studentLoanService = studentLoanService;
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount() {
        UserResponse user = userService.getCurrentUser();
        return (user != null && user.getRole() == UserRole.STUDENT) ? studentLoanService.getUnreadNotificationCount(user) : 0;
    }

    /**
     * UC-EM-03: Loan Request Registration - Hiển thị form tạo phiếu mượn
     */
    @GetMapping("/loans/create")
    public String showCreateLoanForm(@RequestParam(value = "deviceId", required = false) Long deviceId,
                                     Model model) {
        CreateLoanRequestDto dto = new CreateLoanRequestDto();
        if (deviceId != null) {
            dto.setDeviceId(deviceId);
            try {
                StudentEquipmentResponse device = studentEquipmentService.getDeviceDetails(deviceId);
                model.addAttribute("selectedDevice", device);
            } catch (Exception ignored) {
            }
        }
        dto.setBorrowDate(LocalDateTime.now().plusMinutes(10).withSecond(0).withNano(0));
        dto.setDueAt(LocalDateTime.now().plusDays(3).withHour(17).withMinute(0).withSecond(0).withNano(0));

        model.addAttribute("pageTitle", "Đăng ký mượn thiết bị");
        model.addAttribute("activeMenu", "loans-create");
        model.addAttribute("loanRequestDto", dto);
        model.addAttribute("availableDevices", studentEquipmentService.searchEquipment(null, null, EquipmentStatus.AVAILABLE));
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
        UserResponse student = userService.getCurrentUser();
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui l\u00f2ng \u0111\u0103ng nh\u1eadp \u0111\u1ec3 th\u1ef1c hi\u1ec7n ch\u1ee9c n\u0103ng n\u00e0y.");
            return "redirect:/login";
        }

        model.addAttribute("pageTitle", "Đăng ký mượn thiết bị");
        model.addAttribute("activeMenu", "loans-create");

        if (bindingResult.hasErrors()) {
            model.addAttribute("availableDevices", studentEquipmentService.searchEquipment(null, null, EquipmentStatus.AVAILABLE));
            if (dto.getDeviceId() != null) {
                try {
                    model.addAttribute("selectedDevice", studentEquipmentService.getDeviceDetails(dto.getDeviceId()));
                } catch (Exception ignored) {
                }
            }
            return "student/loan-create";
        }

        try {
            StudentLoanResponse createdLoan = studentLoanService.createLoanRequest(student, dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "\u0110\u0103ng k\u00fd m\u01b0\u1ee3n thi\u1ebft b\u1ecb th\u00e0nh c\u00f4ng! M\u00e3 phi\u1ebfu c\u1ee7a b\u1ea1n l\u00e0: " + createdLoan.getRequestCode());
            return "redirect:/student/loans/" + createdLoan.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("availableDevices", studentEquipmentService.searchEquipment(null, null, EquipmentStatus.AVAILABLE));
            if (dto.getDeviceId() != null) {
                try {
                    model.addAttribute("selectedDevice", studentEquipmentService.getDeviceDetails(dto.getDeviceId()));
                } catch (Exception ignored) {
                }
            }
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
        UserResponse student = userService.getCurrentUser();
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui l\u00f2ng \u0111\u0103ng nh\u1eadp \u0111\u1ec3 xem phi\u1ebfu m\u01b0\u1ee3n.");
            return "redirect:/login";
        }

        List<StudentLoanResponse> loans = studentLoanService.getPersonalLoanRequests(student, status);
        List<StudentLoanResponse> allLoans = studentLoanService.getPersonalLoanRequests(student, null);

        long pendingCount = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.PENDING).count();
        long borrowedCount = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.BORROWED).count();
        long returnedCount = allLoans.stream().filter(l -> l.getStatus() == LoanStatus.RETURNED).count();

        model.addAttribute("pageTitle", "Phiếu mượn của tôi");
        model.addAttribute("activeMenu", "loans");
        model.addAttribute("loans", loans);
        model.addAttribute("statuses", LoanStatus.values());
        model.addAttribute("selectedStatus", status);

        model.addAttribute("totalLoans", allLoans.size());
        model.addAttribute("pendingLoans", pendingCount);
        model.addAttribute("borrowedLoans", borrowedCount);
        model.addAttribute("returnedLoans", returnedCount);

        model.addAttribute("cancelDto", new CancelLoanRequestDto());

        return "student/loan-list";
    }

    /**
     * UC-EM-05: Personal Loan Tracking - Xem chi tiết phiếu mượn
     */
    @GetMapping("/loans/{id}")
    public String viewLoanDetails(@PathVariable("id") Long id,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        UserResponse student = userService.getCurrentUser();
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui l\u00f2ng \u0111\u0103ng nh\u1eadp \u0111\u1ec3 xem th\u00f4ng tin.");
            return "redirect:/login";
        }

        try {
            StudentLoanResponse loan = studentLoanService.getLoanRequestDetails(id, student);
            model.addAttribute("pageTitle", "Chi tiết phiếu #" + loan.getRequestCode());
            model.addAttribute("activeMenu", "loans");
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
        UserResponse student = userService.getCurrentUser();
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui l\u00f2ng \u0111\u0103ng nh\u1eadp \u0111\u1ec3 th\u1ef1c hi\u1ec7n.");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError() != null ?
                    bindingResult.getFieldError().getDefaultMessage() : "D\u1eef li\u1ec7u kh\u00f4ng h\u1ee3p l\u1ec7";
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/student/loans/" + cancelDto.getLoanId();
        }

        try {
            StudentLoanResponse updatedLoan = studentLoanService.cancelLoanRequest(student, cancelDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "\u0110\u00e3 h\u1ee7y phi\u1ebfu m\u01b0\u1ee3n " + updatedLoan.getRequestCode() + " th\u00e0nh c\u00f4ng.");
            return "redirect:/student/loans/" + updatedLoan.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/loans/" + cancelDto.getLoanId();
        }
    }

    /**
     * UC-EM-06: Status Notification Tracking - Màn hình danh sách thông báo trạng thái
     */
    @GetMapping("/notifications")
    public String viewNotifications(Model model, RedirectAttributes redirectAttributes) {
        UserResponse student = userService.getCurrentUser();
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui l\u00f2ng \u0111\u0103ng nh\u1eadp \u0111\u1ec3 xem th\u00f4ng b\u00e1o.");
            return "redirect:/login";
        }

        List<StudentNotificationResponse> notifications = studentLoanService.getStudentNotifications(student);
        model.addAttribute("pageTitle", "Thông báo trạng thái");
        model.addAttribute("activeMenu", "notifications");
        model.addAttribute("notifications", notifications);
        return "student/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationAsRead(@PathVariable("id") Long id) {
        UserResponse student = userService.getCurrentUser();
        if (student != null) {
            studentLoanService.markNotificationAsRead(id, student);
        }
        return "redirect:/student/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllAsRead() {
        UserResponse student = userService.getCurrentUser();
        if (student != null) {
            studentLoanService.markAllNotificationsAsRead(student);
        }
        return "redirect:/student/notifications";
    }
}
