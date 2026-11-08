package com.lab.lab_equipment_system.features.equipment.mapper;

import com.lab.lab_equipment_system.features.equipment.dto.*;
import com.lab.lab_equipment_system.shared.entity.*;

public final class StudentEquipmentMapper {
    private StudentEquipmentMapper() { }

    public static EquipmentCategoryResponse toCategoryResponse(Category entity) {
        if (entity == null) return null;
        EquipmentCategoryResponse response = new EquipmentCategoryResponse();
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
}
