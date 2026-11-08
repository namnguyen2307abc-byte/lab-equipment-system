package com.lab.lab_equipment_system.features.equipment.controller;

import com.lab.lab_equipment_system.features.equipment.dto.CategoryRequest;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceFilterRequest;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceRequest;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceResponse;
import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;
import com.lab.lab_equipment_system.features.equipment.exception.DuplicateEquipmentCodeException;
import com.lab.lab_equipment_system.features.equipment.exception.EquipmentOperationException;
import com.lab.lab_equipment_system.features.equipment.service.CategoryService;
import com.lab.lab_equipment_system.features.equipment.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý các request giao diện web Thymeleaf cho module Thiết bị (Thành viên 2: Tuấn).
 */
@Controller
public class EquipmentController {

    private final DeviceService deviceService;
    private final CategoryService categoryService;

    public EquipmentController(DeviceService deviceService, CategoryService categoryService) {
        this.deviceService = deviceService;
        this.categoryService = categoryService;
    }

    /**
     * Màn hình danh sách thiết bị (UC-EC-01, UC-EC-05)
     */
    @GetMapping("/equipment")
    public String listEquipment(@ModelAttribute("filter") DeviceFilterRequest filter, Model model) {
        Page<DeviceResponse> devicePage = deviceService.searchDevices(filter);

        model.addAttribute("pageTitle", "Quản lý thiết bị");
        model.addAttribute("activeMenu", "equipment-management");
        model.addAttribute("devicePage", devicePage);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("statuses", EquipmentStatus.values());

        // Thống kê nhanh tình trạng thiết bị
        model.addAttribute("totalCount", deviceService.countTotal());
        model.addAttribute("availableCount", deviceService.countByStatus(EquipmentStatus.AVAILABLE));
        model.addAttribute("borrowedCount", deviceService.countByStatus(EquipmentStatus.ON_LOAN));
        model.addAttribute("maintenanceCount", deviceService.countByStatus(EquipmentStatus.UNDER_MAINTENANCE));
        model.addAttribute("retiredCount", deviceService.countByStatus(EquipmentStatus.RETIRED));

        return "equipment/list";
    }

    /**
     * Màn hình thêm thiết bị mới (UC-EC-02)
     */
    @GetMapping("/equipment/new")
    public String showCreateForm(Model model) {
        model.addAttribute("pageTitle", "Thêm thiết bị mới");
        model.addAttribute("activeMenu", "equipment-management");
        model.addAttribute("deviceRequest", new DeviceRequest());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("statuses", EquipmentStatus.values());
        model.addAttribute("isEdit", false);
        return "equipment/form";
    }

    /**
     * Xử lý lưu thiết bị mới (UC-EC-02)
     */
    @PostMapping("/equipment")
    public String createEquipment(@Valid @ModelAttribute("deviceRequest") DeviceRequest request,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Thêm thiết bị mới");
            model.addAttribute("activeMenu", "equipment-management");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("statuses", EquipmentStatus.values());
            model.addAttribute("isEdit", false);
            return "equipment/form";
        }

