package com.lab.lab_equipment_system.features.equipment.service;

import com.lab.lab_equipment_system.features.equipment.dto.StudentEquipmentResponse;
import com.lab.lab_equipment_system.features.equipment.repository.*;
import com.lab.lab_equipment_system.shared.entity.*;
import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentEquipmentServiceTests {
    @Mock StudentEquipmentRepository deviceRepository;
    @Mock EquipmentCategoryRepository categoryRepository;
    @InjectMocks StudentEquipmentServiceImpl studentEquipmentService;
    Equipment mockDevice;

    @BeforeEach void fixtures() {
        Category category = new Category("Arduino", "Kit"); category.setId(10L);
        mockDevice = new Equipment("ARD-001", "Kit Arduino Uno R3", category, "ATmega328P", EquipmentStatus.AVAILABLE, null, "A1");
        mockDevice.setId(100L);
    }

    @Test
    @DisplayName("UC-EM-01: Tìm kiếm thiết bị theo từ khóa và danh mục")
    void testSearchEquipment() {
        when(deviceRepository.searchDevices("Arduino", 10L, EquipmentStatus.AVAILABLE))
                .thenReturn(List.of(mockDevice));

        List<StudentEquipmentResponse> result = studentEquipmentService.searchEquipment("Arduino", 10L, EquipmentStatus.AVAILABLE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ARD-001", result.get(0).getCode());
        verify(deviceRepository, times(1)).searchDevices("Arduino", 10L, EquipmentStatus.AVAILABLE);
    }

    @Test
    @DisplayName("UC-EM-02: Xem chi tiết thiết bị thành công")
    void testGetDeviceDetails() {
        when(deviceRepository.findById(100L)).thenReturn(Optional.of(mockDevice));

        StudentEquipmentResponse found = studentEquipmentService.getDeviceDetails(100L);

        assertNotNull(found);
        assertEquals("Kit Arduino Uno R3", found.getName());
        assertEquals(EquipmentStatus.AVAILABLE, found.getStatus());
    }

}
