package com.lab.lab_equipment_system.features.equipment.repository;

import org.springframework.stereotype.Repository;

/**
 * Interface thuận tiện (alias) cho DeviceRepository, phục vụ cho việc tích hợp
 * hoặc nếu các thành viên khác trong nhóm tìm kiếm theo tên EquipmentRepository.
 */
@Repository
public interface EquipmentRepository extends DeviceRepository {
}
