package com.lab.lab_equipment_system.features.equipment.dto;

import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DeviceRequest {

    @NotBlank(message = "Mã thiết bị không được để trống")
    @Size(max = 50, message = "Mã thiết bị không được vượt quá 50 ký tự")
    private String code;

    @NotBlank(message = "Tên thiết bị không được để trống")
    @Size(max = 150, message = "Tên thiết bị không được vượt quá 150 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn loại thiết bị")
    private Long categoryId;

    @NotNull(message = "Vui lòng chọn tình trạng thiết bị")
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Size(max = 4000, message = "Thông số kỹ thuật không được vượt quá 4000 ký tự")
    private String specifications;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    public DeviceRequest() {
    }

    public DeviceRequest(String code, String name, Long categoryId, EquipmentStatus status, String specifications, String description) {
        this.code = code;
        this.name = name;
        this.categoryId = categoryId;
        this.status = status;
        this.specifications = specifications;
        this.description = description;
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

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
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
