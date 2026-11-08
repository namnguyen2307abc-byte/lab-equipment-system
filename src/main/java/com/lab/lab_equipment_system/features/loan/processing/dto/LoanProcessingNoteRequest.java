package com.lab.lab_equipment_system.features.loan.processing.dto;

import jakarta.validation.constraints.Size;

public class LoanProcessingNoteRequest {
    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự.")
    private String staffNotes;
    public LoanProcessingNoteRequest() {}
    public LoanProcessingNoteRequest(String staffNotes) { this.staffNotes = staffNotes; }
    public String getStaffNotes() { return staffNotes; }
    public void setStaffNotes(String staffNotes) { this.staffNotes = staffNotes; }
}
