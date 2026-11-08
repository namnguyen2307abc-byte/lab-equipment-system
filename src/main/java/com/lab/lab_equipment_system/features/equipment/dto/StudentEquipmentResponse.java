package com.lab.lab_equipment_system.features.equipment.dto;

import java.time.LocalDateTime;
import com.lab.lab_equipment_system.shared.enums.*;

public class StudentEquipmentResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private EquipmentCategoryResponse category;
    private String specifications;
    private EquipmentStatus status;
    private String imageUrl;
    private String location;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EquipmentCategoryResponse getCategory() { return category; }
    public void setCategory(EquipmentCategoryResponse category) { this.category = category; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isAvailable() { return status == EquipmentStatus.AVAILABLE; }
}
