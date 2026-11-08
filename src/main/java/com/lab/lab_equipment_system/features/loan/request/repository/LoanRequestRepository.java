package com.lab.lab_equipment_system.features.loan.request.repository;

import com.lab.lab_equipment_system.shared.entity.LoanRequest;
import com.lab.lab_equipment_system.shared.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {

    Optional<LoanRequest> findByRequestCode(String requestCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoanRequest l where l.id = :id")
    Optional<LoanRequest> findByIdForUpdate(@Param("id") Long id);


    List<LoanRequest> findByStudentIdOrderByRequestedAtDesc(Long studentId);

    @Query("SELECT lr FROM LoanRequest lr WHERE lr.student.id = :studentId " +
           "AND (:status IS NULL OR lr.status = :status) " +
           "ORDER BY lr.requestedAt DESC")
    List<LoanRequest> findByStudentIdAndStatus(@Param("studentId") Long studentId,
                                              @Param("status") LoanStatus status);

    @Query("SELECT COUNT(lr) FROM LoanRequest lr WHERE lr.equipment.id = :deviceId AND lr.status IN ('PENDING', 'APPROVED', 'BORROWED', 'RETURN_REQUESTED')")
    long countActiveLoansForDevice(@Param("deviceId") Long deviceId);
}
