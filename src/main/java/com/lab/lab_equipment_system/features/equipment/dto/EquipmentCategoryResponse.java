package com.lab.lab_equipment_system.features.equipment.dto;

import java.time.LocalDateTime;
import com.lab.lab_equipment_system.shared.enums.*;

public class EquipmentCategoryResponse {
    private Long id;
    private String name;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

}
