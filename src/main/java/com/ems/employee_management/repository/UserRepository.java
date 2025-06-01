package com.ems.employee_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ems.employee_management.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}