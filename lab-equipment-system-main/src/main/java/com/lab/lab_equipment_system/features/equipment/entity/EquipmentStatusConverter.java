package com.lab.lab_equipment_system.features.equipment.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter tự động chuyển đổi giữa String trong Database SQL Server và enum EquipmentStatus trong Java.
 * Giúp hệ thống hoạt động tương thích an toàn nếu DB chứa 'UNDER_MAINTENANCE', 'MAINTENANCE',...
 */
@Converter(autoApply = true)
public class EquipmentStatusConverter implements AttributeConverter<EquipmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(EquipmentStatus attribute) {
        if (attribute == null) {
            return EquipmentStatus.AVAILABLE.name();
        }
        return attribute.name();
    }

    @Override
    public EquipmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return EquipmentStatus.AVAILABLE;
        }

        String normalized = dbData.trim().toUpperCase();

        try {
            return EquipmentStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            // Linh hoạt map các biến thể nếu database nhóm dùng từ đồng nghĩa
            if (normalized.contains("MAINTEN")) {
                return EquipmentStatus.UNDER_MAINTENANCE;
            }
            if (normalized.contains("BORROW") || normalized.contains("LOAN")) {
                return EquipmentStatus.BORROWED;
            }
            if (normalized.contains("DAMAGE") || normalized.contains("BROKEN")) {
                return EquipmentStatus.DAMAGED;
            }
            if (normalized.contains("RETIRE") || normalized.contains("DISPOSE")) {
                return EquipmentStatus.RETIRED;
            }
            if (normalized.contains("LOST")) {
                return EquipmentStatus.LOST;
            }
            return EquipmentStatus.AVAILABLE;
        }
    }
}
