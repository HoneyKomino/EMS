package com.ems.employee_management.service;

import com.ems.employee_management.model.Employee;

import java.util.List;

public interface EmployeeService {

    Employee findByUserId(Long userId);

    List<Employee> findEmployeesByDepartmentId(Long departmentId);
}
