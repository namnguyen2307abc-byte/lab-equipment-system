package com.lab.lab_equipment_system.controller;

import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;
import com.lab.lab_equipment_system.service.StudentLoanService;
import com.lab.lab_equipment_system.dto.*;
import com.lab.lab_equipment_system.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/student/equipment")
public class StudentEquipmentController {

    private final StudentLoanService studentLoanService;
    private final UserService userService;

    public StudentEquipmentController(StudentLoanService studentLoanService,
                                      UserService userService) {
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
        return (user != null && user.getRole() == com.lab.lab_equipment_system.shared.enums.UserRole.STUDENT)
            ? studentLoanService.getUnreadNotificationCount(user) : 0;
    }

    /**
     * UC-EM-01: Equipment Catalog Search
     */
    @GetMapping
    public String searchCatalog(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "categoryId", required = false) Long categoryId,
                                @RequestParam(value = "status", required = false) EquipmentStatus status,
                                Model model) {
        List<StudentEquipmentResponse> devices = studentLoanService.searchEquipment(keyword, categoryId, status);
        List<StudentEquipmentResponse> allDevices = studentLoanService.searchEquipment(null, null, null);

        long availableCount = allDevices.stream().filter(d -> d.getStatus() == EquipmentStatus.AVAILABLE).count();
        long borrowedCount = allDevices.stream().filter(d -> d.getStatus() == EquipmentStatus.ON_LOAN).count();
        long maintenanceCount = allDevices.stream().filter(d -> d.getStatus() == EquipmentStatus.UNDER_MAINTENANCE).count();

        model.addAttribute("pageTitle", "Danh mục thiết bị");
        model.addAttribute("activeMenu", "equipment");
        model.addAttribute("devices", devices);
        model.addAttribute("categories", studentLoanService.getAllCategories());
        model.addAttribute("statuses", EquipmentStatus.values());
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStatus", status);

        model.addAttribute("totalDevices", allDevices.size());
        model.addAttribute("availableDevices", availableCount);
        model.addAttribute("inUseDevices", borrowedCount);
        model.addAttribute("maintenanceDevices", maintenanceCount);

        return "student/equipment-catalog";
    }

    /**
     * UC-EM-02: View Equipment Details
     */
    @GetMapping("/{id}")
    public String viewEquipmentDetail(@PathVariable("id") Long id, Model model) {
        StudentEquipmentResponse device = studentLoanService.getDeviceDetails(id);
        model.addAttribute("pageTitle", "Chi tiết thiết bị: " + device.getName());
        model.addAttribute("activeMenu", "equipment");
        model.addAttribute("device", device);
        return "student/equipment-detail";
    }
}
