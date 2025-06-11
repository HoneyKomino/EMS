package com.ems.employee_management.service;

import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.Job;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    Employee findByUserId(Long userId);

    List<Employee> findEmployeesByDepartmentId(Long departmentId);

    /* ▼▼ NEW — used by manager job‑assignment screen ▼▼ */

    Optional<Employee> findById(Long id);

    List<Job> findAllJobs();          // list for the <select>

    void assignJob(Long empId, Long jobId);  // update employee.job

    List<Employee> findByDepartmentAndKeyword(Long deptId, String keyword);
}
