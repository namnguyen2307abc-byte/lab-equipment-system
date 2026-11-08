package com.lab.lab_equipment_system.features.maintenance.repository;
import com.lab.lab_equipment_system.shared.entity.LoanRequest;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import com.lab.lab_equipment_system.shared.enums.LoanStatus;
public interface MaintenanceLoanRepository extends JpaRepository<LoanRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoanRequest l where l.id = :id")
    Optional<LoanRequest> findByIdForUpdate(@Param("id") Long id);
    boolean existsByEquipment_IdAndStatusIn(Long equipmentId, Collection<LoanStatus> statuses);
    @EntityGraph(attributePaths = "equipment")
    List<LoanRequest> findByStudent_IdAndStatusInOrderByRequestedAtDesc(Long studentId, Collection<LoanStatus> statuses);
}