        try {
            DeviceResponse created = deviceService.createDevice(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm thiết bị '" + created.getName() + " (" + created.getCode() + ")' thành công!");
            return "redirect:/equipment";
        } catch (DuplicateEquipmentCodeException e) {
            bindingResult.rejectValue("code", "duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Thêm thiết bị mới");
            model.addAttribute("activeMenu", "equipment-management");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("statuses", EquipmentStatus.values());
            model.addAttribute("isEdit", false);
            return "equipment/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không thể tạo thiết bị: " + e.getMessage());
            model.addAttribute("pageTitle", "Thêm thiết bị mới");
            model.addAttribute("activeMenu", "equipment-management");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("statuses", EquipmentStatus.values());
            model.addAttribute("isEdit", false);
            return "equipment/form";
        }
    }

    /**
     * Màn hình xem chi tiết thiết bị
     */
    @GetMapping("/equipment/{id}")
    public String showDetail(@PathVariable("id") Long id, Model model) {
        DeviceResponse device = deviceService.getDeviceById(id);
        model.addAttribute("pageTitle", "Chi tiết thiết bị · " + device.getCode());
        model.addAttribute("activeMenu", "equipment-management");
        model.addAttribute("device", device);
        model.addAttribute("statuses", EquipmentStatus.values());
        return "equipment/detail";
    }

    /**
     * Màn hình chỉnh sửa thông tin thiết bị (UC-EC-03)
     */
    @GetMapping("/equipment/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        DeviceResponse device = deviceService.getDeviceById(id);
        DeviceRequest request = new DeviceRequest(
                device.getCode(),
                device.getName(),
                device.getCategoryId(),
                device.getStatus(),
                device.getSpecifications(),
                device.getDescription()
        );

        model.addAttribute("pageTitle", "Chỉnh sửa thiết bị · " + device.getCode());
        model.addAttribute("activeMenu", "equipment-management");
        model.addAttribute("deviceId", id);
        model.addAttribute("deviceRequest", request);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("statuses", EquipmentStatus.values());
        model.addAttribute("isEdit", true);
        return "equipment/form";
    }

    /**
     * Xử lý cập nhật thông tin thiết bị (UC-EC-03)
     */
    @PostMapping("/equipment/{id}/edit")
    public String updateEquipment(@PathVariable("id") Long id,
                                  @Valid @ModelAttribute("deviceRequest") DeviceRequest request,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Chỉnh sửa thiết bị");
            model.addAttribute("activeMenu", "equipment-management");
            model.addAttribute("deviceId", id);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("statuses", EquipmentStatus.values());
            model.addAttribute("isEdit", true);
            return "equipment/form";
        }

        try {
            DeviceResponse updated = deviceService.updateDevice(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật thông tin thiết bị '" + updated.getCode() + "' thành công!");
            return "redirect:/equipment/" + id;
        } catch (DuplicateEquipmentCodeException e) {
            bindingResult.rejectValue("code", "duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Chỉnh sửa thiết bị");
            model.addAttribute("activeMenu", "equipment-management");
            model.addAttribute("deviceId", id);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("statuses", EquipmentStatus.values());
            model.addAttribute("isEdit", true);
            return "equipment/form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không thể cập nhật thiết bị: " + e.getMessage());
            model.addAttribute("pageTitle", "Chỉnh sửa thiết bị");
            model.addAttribute("activeMenu", "equipment-management");
            model.addAttribute("deviceId", id);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("statuses", EquipmentStatus.values());
            model.addAttribute("isEdit", true);
            return "equipment/form";
        }
    }

    /**
     * Xử lý cập nhật nhanh tình trạng thiết bị
     */
    @PostMapping("/equipment/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") EquipmentStatus status,
                               @RequestParam(value = "redirect", required = false, defaultValue = "list") String redirect,
                               RedirectAttributes redirectAttributes) {
        try {
            DeviceResponse updated = deviceService.updateDeviceStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã chuyển trạng thái thiết bị '" + updated.getCode() + "' sang: " + updated.getStatusDisplayName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể đổi trạng thái: " + e.getMessage());
        }

        if ("detail".equalsIgnoreCase(redirect)) {
            return "redirect:/equipment/" + id;
        }
        return "redirect:/equipment";
    }

    /**
     * Xử lý xóa thiết bị (UC-EC-04)
     */
    @PostMapping("/equipment/{id}/delete")
    public String deleteEquipment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            DeviceResponse device = deviceService.getDeviceById(id);
            deviceService.deleteDevice(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xóa thiết bị '" + device.getCode() + " - " + device.getName() + "' khỏi hệ thống.");
        } catch (EquipmentOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa thiết bị: " + e.getMessage());
        }
        return "redirect:/equipment";
    }

    // ==========================================
    // Quản lý Loại thiết bị (Categories)
    // ==========================================

    /**
     * Màn hình danh sách loại thiết bị
     */
    @GetMapping("/equipment/categories")
    public String listCategories(Model model) {
        model.addAttribute("pageTitle", "Quản lý loại thiết bị");
        model.addAttribute("activeMenu", "equipment-management");
        model.addAttribute("categories", categoryService.getAllCategoriesWithDeviceCount());
        if (!model.containsAttribute("categoryRequest")) {
            model.addAttribute("categoryRequest", new CategoryRequest());
        }
        return "equipment/categories";
    }

    /**
     * Màn hình riêng thêm mới Loại thiết bị
     */
    @GetMapping("/equipment/categories/new")
    public String showCreateCategoryForm(@RequestParam(value = "redirect", required = false) String redirectTarget,
                                         Model model) {
        model.addAttribute("pageTitle", "Thêm loại thiết bị mới");
        model.addAttribute("activeMenu", "equipment-management");
        model.addAttribute("categoryRequest", new CategoryRequest());
        model.addAttribute("redirectTarget", redirectTarget);
        return "equipment/category-form";
    }

    /**
     * Xử lý thêm loại thiết bị
     */
    @PostMapping({"/equipment/categories", "/equipment/categories/new"})
    public String createCategory(@Valid @ModelAttribute("categoryRequest") CategoryRequest request,
                                 BindingResult bindingResult,
                                 @RequestParam(value = "redirectTarget", required = false) String redirectTarget,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Thêm loại thiết bị mới");
            model.addAttribute("activeMenu", "equipment-management");
            model.addAttribute("categories", categoryService.getAllCategoriesWithDeviceCount());
            model.addAttribute("redirectTarget", redirectTarget);
            return "equipment/category-form";
        }

        try {
            categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm loại thiết bị '" + request.getName() + "' thành công!");

            // Nếu người dùng đang ở trang thêm thiết bị bấm sang thêm loại, quay lại trang thêm thiết bị
            if ("equipment_new".equalsIgnoreCase(redirectTarget)) {
                return "redirect:/equipment/new";
            }
            return "redirect:/equipment/categories";
        } catch (EquipmentOperationException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            model.addAttribute("pageTitle", "Thêm loại thiết bị mới");
            model.addAttribute("activeMenu", "equipment-management");
            model.addAttribute("categories", categoryService.getAllCategoriesWithDeviceCount());
            model.addAttribute("redirectTarget", redirectTarget);
            return "equipment/category-form";
        }
    }

    /**
     * Xử lý cập nhật loại thiết bị
     */
    @PostMapping("/equipment/categories/{id}")
    public String updateCategory(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("categoryRequest") CategoryRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu cập nhật loại thiết bị không hợp lệ.");
            return "redirect:/equipment/categories";
        }

        try {
            categoryService.updateCategory(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật loại thiết bị '" + request.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/equipment/categories";
    }

    /**
     * Xử lý xóa loại thiết bị
     */
    @PostMapping("/equipment/categories/{id}/delete")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa loại thiết bị thành công!");
        } catch (EquipmentOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa loại thiết bị: " + e.getMessage());
        }
        return "redirect:/equipment/categories";
    }
}
