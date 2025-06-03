package com.ems.employee_management.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ems.employee_management.model.Role;
import com.ems.employee_management.repository.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);
            System.out.println("[INFO] ROLE_USER eklendi.");
        }

        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
            System.out.println("[INFO] ROLE_ADMIN eklendi.");
        }
    }
}
