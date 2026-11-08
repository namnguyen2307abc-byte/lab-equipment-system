package com.lab.lab_equipment_system.features.loan.processing.service;

import com.lab.lab_equipment_system.features.loan.processing.dto.LoanProcessingDto;
import com.lab.lab_equipment_system.features.loan.processing.mapper.LoanProcessingMapper;
import com.lab.lab_equipment_system.features.loan.processing.repository.LoanProcessingRepository;
import com.lab.lab_equipment_system.shared.entity.LoanRequest;
import com.lab.lab_equipment_system.shared.enums.LoanStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanProcessingServiceImpl implements LoanProcessingService {

    @Autowired
    private LoanProcessingRepository processingRepository;

    @Autowired
    private LoanProcessingMapper mapper;

    @Override
    public List<LoanProcessingDto> getBorrowRequests(String statusFilter) {
        List<LoanRequest> list;
        if (statusFilter != null && !statusFilter.isEmpty()) {
            LoanStatus status = LoanStatus.valueOf(statusFilter);
            list = processingRepository.findByStatusOrderByRequestedAtDesc(status);
        } else {
            list = processingRepository.findByStatusInOrderByRequestedAtDesc(
                    Arrays.asList(LoanStatus.PENDING, LoanStatus.APPROVED)
            );
        }
        return list.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public LoanProcessingDto getLoanDetail(Long id) {
        LoanRequest entity = processingRepository.findById(id).orElse(null);
        return mapper.toDto(entity);
    }

    @Override
    public void approveBorrow(Long requestId, String staffNotes) {
        LoanRequest req = processingRepository.findById(requestId).orElse(null);
        if (req != null && req.getStatus() == LoanStatus.PENDING) {
            req.setStatus(LoanStatus.APPROVED);
            if (staffNotes != null && !staffNotes.isEmpty()) {
                req.setStaffNotes(staffNotes);
            }
            processingRepository.save(req);
        }
    }

    @Override
    public void rejectBorrow(Long requestId, String rejectReason) {
        LoanRequest req = processingRepository.findById(requestId).orElse(null);
        if (req != null && req.getStatus() == LoanStatus.PENDING) {
            req.setStatus(LoanStatus.REJECTED);
            req.setRejectReason(rejectReason);
            processingRepository.save(req);
        }
    }

    @Override
    public void handoverEquipment(Long requestId) {
        LoanRequest req = processingRepository.findById(requestId).orElse(null);
        if (req != null && req.getStatus() == LoanStatus.APPROVED) {
            req.setStatus(LoanStatus.BORROWED);
            req.setHandedOverAt(LocalDateTime.now());
            processingRepository.save(req);
        }
    }

    @Override
    public List<LoanProcessingDto> getReturnRequests() {
        return processingRepository.findByStatusInOrderByRequestedAtDesc(
                        Arrays.asList(LoanStatus.RETURN_REQUESTED, LoanStatus.BORROWED))
                .stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void confirmReturn(Long requestId, String staffNotes) {
        LoanRequest req = processingRepository.findById(requestId).orElse(null);
        if (req != null) {
            req.setStatus(LoanStatus.RETURNED);
            req.setReturnedAt(LocalDateTime.now());
            if (staffNotes != null && !staffNotes.isEmpty()) {
                req.setStaffNotes(staffNotes);
            }
            processingRepository.save(req);
        }
    }

    @Override
    public List<LoanProcessingDto> getLoanHistory(String keyword) {
        List<LoanRequest> list;
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = processingRepository
                    .findByEquipment_CodeContainingIgnoreCaseOrStudent_FullNameContainingIgnoreCaseOrderByRequestedAtDesc(
                            keyword.trim(), keyword.trim());
        } else {
            list = processingRepository.findByStatusInOrderByRequestedAtDesc(
                    Arrays.asList(LoanStatus.RETURNED, LoanStatus.REJECTED, LoanStatus.CANCELLED)
            );
        }
        return list.stream().map(mapper::toDto).collect(Collectors.toList());
    }
}