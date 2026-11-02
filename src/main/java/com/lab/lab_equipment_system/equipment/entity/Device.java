package com.lab.lab_equipment_system.equipment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // e.g. ARD-001, ESP-001, RPI-001

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String specifications; // Thông số kỹ thuật chi tiết

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeviceStatus status = DeviceStatus.AVAILABLE;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(length = 100)
    private String location; // e.g. Tủ A1 - Phòng Lab 302

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Device() {
    }

    public Device(String code, String name, Category category, String specifications, DeviceStatus status, String imageUrl, String location) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.specifications = specifications;
        this.status = status;
        this.imageUrl = imageUrl;
        this.location = location;
        this.createdAt = LocalDateTime.now();
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAvailable() {
        return this.status == DeviceStatus.AVAILABLE;
    }
}
