package com.lab.lab_equipment_system.features.equipment.service;

import com.lab.lab_equipment_system.features.equipment.dto.CategoryRequest;
import com.lab.lab_equipment_system.features.equipment.dto.CategoryResponse;
import com.lab.lab_equipment_system.shared.entity.Category;
import com.lab.lab_equipment_system.features.equipment.exception.EquipmentOperationException;
import com.lab.lab_equipment_system.features.equipment.exception.ResourceNotFoundException;
import com.lab.lab_equipment_system.features.equipment.repository.EquipmentCategoryRepository;
import com.lab.lab_equipment_system.features.equipment.repository.EquipmentCatalogRepository;
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
class CategoryServiceTest {

    @Mock
    private EquipmentCategoryRepository categoryRepository;

    @Mock
    private EquipmentCatalogRepository deviceRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category("Arduino", "Kit Arduino");
        sampleCategory.setId(1L);
    }

    @Test
    @DisplayName("Tạo loại thiết bị thành công khi tên chưa tồn tại")
    void testCreateCategory_Success() {
        CategoryRequest request = new CategoryRequest("ESP32", "Kit ESP32 WiFi");
        when(categoryRepository.existsByNameIgnoreCase("ESP32")).thenReturn(false);
        Category saved = new Category("ESP32", "Kit ESP32 WiFi");
        saved.setId(2L);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(deviceRepository.countByCategoryId(2L)).thenReturn(0L);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("ESP32", response.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi tạo loại thiết bị trùng tên")
    void testCreateCategory_DuplicateName() {
        CategoryRequest request = new CategoryRequest("Arduino", "Trùng");
        when(categoryRepository.existsByNameIgnoreCase("Arduino")).thenReturn(true);

        assertThrows(EquipmentOperationException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi xóa loại thiết bị đang có thiết bị trực thuộc")
    void testDeleteCategory_HasDevices() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(deviceRepository.countByCategoryId(1L)).thenReturn(3L);

        EquipmentOperationException ex = assertThrows(EquipmentOperationException.class,
                () -> categoryService.deleteCategory(1L));

        assertTrue(ex.getMessage().contains("đang có 3 thiết bị"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Xóa loại thiết bị thành công khi không có thiết bị nào")
    void testDeleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(deviceRepository.countByCategoryId(1L)).thenReturn(0L);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L));
        verify(categoryRepository).delete(sampleCategory);
    }
}
