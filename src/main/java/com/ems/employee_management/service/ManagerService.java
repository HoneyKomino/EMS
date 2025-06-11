package com.ems.employee_management.service;

import com.ems.employee_management.model.Department;
import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.User;

public interface ManagerService {
    Long getDepartmentIdByUser(User user);

    void linkManager(Employee emp, Department department);

    void unlinkManager(Employee emp);
}
