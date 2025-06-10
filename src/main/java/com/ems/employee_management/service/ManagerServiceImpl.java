package com.ems.employee_management.service;

import com.ems.employee_management.model.User;
import com.ems.employee_management.repository.ManagerRepository;
import org.springframework.stereotype.Service;

@Service
public class ManagerServiceImpl implements ManagerService {

    private final ManagerRepository managerRepository;

    public ManagerServiceImpl(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    @Override
    public Long getDepartmentIdByUser(User user) {
        return managerRepository.findDepartmentIdByUserId(user.getId());
    }
}
