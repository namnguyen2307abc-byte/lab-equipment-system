package com.lab.lab_equipment_system.features.equipment.dto;

import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;

public class DeviceResponse {

    private Long id;
    private String code;
    private String name;
    private Long categoryId;
    private String categoryName;
    private EquipmentStatus status;
    private String statusDisplayName;
    private String statusBadgeClass;
    private String specifications;
    private String description;

    public DeviceResponse() {
    }

    public DeviceResponse(Long id, String code, String name, Long categoryId, String categoryName,
                          EquipmentStatus status, String specifications, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.status = status;
        this.statusDisplayName = status != null ? status.getDisplayName() : "";
        this.statusBadgeClass = status != null ? badgeClass(status) : "";
        this.specifications = specifications;
        this.description = description;
    }

    public EquipmentStatus[] getEditableStatuses() {
        if (status == EquipmentStatus.ON_LOAN || status == EquipmentStatus.UNDER_MAINTENANCE) {
            return new EquipmentStatus[] {status};
        }
        return new EquipmentStatus[] {EquipmentStatus.AVAILABLE, EquipmentStatus.RETIRED};
    }

    private static String badgeClass(EquipmentStatus status) {
        return switch (status) {
            case AVAILABLE -> "badge-available";
            case ON_LOAN -> "badge-borrowed";
            case UNDER_MAINTENANCE -> "badge-maintenance";
            case RETIRED -> "badge-retired";
        };
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
        this.statusDisplayName = status != null ? status.getDisplayName() : "";
        this.statusBadgeClass = status != null ? badgeClass(status) : "";
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }

    public String getStatusBadgeClass() {
        return statusBadgeClass;
    }

    public void setStatusBadgeClass(String statusBadgeClass) {
        this.statusBadgeClass = statusBadgeClass;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
