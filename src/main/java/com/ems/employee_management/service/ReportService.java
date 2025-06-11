package com.ems.employee_management.service;

import com.ems.employee_management.model.Employee;
import com.ems.employee_management.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class ReportService {

    private final EmployeeRepository empRepo;

    public ReportService(EmployeeRepository empRepo) { this.empRepo = empRepo; }

    public DepartmentStats stats(Long deptId) {
        List<Employee> emps = empRepo.findByDepartment_Id(deptId);
        int count = emps.size();
        BigDecimal total   = emps.stream()
                .map(Employee::getSalary)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = count > 0 ? total.divide(BigDecimal.valueOf(count), RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new DepartmentStats(count, avg);
    }

    public record DepartmentStats(int headcount, BigDecimal avgSalary) {}
}