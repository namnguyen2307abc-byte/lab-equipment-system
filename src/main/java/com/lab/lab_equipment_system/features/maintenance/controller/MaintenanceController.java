package com.lab.lab_equipment_system.features.maintenance.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lab.lab_equipment_system.features.maintenance.dto.*;
import com.lab.lab_equipment_system.features.maintenance.service.*;
import com.lab.lab_equipment_system.shared.enums.MaintenanceStatus;

@Controller
@RequestMapping("/maintenance")
@org.springframework.context.annotation.Profile("!ui-preview")
public class MaintenanceController {
    private final MaintenanceService service;
    private final MaintenanceIdentityService identity;

    public MaintenanceController(MaintenanceService service, MaintenanceIdentityService identity) {
        this.service = service; this.identity = identity;
    }

    @ModelAttribute
    void layout(Model model) {
        model.addAttribute("activeMenu", "maintenance");
        model.addAttribute("maintenanceUser", identity.currentUser());
    }

    @GetMapping
    String list(@ModelAttribute("searchRequest") MaintenanceSearchRequest request, Model model) {
        model.addAttribute("pageTitle", "Bảo trì thiết bị");
        model.addAttribute("tickets", service.list(request));
        model.addAttribute("stats", service.stats());
        model.addAttribute("statuses", MaintenanceStatus.values());
        model.addAttribute("equipmentOptions", service.filterEquipment());
        model.addAttribute("selectedStatus", request.getStatus());
        model.addAttribute("selectedEquipment", request.getEquipmentId());
        model.addAttribute("query", request.getQ());
        return "maintenance/list";
    }

    @GetMapping("/report")
    String report(@RequestParam(required = false) Long loanRequestId, Model model) {
        service.requireStudentAccess();
        DamageReportRequest form = new DamageReportRequest();
        form.setLoanRequestId(loanRequestId);
        model.addAttribute("reportForm", form);
        return reportModel(model);
    }

    @PostMapping("/report")
    String report(@Valid @ModelAttribute("reportForm") DamageReportRequest form, BindingResult errors,
                  Model model, RedirectAttributes redirect) {
        service.requireStudentAccess();
        if (errors.hasErrors()) return reportModel(model);
        MaintenanceTicketResponse response = service.reportDamage(form);
        redirect.addFlashAttribute("successMessage", "Đã gửi báo hỏng. Nhân viên sẽ tiếp nhận và xử lý phiếu của bạn.");
        return "redirect:/maintenance/" + response.getId();
    }

    @GetMapping("/new")
    String create(@RequestParam(required = false) Long equipmentId,
                  @RequestParam(required = false) Long loanRequestId, Model model) {
        service.requireStaffAccess();
        CreateMaintenanceTicketRequest form = new CreateMaintenanceTicketRequest();
        form.setEquipmentId(equipmentId); form.setLoanRequestId(loanRequestId);
        model.addAttribute("createForm", form);
        return createModel(model);
    }

    @PostMapping
    String create(@Valid @ModelAttribute("createForm") CreateMaintenanceTicketRequest form, BindingResult errors,
                  Model model, RedirectAttributes redirect) {
        service.requireStaffAccess();
        if (errors.hasErrors()) return createModel(model);
        MaintenanceTicketResponse response = service.createTicket(form);
        redirect.addFlashAttribute("successMessage", "Đã tạo phiếu bảo trì và tạm ngừng cho mượn thiết bị.");
        return "redirect:/maintenance/" + response.getId();
    }

    @GetMapping("/{id:\\d+}")
    String detail(@PathVariable Long id, Model model) { return detailModel(id, model); }

    @PostMapping("/{id:\\d+}/assess")
    String assess(@PathVariable Long id, @Valid @ModelAttribute("assessForm") MaintenanceTicketActionRequest form,
                  BindingResult errors, Model model, RedirectAttributes redirect) {
        service.requireStaffAccess();
        if (errors.hasErrors()) return detailModel(id, model);
        service.assess(id, form);
        redirect.addFlashAttribute("successMessage", "Đã tiếp nhận phiếu bảo trì.");
        return "redirect:/maintenance/" + id;
    }

    @PostMapping("/{id:\\d+}/progress")
    String progress(@PathVariable Long id, @Valid @ModelAttribute("progressForm") MaintenanceTicketActionRequest form,
                    BindingResult errors, Model model, RedirectAttributes redirect) {
        service.requireStaffAccess();
        if (errors.hasErrors()) return detailModel(id, model);
        service.updateProgress(id, form);
        redirect.addFlashAttribute("successMessage", "Đã lưu tiến độ sửa chữa.");
        return "redirect:/maintenance/" + id;
    }

    @PostMapping("/{id:\\d+}/complete")
    String complete(@PathVariable Long id, @Valid @ModelAttribute("completeForm") CompleteMaintenanceTicketRequest form,
                    BindingResult errors, Model model, RedirectAttributes redirect) {
        service.requireStaffAccess();
        if (errors.hasErrors()) return detailModel(id, model);
        service.complete(id, form);
        redirect.addFlashAttribute("successMessage", "Đã hoàn tất phiếu và cập nhật tình trạng thiết bị.");
        return "redirect:/maintenance/" + id;
    }

    private String reportModel(Model model) {
        model.addAttribute("pageTitle", "Báo hỏng thiết bị");
        model.addAttribute("loanOptions", service.studentLoanOptions());
        return "maintenance/report";
    }
    private String createModel(Model model) {
        model.addAttribute("pageTitle", "Tạo phiếu bảo trì");
        model.addAttribute("equipmentOptions", service.staffEquipmentOptions());
        return "maintenance/new";
    }
    private String detailModel(Long id, Model model) {
        MaintenanceTicketDetailResponse ticket = service.detail(id);
        model.addAttribute("pageTitle", ticket.getSummary().getCode());
        model.addAttribute("ticket", ticket);
        if (!model.containsAttribute("assessForm")) {
            MaintenanceTicketActionRequest form = new MaintenanceTicketActionRequest(); form.setVersion(ticket.getVersion());
            model.addAttribute("assessForm", form);
        }
        if (!model.containsAttribute("progressForm")) {
            MaintenanceTicketActionRequest form = new MaintenanceTicketActionRequest(); form.setVersion(ticket.getVersion());
            model.addAttribute("progressForm", form);
        }
        if (!model.containsAttribute("completeForm")) {
            CompleteMaintenanceTicketRequest form = new CompleteMaintenanceTicketRequest(); form.setVersion(ticket.getVersion());
            model.addAttribute("completeForm", form);
        }
        return "maintenance/detail";
    }
}
