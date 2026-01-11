package com.micropay.auth.repository;

import com.micropay.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find role by name
     * @param name role name
     * @return Optional Role
     */
    Optional<Role> findByName(String name);
}

