package com.ems.employee_management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ems.employee_management.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
