package com.lab.lab_equipment_system.features.equipment.repository;

import com.lab.lab_equipment_system.features.equipment.entity.Device;
import com.lab.lab_equipment_system.features.equipment.entity.EquipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {

    Optional<Device> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    long countByCategoryId(Long categoryId);

    long countByStatus(EquipmentStatus status);

    long countByStatusIn(Collection<EquipmentStatus> statuses);

    List<Device> findByCategoryId(Long categoryId);

    List<Device> findByStatus(EquipmentStatus status);

    @Query("SELECT d FROM Device d JOIN FETCH d.category WHERE d.id = :id")
    Optional<Device> findByIdWithCategory(@Param("id") Long id);

    @Query("SELECT d FROM Device d JOIN FETCH d.category ORDER BY d.id DESC")
    List<Device> findAllWithCategory();

    @Query(value = "SELECT d FROM Device d JOIN FETCH d.category c " +
                   "WHERE (:keyword IS NULL OR :keyword = '' OR " +
                   "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "LOWER(d.specifications) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                   "AND (:categoryId IS NULL OR c.id = :categoryId) " +
                   "AND (:statuses IS NULL OR d.status IN :statuses)",
           countQuery = "SELECT COUNT(d) FROM Device d " +
                        "WHERE (:keyword IS NULL OR :keyword = '' OR " +
                        "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(d.specifications) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:categoryId IS NULL OR d.category.id = :categoryId) " +
                        "AND (:statuses IS NULL OR d.status IN :statuses)")
    Page<Device> searchAndFilter(@Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId,
                                 @Param("statuses") Collection<EquipmentStatus> statuses,
                                 Pageable pageable);
}
