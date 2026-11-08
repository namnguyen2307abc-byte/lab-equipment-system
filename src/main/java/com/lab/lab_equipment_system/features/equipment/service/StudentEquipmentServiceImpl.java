package com.lab.lab_equipment_system.features.equipment.service;

import com.lab.lab_equipment_system.shared.exception.ResourceNotFoundException;

import com.lab.lab_equipment_system.features.equipment.dto.*;
import com.lab.lab_equipment_system.features.equipment.mapper.StudentEquipmentMapper;
import com.lab.lab_equipment_system.features.equipment.repository.*;
import com.lab.lab_equipment_system.shared.entity.Equipment;
import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentEquipmentServiceImpl implements StudentEquipmentService {
    private final StudentEquipmentRepository deviceRepository;
    private final EquipmentCategoryRepository categoryRepository;

    public StudentEquipmentServiceImpl(StudentEquipmentRepository deviceRepository, EquipmentCategoryRepository categoryRepository) {
        this.deviceRepository = deviceRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentEquipmentResponse> searchEquipment(String keyword, Long categoryId, EquipmentStatus status) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return deviceRepository.searchDevices(cleanKeyword, categoryId, status).stream()
            .map(StudentEquipmentMapper::toStudentEquipmentResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(StudentEquipmentMapper::toCategoryResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentEquipmentResponse getDeviceDetails(Long deviceId) {
        Equipment equipment = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị: " + deviceId));
        return StudentEquipmentMapper.toStudentEquipmentResponse(equipment);
    }

}
