package com.lab.lab_equipment_system.features.maintenance.dto;

import java.util.List;

public class MaintenanceTicketDetailResponse {
    private MaintenanceTicketResponse summary;
    private String description;
    private String resolutionNote;
    private Long loanRequestId;
    private String updatedAt;
    private String resolvedAt;
    private long version;
    private boolean equipmentStillBorrowed;
    private List<MaintenanceEventResponse> events;

    public MaintenanceTicketResponse getSummary() {
        return summary;
    }

    public void setSummary(MaintenanceTicketResponse summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public Long getLoanRequestId() {
        return loanRequestId;
    }

    public void setLoanRequestId(Long loanRequestId) {
        this.loanRequestId = loanRequestId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isEquipmentStillBorrowed() {
        return equipmentStillBorrowed;
    }

    public void setEquipmentStillBorrowed(boolean equipmentStillBorrowed) {
        this.equipmentStillBorrowed = equipmentStillBorrowed;
    }

    public List<MaintenanceEventResponse> getEvents() {
        return events;
    }

    public void setEvents(List<MaintenanceEventResponse> events) {
        this.events = events;
    }
}
