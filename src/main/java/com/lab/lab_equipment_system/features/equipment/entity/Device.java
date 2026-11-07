package com.lab.lab_equipment_system.features.equipment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Thực thể Thiết bị vật lý (equipment).
 * Phụ trách bởi Thành viên 2 (Tuấn).
 * Lưu thông tin từng thiết bị đơn lẻ với mã duy nhất (ví dụ: ARD-001, ESP-001).
 */
@Entity
@Table(name = "equipment")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Convert(converter = EquipmentStatusConverter.class)
    @Column(name = "status", nullable = false, length = 30)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "specifications", length = 500)
    private String specifications;

    public Device() {
    }

    public Device(String code, String name, Category category, EquipmentStatus status, String description, String specifications) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.status = status != null ? status : EquipmentStatus.AVAILABLE;
        this.description = description;
        this.specifications = specifications;
    }

    public Device(Long id, String code, String name, Category category, EquipmentStatus status, String description, String specifications) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.status = status != null ? status : EquipmentStatus.AVAILABLE;
        this.description = description;
        this.specifications = specifications;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }
}
