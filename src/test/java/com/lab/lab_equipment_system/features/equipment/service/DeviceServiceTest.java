package com.lab.lab_equipment_system.features.equipment.service;

import com.lab.lab_equipment_system.features.equipment.dto.DeviceRequest;
import com.lab.lab_equipment_system.features.equipment.dto.DeviceResponse;
import com.lab.lab_equipment_system.features.equipment.entity.Category;
import com.lab.lab_equipment_system.features.equipment.entity.Device;
import com.lab.lab_equipment_system.features.equipment.entity.EquipmentStatus;
import com.lab.lab_equipment_system.features.equipment.exception.DuplicateEquipmentCodeException;
import com.lab.lab_equipment_system.features.equipment.exception.EquipmentOperationException;
import com.lab.lab_equipment_system.features.equipment.repository.CategoryRepository;
import com.lab.lab_equipment_system.features.equipment.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private Category category;
    private Device sampleDevice;

    @BeforeEach
    void setUp() {
        category = new Category(1L, "Arduino", "Arduino kits");
        sampleDevice = new Device(10L, "ARD-001", "Arduino Uno R3", category, EquipmentStatus.AVAILABLE, "Vị trí A1", "ATmega328P");
    }

    @Test
    @DisplayName("Tạo thiết bị thành công với mã duy nhất")
    void testCreateDevice_Success() {
        DeviceRequest request = new DeviceRequest("ARD-002", "Arduino Mega", 1L, EquipmentStatus.AVAILABLE, "ATmega2560", "Ngăn A2");
        when(deviceRepository.existsByCodeIgnoreCase("ARD-002")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
            Device d = invocation.getArgument(0);
            d.setId(11L);
            return d;
        });

        DeviceResponse response = deviceService.createDevice(request);

        assertNotNull(response);
        assertEquals("ARD-002", response.getCode());
        assertEquals("Arduino Mega", response.getName());
        assertEquals("Arduino", response.getCategoryName());
        assertEquals(EquipmentStatus.AVAILABLE, response.getStatus());
        verify(deviceRepository).save(any(Device.class));
    }

    @Test
    @DisplayName("Ném lỗi DuplicateEquipmentCodeException khi mã thiết bị đã tồn tại")
    void testCreateDevice_DuplicateCode() {
        DeviceRequest request = new DeviceRequest("ARD-001", "Arduino Trùng", 1L, EquipmentStatus.AVAILABLE, "", "");
        when(deviceRepository.existsByCodeIgnoreCase("ARD-001")).thenReturn(true);

        assertThrows(DuplicateEquipmentCodeException.class, () -> deviceService.createDevice(request));
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Cập nhật tình trạng thiết bị thành công")
    void testUpdateDeviceStatus_Success() {
        when(deviceRepository.findById(10L)).thenReturn(Optional.of(sampleDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(sampleDevice);

        DeviceResponse response = deviceService.updateDeviceStatus(10L, EquipmentStatus.MAINTENANCE);

        assertNotNull(response);
        assertEquals(EquipmentStatus.MAINTENANCE, sampleDevice.getStatus());
        verify(deviceRepository).save(sampleDevice);
    }

    @Test
    @DisplayName("Ném lỗi khi xóa thiết bị đang ở trạng thái BORROWED (Đang mượn)")
    void testDeleteDevice_BorrowedThrowsException() {
        sampleDevice.setStatus(EquipmentStatus.BORROWED);
        when(deviceRepository.findById(10L)).thenReturn(Optional.of(sampleDevice));

        EquipmentOperationException ex = assertThrows(EquipmentOperationException.class,
                () -> deviceService.deleteDevice(10L));

        assertTrue(ex.getMessage().contains("Đang mượn"));
        verify(deviceRepository, never()).delete(any(Device.class));
    }

    @Test
    @DisplayName("Xóa thiết bị thành công khi ở trạng thái AVAILABLE")
    void testDeleteDevice_Success() {
        sampleDevice.setStatus(EquipmentStatus.AVAILABLE);
        when(deviceRepository.findById(10L)).thenReturn(Optional.of(sampleDevice));

        assertDoesNotThrow(() -> deviceService.deleteDevice(10L));
        verify(deviceRepository).delete(sampleDevice);
    }
}
