package com.be.repository;

import com.be.entity.User;
import com.be.common.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
