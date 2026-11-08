package com.lab.lab_equipment_system.features.equipment.repository;

import com.lab.lab_equipment_system.shared.entity.Equipment;
import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;
import com.lab.lab_equipment_system.shared.enums.LoanStatus;
import com.lab.lab_equipment_system.shared.enums.MaintenanceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EquipmentCatalogRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {
    Optional<Equipment> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    long countByCategoryId(Long categoryId);
    long countByStatus(EquipmentStatus status);

    @Query("SELECT e FROM Equipment e JOIN FETCH e.category WHERE e.id = :id")
    Optional<Equipment> findByIdWithCategory(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.id = :id")
    Optional<Equipment> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT e FROM Equipment e JOIN FETCH e.category ORDER BY e.id DESC")
    List<Equipment> findAllWithCategory();

    @Query("SELECT COUNT(l) FROM LoanRequest l WHERE l.equipment.id = :id")
    long countLoanHistory(@Param("id") Long id);

    @Query("SELECT COUNT(t) FROM MaintenanceTicket t WHERE t.equipment.id = :id")
    long countMaintenanceHistory(@Param("id") Long id);

    @Query("SELECT COUNT(l) FROM LoanRequest l WHERE l.equipment.id = :id AND l.status IN :statuses")
    long countActiveLoans(@Param("id") Long id, @Param("statuses") Collection<LoanStatus> statuses);

    @Query("SELECT COUNT(t) FROM MaintenanceTicket t WHERE t.equipment.id = :id AND t.status IN :statuses")
    long countActiveMaintenance(@Param("id") Long id, @Param("statuses") Collection<MaintenanceStatus> statuses);
}
