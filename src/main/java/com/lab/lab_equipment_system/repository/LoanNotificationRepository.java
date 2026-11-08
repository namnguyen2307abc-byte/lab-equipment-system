package com.lab.lab_equipment_system.repository;

import com.lab.lab_equipment_system.shared.entity.LoanNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanNotificationRepository extends JpaRepository<LoanNotification, Long> {

    List<LoanNotification> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<LoanNotification> findByStudentIdAndReadFalseOrderByCreatedAtDesc(Long studentId);

    long countByStudentIdAndReadFalse(Long studentId);
}
