package com.lab.lab_equipment_system.features.loan.request.mapper;

import com.lab.lab_equipment_system.features.loan.request.dto.*;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.mapper.UserMapper;
import com.lab.lab_equipment_system.features.equipment.mapper.StudentEquipmentMapper;

public final class StudentLoanMapper {
    private StudentLoanMapper() { }

    public static StudentLoanResponse toStudentLoanResponse(LoanRequest entity) {
        if (entity == null) return null;
        StudentLoanResponse response = new StudentLoanResponse();
        response.setId(entity.getId());
        response.setRequestCode(entity.getRequestCode());
        response.setStudent(UserMapper.toUserResponse(entity.getStudent()));
        response.setEquipment(StudentEquipmentMapper.toStudentEquipmentResponse(entity.getEquipment()));
        response.setRequestedAt(entity.getRequestedAt());
        response.setBorrowDate(entity.getBorrowDate());
        response.setDueAt(entity.getDueAt());
        response.setHandedOverAt(entity.getHandedOverAt());
        response.setReturnedAt(entity.getReturnedAt());
        response.setStatus(entity.getStatus());
        response.setPurpose(entity.getPurpose());
        response.setNotes(entity.getNotes());
        response.setCancelReason(entity.getCancelReason());
        response.setCancelledAt(entity.getCancelledAt());
        response.setRejectReason(entity.getRejectReason());
        response.setStaffNotes(entity.getStaffNotes());
        return response;
    }

    public static StudentNotificationResponse toStudentNotificationResponse(LoanNotification entity) {
        if (entity == null) return null;
        StudentNotificationResponse response = new StudentNotificationResponse();
        response.setId(entity.getId());
        response.setLoanRequest(toStudentLoanResponse(entity.getLoanRequest()));
        response.setTitle(entity.getTitle());
        response.setMessage(entity.getMessage());
        response.setType(entity.getType());
        response.setRead(entity.isRead());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
