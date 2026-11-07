package com.lab.lab_equipment_system.features.maintenance.repository;
import com.lab.lab_equipment_system.shared.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface MaintenanceUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
}

