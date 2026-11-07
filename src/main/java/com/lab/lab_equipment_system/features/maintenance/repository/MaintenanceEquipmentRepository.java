package com.lab.lab_equipment_system.features.maintenance.repository;
import com.lab.lab_equipment_system.shared.entity.Equipment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface MaintenanceEquipmentRepository extends JpaRepository<Equipment, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Equipment e where e.id = :id")
    Optional<Equipment> findByIdForUpdate(@Param("id") Long id);
    List<Equipment> findAllByOrderByCodeAsc();
    Optional<Equipment> findByCode(String code);
}

