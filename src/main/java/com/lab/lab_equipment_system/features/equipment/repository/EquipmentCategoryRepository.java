package com.lab.lab_equipment_system.features.equipment.repository;

import com.lab.lab_equipment_system.shared.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface EquipmentCategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    List<Category> findAllByOrderByNameAsc();

    @Query("SELECT c.id, c.name, c.description, COUNT(e.id) FROM Category c " +
           "LEFT JOIN Equipment e ON e.category = c " +
           "GROUP BY c.id, c.name, c.description ORDER BY c.name ASC")
    List<Object[]> findAllCategoriesWithDeviceCount();
}
