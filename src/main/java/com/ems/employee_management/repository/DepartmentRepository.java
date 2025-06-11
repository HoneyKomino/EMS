package com.ems.employee_management.repository;

import com.ems.employee_management.model.Department;
import com.ems.employee_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByDepartmentNameContainingIgnoreCaseOrManager_UsernameContainingIgnoreCase(String departmentName, String managerUsername);
    Department findByManager(User manager);
}
