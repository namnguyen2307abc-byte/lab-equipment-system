package com.lab.lab_equipment_system.repository;

import com.lab.lab_equipment_system.shared.entity.Equipment;
import com.lab.lab_equipment_system.shared.enums.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Equipment, Long> {

    Optional<Equipment> findByCode(String code);

    List<Equipment> findByStatus(EquipmentStatus status);

    List<Equipment> findByCategoryId(Long categoryId);

    @Query("SELECT d FROM Equipment d WHERE " +
           "(:keyword IS NULL OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR d.category.id = :categoryId) AND " +
           "(:status IS NULL OR d.status = :status) " +
           "ORDER BY d.id DESC")
    List<Equipment> searchDevices(@Param("keyword") String keyword,
                              @Param("categoryId") Long categoryId,
                              @Param("status") EquipmentStatus status);
}
