package com.ems.employee_management.service;

import com.ems.employee_management.model.User;

public interface ManagerService {
    Long getDepartmentIdByUser(User user);
}
