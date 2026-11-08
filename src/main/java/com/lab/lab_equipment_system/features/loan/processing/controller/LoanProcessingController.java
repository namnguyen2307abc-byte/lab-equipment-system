package com.lab.lab_equipment_system.features.loan.processing.controller;

import com.lab.lab_equipment_system.features.auth.service.UserService;
import com.lab.lab_equipment_system.features.loan.processing.dto.*;
import com.lab.lab_equipment_system.features.loan.processing.service.LoanProcessingService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/loans")
public class LoanProcessingController {
    private final LoanProcessingService service;
    private final UserService users;
    public LoanProcessingController(LoanProcessingService service, UserService users) {
        this.service = service; this.users = users;
    }
    @ModelAttribute
    public void layout(Model model) {
        model.addAttribute("currentUser", users.getCurrentUser());
        model.addAttribute("activeMenu", "loan-processing");
    }

    @GetMapping("/borrow-requests")
    public String borrowRequests(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("pageTitle", "Xử lý phiếu mượn");
        model.addAttribute("requests", service.getBorrowRequests(status));
        model.addAttribute("selectedStatus", status);
        return "staff/borrow-list";
    }
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) { return detailModel(id, model); }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id, @Valid @ModelAttribute("approvalForm") LoanProcessingNoteRequest form,
            BindingResult binding, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        if (binding.hasErrors()) { response.setStatus(400); return detailModel(id, model); }
        service.approveBorrow(id, form);
        redirect.addFlashAttribute("successMessage", "Đã duyệt phiếu mượn.");
        return "redirect:/staff/loans/detail/" + id;
    }
    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id, @Valid @ModelAttribute("rejectionForm") LoanRejectionRequest form,
            BindingResult binding, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        if (binding.hasErrors()) { response.setStatus(400); return detailModel(id, model); }
        service.rejectBorrow(id, form);
        redirect.addFlashAttribute("successMessage", "Đã từ chối phiếu và gửi lý do cho sinh viên.");
        return "redirect:/staff/loans/detail/" + id;
    }
    @PostMapping("/handover/{id}")
    public String handover(@PathVariable Long id, RedirectAttributes redirect) {
        service.handoverEquipment(id);
        redirect.addFlashAttribute("successMessage", "Đã bàn giao và cập nhật thiết bị đang mượn.");
        return "redirect:/staff/loans/detail/" + id;
    }
    @GetMapping("/return-requests")
    public String returnRequests(Model model) {
        model.addAttribute("pageTitle", "Nhận trả thiết bị");
        model.addAttribute("returnRequests", service.getReturnRequests());
        return "staff/return-list";
    }
    @PostMapping("/confirm-return/{id}")
    public String confirmReturn(@PathVariable Long id, @Valid @ModelAttribute("returnForm") LoanProcessingNoteRequest form,
            BindingResult binding, Model model, HttpServletResponse response, RedirectAttributes redirect) {
        if (binding.hasErrors()) { response.setStatus(400); return detailModel(id, model); }
        service.confirmReturn(id, form);
        redirect.addFlashAttribute("successMessage", "Đã nhận trả và cập nhật tình trạng thiết bị.");
        return "redirect:/staff/loans/detail/" + id;
    }
    @GetMapping("/history")
    public String history(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("pageTitle", "Lịch sử mượn trả");
        model.addAttribute("historyList", service.getLoanHistory(keyword));
        model.addAttribute("keyword", keyword);
        return "staff/loan-history";
    }
    private String detailModel(Long id, Model model) {
        model.addAttribute("pageTitle", "Chi tiết phiếu mượn");
        model.addAttribute("loan", service.getLoanDetail(id));
        if (!model.containsAttribute("approvalForm")) model.addAttribute("approvalForm", new LoanProcessingNoteRequest());
        if (!model.containsAttribute("rejectionForm")) model.addAttribute("rejectionForm", new LoanRejectionRequest());
        if (!model.containsAttribute("returnForm")) model.addAttribute("returnForm", new LoanProcessingNoteRequest());
        return "staff/loan-detail";
    }
}
