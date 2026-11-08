package com.lab.lab_equipment_system.features.loan.processing.repository;

import com.lab.lab_equipment_system.shared.entity.LoanRequest;
import com.lab.lab_equipment_system.shared.enums.LoanStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoanProcessingRepository extends JpaRepository<LoanRequest, Long> {
    List<LoanRequest> findByStatusOrderByRequestedAtDesc(LoanStatus status);
    List<LoanRequest> findByStatusInOrderByRequestedAtDesc(List<LoanStatus> statuses);

    @Query("select l.equipment.id from LoanRequest l where l.id = :id")
    Optional<Long> findEquipmentId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoanRequest l where l.id = :id")
    Optional<LoanRequest> findByIdForUpdate(@Param("id") Long id);

    boolean existsByEquipment_IdAndStatusInAndIdNot(Long equipmentId, Collection<LoanStatus> statuses, Long id);

    @Query("select l from LoanRequest l where l.status in :statuses and (:pattern is null "
        + "or lower(l.equipment.code) like :pattern escape '!' "
        + "or lower(l.student.fullName) like :pattern escape '!') order by l.requestedAt desc, l.id desc")
    List<LoanRequest> findHistory(@Param("statuses") Collection<LoanStatus> statuses, @Param("pattern") String pattern);
}
