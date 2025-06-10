package com.ems.employee_management.service;

import com.ems.employee_management.model.Employee;
import com.ems.employee_management.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee findByUserId(Long userId) {
        return employeeRepository.findByUser_Id(userId); // ✅ Doğru metot
    }

    @Override
    public List<Employee> findEmployeesByDepartmentId(Long departmentId) {
        return employeeRepository.findByDepartment_Id(departmentId); // ✅ Doğru metot
    }
}
