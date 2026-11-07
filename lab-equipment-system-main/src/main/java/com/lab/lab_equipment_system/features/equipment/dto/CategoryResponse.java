package com.lab.lab_equipment_system.features.equipment.dto;

public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private long deviceCount;

    public CategoryResponse() {
    }

    public CategoryResponse(Long id, String name, String description, long deviceCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.deviceCount = deviceCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(long deviceCount) {
        this.deviceCount = deviceCount;
    }
}
