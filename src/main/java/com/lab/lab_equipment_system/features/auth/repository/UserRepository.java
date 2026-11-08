package com.lab.lab_equipment_system.features.auth.repository;

import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import com.lab.lab_equipment_system.shared.entity.User;
import com.lab.lab_equipment_system.shared.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    // The oldest account is a stable mutex: accounts are never physically deleted by this module.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findFirstByOrderByIdAsc();

    long countByRoleAndActiveTrue(UserRole role);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) " +
           "ORDER BY u.id DESC")
    List<User> searchUsers(@Param("keyword") String keyword, @Param("role") UserRole role);
}
