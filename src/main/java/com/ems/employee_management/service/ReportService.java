package com.ems.employee_management.service;

import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.Job;
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
        int headcount = emps.size();
        BigDecimal total = BigDecimal.ZERO;

        for (Employee e : emps) {
            Job j = e.getJob();
            if (j != null && j.getMinSalary() != null && j.getMaxSalary() != null) {
                BigDecimal mid =
                        BigDecimal.valueOf(j.getMinSalary())
                                .add(BigDecimal.valueOf(j.getMaxSalary()))
                                .divide(BigDecimal.valueOf(2));
                total = total.add(mid);
            }
        }

        BigDecimal avg = headcount > 0 ? total.divide(BigDecimal.valueOf(headcount), 2, RoundingMode.HALF_UP)
                : null;

        return new DepartmentStats(headcount, avg);
    }

    public record DepartmentStats(int headcount, BigDecimal avgSalary) {}
}