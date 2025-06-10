package com.ems.employee_management.repository;

import com.ems.employee_management.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Belirli bir kullanıcıya ait çalışanı bul
    Employee findByUser_Id(Long userId); // Daha doğru: `userId` yerine `user_Id`

    // Belirli bir departmana ait çalışanları bul
    List<Employee> findByDepartment_Id(Long departmentId);
}
