package com.lab.lab_equipment_system.features.maintenance.service;

import java.util.List;
import org.springframework.data.domain.Page;
import com.lab.lab_equipment_system.features.maintenance.dto.CompleteMaintenanceTicketRequest;
import com.lab.lab_equipment_system.features.maintenance.dto.CreateMaintenanceTicketRequest;
import com.lab.lab_equipment_system.features.maintenance.dto.DamageReportRequest;
import com.lab.lab_equipment_system.features.maintenance.dto.EquipmentOptionResponse;
import com.lab.lab_equipment_system.features.maintenance.dto.LoanOptionResponse;
import com.lab.lab_equipment_system.features.maintenance.dto.MaintenanceSearchRequest;
import com.lab.lab_equipment_system.features.maintenance.dto.MaintenanceStatsResponse;
import com.lab.lab_equipment_system.features.maintenance.dto.MaintenanceTicketActionRequest;
import com.lab.lab_equipment_system.features.maintenance.dto.MaintenanceTicketDetailResponse;
import com.lab.lab_equipment_system.features.maintenance.dto.MaintenanceTicketResponse;

public interface MaintenanceService {
    Page<MaintenanceTicketResponse> list(MaintenanceSearchRequest request);
    MaintenanceStatsResponse stats();
    List<EquipmentOptionResponse> filterEquipment();
    List<EquipmentOptionResponse> staffEquipmentOptions();
    List<LoanOptionResponse> studentLoanOptions();
    void requireStaffAccess();
    void requireStudentAccess();
    MaintenanceTicketDetailResponse detail(Long id);
    MaintenanceTicketResponse reportDamage(DamageReportRequest request);
    MaintenanceTicketResponse createTicket(CreateMaintenanceTicketRequest request);
    MaintenanceTicketResponse assess(Long id, MaintenanceTicketActionRequest request);
    MaintenanceTicketResponse updateProgress(Long id, MaintenanceTicketActionRequest request);
    MaintenanceTicketResponse complete(Long id, CompleteMaintenanceTicketRequest request);
}
