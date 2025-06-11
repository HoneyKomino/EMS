package com.ems.employee_management.service;

import com.ems.employee_management.model.Department;
import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.Manager;
import com.ems.employee_management.model.User;
import com.ems.employee_management.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {

    private final ManagerRepository managerRepository;

    @Override
    public Long getDepartmentIdByUser(User user) {
        return managerRepository.findDepartmentIdByUserId(user.getId());
    }

    @Override
    public void linkManager(Employee emp, Department dept) {
        Manager m = managerRepository.findByEmployee(emp);
        if (m == null) m = new Manager();
        m.setEmployee(emp);
        m.setDepartment(dept);
        managerRepository.save(m);
    }

    @Override
    public void unlinkManager(Employee emp) {
        managerRepository.deleteByEmployee(emp);
    }
}
