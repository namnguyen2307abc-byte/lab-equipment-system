package com.lab.lab_equipment_system.features.equipment.exception;

public class DuplicateEquipmentCodeException extends RuntimeException {
    public DuplicateEquipmentCodeException(String message) {
        super(message);
    }
}
