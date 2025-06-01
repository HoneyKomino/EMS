package com.ems.employee_management.model;

import java.util.Set;

public class User {
    private String username;
    private String password;
    private Set<Role> roles;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}