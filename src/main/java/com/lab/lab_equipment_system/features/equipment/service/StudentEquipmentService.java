package com.lab.lab_equipment_system.features.equipment.service;

import com.lab.lab_equipment_system.features.equipment.dto.EquipmentCategoryResponse;
import com.lab.lab_equipment_system.features.equipment.dto.StudentEquipmentResponse;
import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;
import java.util.List;

public interface StudentEquipmentService {
    List<StudentEquipmentResponse> searchEquipment(String keyword, Long categoryId, EquipmentStatus status);
    List<EquipmentCategoryResponse> getAllCategories();
    StudentEquipmentResponse getDeviceDetails(Long deviceId);
}
