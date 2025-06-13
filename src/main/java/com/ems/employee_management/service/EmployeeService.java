package com.ems.employee_management.service;

import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.Job;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    Employee findByUserId(Long userId);

    List<Employee> findEmployeesByDepartmentId(Long departmentId);


    Optional<Employee> findById(Long id);

    List<Job> findAllJobs();

    void assignJob(Long empId, Long jobId);

    List<Employee> findByDepartmentAndKeyword(Long deptId, String keyword);
}
