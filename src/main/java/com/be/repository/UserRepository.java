package com.be.repository;

import com.be.entity.User;
import com.be.common.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByProviderIdAndAuthProvider(String providerId, String authProvider);

    @Query("select count(distinct u) from User u join u.userRoles ur where ur.role.name = :role")
    long countByRole(UserRole role);

    long countByIsActiveTrue();
    User findUserById(Long id);

    @Query("select u from User u " +
           "where (:role is null or u.role = :role) " +
           "and (:search is null or :search = '' or u.fullName like %:search% or u.email like %:search% or u.phone like %:search%)")
    Page<User> findAllFiltered(@Param("role") UserRole role, @Param("search") String search, Pageable pageable);
}

