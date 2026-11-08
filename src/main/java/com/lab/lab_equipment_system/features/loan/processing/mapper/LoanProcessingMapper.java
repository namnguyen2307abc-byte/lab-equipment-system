package com.lab.lab_equipment_system.features.loan.processing.mapper;

import com.lab.lab_equipment_system.features.loan.processing.dto.LoanProcessingDto;
import com.lab.lab_equipment_system.shared.entity.LoanRequest;
import org.springframework.stereotype.Component;

@Component
public class LoanProcessingMapper {

    public LoanProcessingDto toDto(LoanRequest entity) {
        if (entity == null) {
            return null;
        }

        LoanProcessingDto dto = new LoanProcessingDto();
        dto.setId(entity.getId());
        dto.setRequestCode(entity.getRequestCode() != null ? entity.getRequestCode() : "LR-" + entity.getId());

        if (entity.getStudent() != null) {
            dto.setStudentName(entity.getStudent().getFullName());
            dto.setStudentEmail(entity.getStudent().getEmail());
        }

        if (entity.getEquipment() != null) {
            dto.setEquipmentId(entity.getEquipment().getId());
            dto.setEquipmentCode(entity.getEquipment().getCode());
            dto.setEquipmentName(entity.getEquipment().getName());
            dto.setEquipmentStatus(entity.getEquipment().getStatus() != null ? entity.getEquipment().getStatus().toString() : "");
        }

        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus());
            dto.setStatusLabel(entity.getStatus().getLabel());
        }

        dto.setProcessedByName(entity.getProcessedBy() != null ? entity.getProcessedBy().getFullName() : "Chưa xử lý");
        dto.setPurpose(entity.getPurpose());
        dto.setNotes(entity.getNotes());
        dto.setRejectReason(entity.getRejectReason());
        dto.setStaffNotes(entity.getStaffNotes());
        dto.setBorrowDate(entity.getBorrowDate());
        dto.setRequestedAt(entity.getRequestedAt());
        dto.setDueAt(entity.getDueAt());
        dto.setHandedOverAt(entity.getHandedOverAt());
        dto.setReturnRequestedAt(entity.getReturnRequestedAt());
        dto.setReturnedAt(entity.getReturnedAt());

        return dto;
    }
}