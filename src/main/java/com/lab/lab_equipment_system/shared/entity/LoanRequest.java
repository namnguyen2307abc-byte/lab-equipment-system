package com.lab.lab_equipment_system.shared.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Nationalized;
import com.lab.lab_equipment_system.shared.enums.*;

@Entity
@Table(name = "loan_requests")
public class LoanRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "processed_by_id", nullable = true)
    private User processedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LoanStatus status = LoanStatus.PENDING;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "handed_over_at", nullable = true)
    private LocalDateTime handedOverAt;

    @Column(name = "return_requested_at", nullable = true)
    private LocalDateTime returnRequestedAt;

    @Column(name = "returned_at", nullable = true)
    private LocalDateTime returnedAt;

    @Column(name = "request_code", length = 50, unique = true)
    private String requestCode;

    @Column(name = "borrow_date")
    private LocalDateTime borrowDate = LocalDateTime.now();

    @Nationalized
    @Column(name = "purpose", length = 500)
    private String purpose;

    @Nationalized
    @Column(name = "notes", length = 500)
    private String notes;

    @Nationalized
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Nationalized
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Nationalized
    @Column(name = "staff_notes", length = 500)
    private String staffNotes;

    public LoanRequest() { }

    public LoanRequest(String requestCode, User student, Equipment equipment, LocalDateTime dueAt, String purpose, String notes) {
        this.requestCode = requestCode;
        this.student = student;
        this.equipment = equipment;
        this.dueAt = dueAt;
        this.purpose = purpose;
        this.notes = notes;
    }

    public String getRequestCode() { return requestCode; }
    public void setRequestCode(String requestCode) { this.requestCode = requestCode; }

    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    public User getProcessedBy() { return processedBy; }
    public void setProcessedBy(User processedBy) { this.processedBy = processedBy; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    public LocalDateTime getHandedOverAt() { return handedOverAt; }
    public void setHandedOverAt(LocalDateTime handedOverAt) { this.handedOverAt = handedOverAt; }

    public LocalDateTime getReturnRequestedAt() { return returnRequestedAt; }
    public void setReturnRequestedAt(LocalDateTime returnRequestedAt) { this.returnRequestedAt = returnRequestedAt; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
}

