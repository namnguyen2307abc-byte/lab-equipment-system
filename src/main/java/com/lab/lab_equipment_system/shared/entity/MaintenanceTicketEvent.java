package com.lab.lab_equipment_system.shared.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Nationalized;
import com.lab.lab_equipment_system.shared.enums.*;

@Entity
@Table(name = "maintenance_ticket_events")
public class MaintenanceTicketEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private MaintenanceTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private MaintenanceEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MaintenanceStatus status;

    @Nationalized
    @Column(name = "note", nullable = false, length = 1000)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MaintenanceTicket getTicket() { return ticket; }
    public void setTicket(MaintenanceTicket ticket) { this.ticket = ticket; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }

    public MaintenanceEventType getEventType() { return eventType; }
    public void setEventType(MaintenanceEventType eventType) { this.eventType = eventType; }

    public MaintenanceStatus getStatus() { return status; }
    public void setStatus(MaintenanceStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

