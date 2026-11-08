package com.lab.lab_equipment_system.features.loan.processing.controller;

import com.lab.lab_equipment_system.features.loan.processing.service.LoanProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff/loans")
public class LoanProcessingController {

    @Autowired
    private LoanProcessingService processingService;

    @GetMapping("/borrow-requests")
    public String viewBorrowRequests(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("requests", processingService.getBorrowRequests(status));
        return "staff/borrow-list";
    }

    @GetMapping("/detail/{id}")
    public String viewLoanDetail(@PathVariable Long id, Model model) {
        model.addAttribute("loan", processingService.getLoanDetail(id));
        return "staff/loan-detail";
    }

    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable Long id,
                                 @RequestParam(required = false) String staffNotes) {
        processingService.approveBorrow(id, staffNotes);
        return "redirect:/staff/loans/borrow-requests";
    }

    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam(required = false) String rejectReason) {
        processingService.rejectBorrow(id, rejectReason);
        return "redirect:/staff/loans/borrow-requests";
    }

    @PostMapping("/handover/{id}")
    public String handoverEquipment(@PathVariable Long id) {
        processingService.handoverEquipment(id);
        return "redirect:/staff/loans/borrow-requests";
    }

    @GetMapping("/return-requests")
    public String viewReturnRequests(Model model) {
        model.addAttribute("returnRequests", processingService.getReturnRequests());
        return "staff/return-list";
    }

    @PostMapping("/confirm-return/{id}")
    public String confirmReturn(@PathVariable Long id,
                                @RequestParam(required = false) String staffNotes) {
        processingService.confirmReturn(id, staffNotes);
        return "redirect:/staff/loans/return-requests";
    }

    @GetMapping("/history")
    public String viewHistory(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("historyList", processingService.getLoanHistory(keyword));
        model.addAttribute("keyword", keyword);
        return "staff/loan-history";
    }
}