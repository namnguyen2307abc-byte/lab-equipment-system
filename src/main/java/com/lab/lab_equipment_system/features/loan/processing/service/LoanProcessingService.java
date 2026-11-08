package com.lab.lab_equipment_system.features.loan.processing.service;

import com.lab.lab_equipment_system.features.loan.processing.dto.LoanProcessingDto;
import java.util.List;

public interface LoanProcessingService {

    List<LoanProcessingDto> getBorrowRequests(String statusFilter);

    LoanProcessingDto getLoanDetail(Long id);

    void approveBorrow(Long requestId, String staffNotes);
    void rejectBorrow(Long requestId, String rejectReason);
    void handoverEquipment(Long requestId);

    List<LoanProcessingDto> getReturnRequests();

    void confirmReturn(Long requestId, String staffNotes);

    List<LoanProcessingDto> getLoanHistory(String keyword);
}