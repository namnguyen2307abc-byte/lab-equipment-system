package com.lab.lab_equipment_system.loan.request.controller;

import com.lab.lab_equipment_system.auth.entity.User;
import com.lab.lab_equipment_system.auth.service.UserService;
import com.lab.lab_equipment_system.equipment.entity.Device;
import com.lab.lab_equipment_system.equipment.entity.DeviceStatus;
import com.lab.lab_equipment_system.loan.request.service.StudentLoanRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/student/equipment")
public class StudentEquipmentController {

    private final StudentLoanRequestService studentLoanRequestService;
    private final UserService userService;

    public StudentEquipmentController(StudentLoanRequestService studentLoanRequestService,
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
     * UC-EM-01: Equipment Catalog Search
     * Cho phép sinh viên tìm kiếm, lọc danh mục thiết bị theo từ khóa (mã, tên), loại thiết bị và trạng thái.
     */
    @GetMapping
    public String searchCatalog(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "categoryId", required = false) Long categoryId,
                                @RequestParam(value = "status", required = false) DeviceStatus status,
                                Model model) {
        List<Device> devices = studentLoanRequestService.searchEquipment(keyword, categoryId, status);
        model.addAttribute("devices", devices);
        model.addAttribute("categories", studentLoanRequestService.getAllCategories());
        model.addAttribute("statuses", DeviceStatus.values());
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        return "student/equipment-catalog";
    }

    /**
     * UC-EM-02: View Equipment Details
     * Cho phép sinh viên xem chi tiết đầy đủ thông tin thiết bị (mã, tên, danh mục, thông số kỹ thuật, trạng thái).
     */
    @GetMapping("/{id}")
    public String viewEquipmentDetail(@PathVariable("id") Long id, Model model) {
        Device device = studentLoanRequestService.getDeviceDetails(id);
        model.addAttribute("device", device);
        return "student/equipment-detail";
    }
}
