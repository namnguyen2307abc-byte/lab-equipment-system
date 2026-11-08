package com.lab.lab_equipment_system.features.loan.processing.service;

import com.lab.lab_equipment_system.features.loan.processing.dto.*;
import java.util.List;

public interface LoanProcessingService {
    List<LoanProcessingDto> getBorrowRequests(String statusFilter);
    LoanProcessingDto getLoanDetail(Long id);
    void approveBorrow(Long requestId, LoanProcessingNoteRequest request);
    void rejectBorrow(Long requestId, LoanRejectionRequest request);
    void handoverEquipment(Long requestId);
    List<LoanProcessingDto> getReturnRequests();
    void confirmReturn(Long requestId, LoanProcessingNoteRequest request);
    List<LoanProcessingDto> getLoanHistory(String keyword);
}
