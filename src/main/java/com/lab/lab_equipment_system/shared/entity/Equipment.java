package com.lab.lab_equipment_system.shared.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Nationalized;
import com.lab.lab_equipment_system.shared.enums.*;

@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Nationalized
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Nationalized
    @Column(name = "description", nullable = true, length = 500)
    private String description;

    @Nationalized
    @Column(name = "specifications", length = 4000)
    private String specifications;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Nationalized
    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Equipment() { }

    public Equipment(String code, String name, Category category, String specifications,
                     EquipmentStatus status, String imageUrl, String location) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.specifications = specifications;
        this.status = status;
        this.imageUrl = imageUrl;
        this.location = location;
    }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isAvailable() { return status == EquipmentStatus.AVAILABLE; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

