package com.ems.employee_management.service;

import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.Job;
import com.ems.employee_management.repository.EmployeeRepository;
import com.ems.employee_management.repository.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final JobRepository jobRepo;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, JobRepository jobRepo) {
        this.employeeRepository = employeeRepository;
        this.jobRepo = jobRepo;
    }

    @Override
    public Employee findByUserId(Long userId) {
        return employeeRepository.findByUser_Id(userId); // ✅ Doğru metot
    }

    @Override
    public List<Employee> findEmployeesByDepartmentId(Long departmentId) {
        return employeeRepository.findByDepartment_Id(departmentId); // ✅ Doğru metot
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public List<Job> findAllJobs() {
        return jobRepo.findAll();
    }

    @Override
    public List<Employee> findByDepartmentAndKeyword(Long deptId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return employeeRepository.findByDepartment_Id(deptId);
        }
        keyword = "%" + keyword.toLowerCase() + "%";
        return employeeRepository.searchInDept(deptId, keyword);
    }

    @Override
    @Transactional
    public void assignJob(Long empId, Long jobId) {
        Employee e = employeeRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Çalışan bulunamadı"));
        Job j = jobRepo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Pozisyon bulunamadı"));
        e.setJob(j);                  // JPA will persist on TX commit
    }
}
