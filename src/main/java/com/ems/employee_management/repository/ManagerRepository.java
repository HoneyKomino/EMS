package com.ems.employee_management.repository;

import com.ems.employee_management.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {

    @Query("SELECT m.department.id FROM Manager m WHERE m.employee.user.id = :userId")
    Long findDepartmentIdByUserId(@Param("userId") Long userId);
}
