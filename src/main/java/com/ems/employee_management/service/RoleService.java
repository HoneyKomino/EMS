package com.ems.employee_management.service;

import com.ems.employee_management.model.Role;

public interface RoleService {
    Role findByName(String name);
}
