package com.lab.lab_equipment_system.features.loan.request.dto;

import java.time.LocalDateTime;
import com.lab.lab_equipment_system.shared.enums.*;

public class StudentNotificationResponse {
    private Long id;
    private StudentLoanResponse loanRequest;
    private String title;
    private String message;
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentLoanResponse getLoanRequest() { return loanRequest; }
    public void setLoanRequest(StudentLoanResponse loanRequest) { this.loanRequest = loanRequest; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
