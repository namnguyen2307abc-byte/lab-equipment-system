package com.lab.lab_equipment_system.features.maintenance.repository;
import com.lab.lab_equipment_system.shared.entity.MaintenanceTicketEvent;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface MaintenanceEventRepository extends JpaRepository<MaintenanceTicketEvent, Long> {
    @EntityGraph(attributePaths = "actor")
    List<MaintenanceTicketEvent> findByTicket_IdOrderByCreatedAtAscIdAsc(Long ticketId);
}

