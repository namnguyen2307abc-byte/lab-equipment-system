package com.lab.lab_equipment_system.features.loan.processing.repository;

import com.lab.lab_equipment_system.shared.entity.LoanRequest;
import com.lab.lab_equipment_system.shared.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanProcessingRepository extends JpaRepository<LoanRequest, Long> {

    List<LoanRequest> findByStatusOrderByRequestedAtDesc(LoanStatus status);

    List<LoanRequest> findByStatusInOrderByRequestedAtDesc(List<LoanStatus> statuses);

    List<LoanRequest> findByEquipment_CodeContainingIgnoreCaseOrStudent_FullNameContainingIgnoreCaseOrderByRequestedAtDesc(
            String equipmentCode, String studentName);
}