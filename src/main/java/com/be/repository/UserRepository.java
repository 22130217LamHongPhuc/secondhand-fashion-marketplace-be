package com.be.repository;

import com.be.entity.User;
import com.be.common.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByProviderIdAndAuthProvider(String providerId, String authProvider);

    long countByRole(UserRole role);
}

