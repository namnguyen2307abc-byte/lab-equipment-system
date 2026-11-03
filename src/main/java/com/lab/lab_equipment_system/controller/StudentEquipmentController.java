package com.lab.lab_equipment_system.controller;

import com.lab.lab_equipment_system.entity.Device;
import com.lab.lab_equipment_system.entity.DeviceStatus;
import com.lab.lab_equipment_system.entity.User;
import com.lab.lab_equipment_system.service.StudentLoanService;
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
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount() {
        User user = userService.getCurrentUser();
        return (user != null) ? studentLoanService.getUnreadNotificationCount(user) : 0;
    }

    /**
     * UC-EM-01: Equipment Catalog Search
     */
    @GetMapping
    public String searchCatalog(@RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "categoryId", required = false) Long categoryId,
                                @RequestParam(value = "status", required = false) DeviceStatus status,
                                Model model) {
        List<Device> devices = studentLoanService.searchEquipment(keyword, categoryId, status);
        model.addAttribute("devices", devices);
        model.addAttribute("categories", studentLoanService.getAllCategories());
        model.addAttribute("statuses", DeviceStatus.values());
        model.addAttribute("selectedKeyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        return "student/equipment-catalog";
    }

    /**
     * UC-EM-02: View Equipment Details
     */
    @GetMapping("/{id}")
    public String viewEquipmentDetail(@PathVariable("id") Long id, Model model) {
        Device device = studentLoanService.getDeviceDetails(id);
        model.addAttribute("device", device);
        return "student/equipment-detail";
    }
}
