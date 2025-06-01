package com.ems.employee_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ems.employee_management.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}