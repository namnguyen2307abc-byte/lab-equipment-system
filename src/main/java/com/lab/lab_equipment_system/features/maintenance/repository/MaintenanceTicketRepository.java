package com.lab.lab_equipment_system.features.maintenance.repository;
import com.lab.lab_equipment_system.shared.entity.MaintenanceTicket;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
import com.lab.lab_equipment_system.shared.enums.MaintenanceStatus;
public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, Long>, JpaSpecificationExecutor<MaintenanceTicket> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from MaintenanceTicket t where t.id = :id")
    Optional<MaintenanceTicket> findByIdForUpdate(@Param("id") Long id);
    boolean existsByEquipment_IdAndStatusIn(Long equipmentId, Collection<MaintenanceStatus> statuses);
}

