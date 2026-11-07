package com.lab.lab_equipment_system.shared.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Nationalized;
import com.lab.lab_equipment_system.shared.enums.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Nationalized
    @Column(name = "description", nullable = true, length = 500)
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

