package com.be.repository;

import com.be.entity.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Long> {
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
