package com.lab.lab_equipment_system.mapper;

import com.lab.lab_equipment_system.dto.*;
import com.lab.lab_equipment_system.shared.entity.*;

public final class StudentDtoMapper {
    private StudentDtoMapper() { }

    public static UserResponse toUserResponse(User entity) {
        if (entity == null) return null;
        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setUsername(entity.getUsername());
        response.setFullName(entity.getFullName());
        response.setEmail(entity.getEmail());
        response.setPhone(entity.getPhone());
        response.setRole(entity.getRole());
        response.setActive(entity.isActive());
        return response;
    }

    public static CategoryResponse toCategoryResponse(Category entity) {
        if (entity == null) return null;
        CategoryResponse response = new CategoryResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        return response;
    }

    public static StudentEquipmentResponse toStudentEquipmentResponse(Equipment entity) {
        if (entity == null) return null;
        StudentEquipmentResponse response = new StudentEquipmentResponse();
        response.setId(entity.getId());
        response.setCode(entity.getCode());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setCategory(toCategoryResponse(entity.getCategory()));
        response.setSpecifications(entity.getSpecifications());
        response.setStatus(entity.getStatus());
        response.setImageUrl(entity.getImageUrl());
        response.setLocation(entity.getLocation());
        return response;
    }

    public static StudentLoanResponse toStudentLoanResponse(LoanRequest entity) {
        if (entity == null) return null;
        StudentLoanResponse response = new StudentLoanResponse();
        response.setId(entity.getId());
        response.setRequestCode(entity.getRequestCode());
        response.setStudent(toUserResponse(entity.getStudent()));
        response.setEquipment(toStudentEquipmentResponse(entity.getEquipment()));
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
