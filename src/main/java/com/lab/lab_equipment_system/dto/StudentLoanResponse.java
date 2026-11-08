package com.lab.lab_equipment_system.dto;

import java.time.LocalDateTime;
import com.lab.lab_equipment_system.shared.enums.*;

public class StudentLoanResponse {
    private Long id;
    private String requestCode;
    private UserResponse student;
    private StudentEquipmentResponse equipment;
    private LocalDateTime requestedAt;
    private LocalDateTime borrowDate;
    private LocalDateTime dueAt;
    private LocalDateTime handedOverAt;
    private LocalDateTime returnedAt;
    private LoanStatus status;
    private String purpose;
    private String notes;
    private String cancelReason;
    private LocalDateTime cancelledAt;
    private String rejectReason;
    private String staffNotes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestCode() { return requestCode; }
    public void setRequestCode(String requestCode) { this.requestCode = requestCode; }

    public UserResponse getStudent() { return student; }
    public void setStudent(UserResponse student) { this.student = student; }

    public StudentEquipmentResponse getEquipment() { return equipment; }
    public void setEquipment(StudentEquipmentResponse equipment) { this.equipment = equipment; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    public LocalDateTime getHandedOverAt() { return handedOverAt; }
    public void setHandedOverAt(LocalDateTime handedOverAt) { this.handedOverAt = handedOverAt; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getStaffNotes() { return staffNotes; }
    public void setStaffNotes(String staffNotes) { this.staffNotes = staffNotes; }

    public boolean isCancellable() { return status == LoanStatus.PENDING; }
}
