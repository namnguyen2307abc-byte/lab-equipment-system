package com.lab.lab_equipment_system.equipment.repository;

import com.lab.lab_equipment_system.equipment.entity.Device;
import com.lab.lab_equipment_system.equipment.entity.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByCode(String code);

    List<Device> findByStatus(DeviceStatus status);

    List<Device> findByCategoryId(Long categoryId);

    @Query("SELECT d FROM Device d WHERE " +
           "(:keyword IS NULL OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR d.category.id = :categoryId) AND " +
           "(:status IS NULL OR d.status = :status) " +
           "ORDER BY d.id DESC")
    List<Device> searchDevices(@Param("keyword") String keyword,
                              @Param("categoryId") Long categoryId,
                              @Param("status") DeviceStatus status);
}
