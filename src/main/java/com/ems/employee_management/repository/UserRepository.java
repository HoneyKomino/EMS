package com.ems.employee_management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ems.employee_management.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username); // doğru
}

