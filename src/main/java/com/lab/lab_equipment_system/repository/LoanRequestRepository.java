package com.lab.lab_equipment_system.repository;

import com.lab.lab_equipment_system.entity.LoanRequest;
import com.lab.lab_equipment_system.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest, Long> {

    Optional<LoanRequest> findByRequestCode(String requestCode);

    List<LoanRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @Query("SELECT lr FROM LoanRequest lr WHERE lr.student.id = :studentId " +
           "AND (:status IS NULL OR lr.status = :status) " +
           "ORDER BY lr.createdAt DESC")
    List<LoanRequest> findByStudentIdAndStatus(@Param("studentId") Long studentId,
                                              @Param("status") LoanStatus status);

    @Query("SELECT COUNT(lr) FROM LoanRequest lr WHERE lr.device.id = :deviceId AND lr.status IN ('PENDING', 'APPROVED', 'HANDED_OVER')")
    long countActiveLoansForDevice(@Param("deviceId") Long deviceId);
}
