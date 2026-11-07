package com.lab.lab_equipment_system.features.maintenance.dto;

import com.lab.lab_equipment_system.shared.enums.UserRole;

public class CurrentUserResponse {
    private Long id;
    private String name;
    private String email;
    private UserRole role;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isStaff() {
        return role != null && role.isStaff();
    }
}
