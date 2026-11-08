package com.lab.lab_equipment_system.features.equipment.controller;

import com.lab.lab_equipment_system.shared.dto.UserResponse;
import com.lab.lab_equipment_system.features.auth.service.UserService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(assignableTypes = EquipmentController.class)
public class EquipmentModelAdvice {
    private final UserService userService;
    public EquipmentModelAdvice(UserService userService) { this.userService = userService; }

    @ModelAttribute("inventoryStatuses")
    public com.lab.lab_equipment_system.shared.enums.EquipmentStatus[] inventoryStatuses() {
        return new com.lab.lab_equipment_system.shared.enums.EquipmentStatus[] {
            com.lab.lab_equipment_system.shared.enums.EquipmentStatus.AVAILABLE,
            com.lab.lab_equipment_system.shared.enums.EquipmentStatus.RETIRED
        };
    }

    @ModelAttribute("currentUser")
    public UserResponse currentUser() { return userService.getCurrentUser(); }
}
