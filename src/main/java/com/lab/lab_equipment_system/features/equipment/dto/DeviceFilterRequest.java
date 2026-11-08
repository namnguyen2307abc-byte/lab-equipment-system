package com.lab.lab_equipment_system.features.equipment.dto;

import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;

public class DeviceFilterRequest {

    private String keyword;
    private Long categoryId;
    private EquipmentStatus status;
    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String sortDirection = "DESC";

    public DeviceFilterRequest() {
    }

    public DeviceFilterRequest(String keyword, Long categoryId, EquipmentStatus status) {
        this.keyword = keyword;
        this.categoryId = categoryId;
        this.status = status;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size > 0 ? size : 10;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
