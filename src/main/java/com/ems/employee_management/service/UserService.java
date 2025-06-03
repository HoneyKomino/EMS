package com.ems.employee_management.service;

import java.util.Collections;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ems.employee_management.model.Role;
import com.ems.employee_management.model.User;
import com.ems.employee_management.repository.RoleRepository;
import com.ems.employee_management.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

   public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
}


    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER bulunamadı."));

        // JPA'nın düzgün ilişkilendirme yapması için Role objesini attach et
        Role attachedRole = roleRepository.getReferenceById(userRole.getId());

        user.setRoles(Collections.singleton(attachedRole));
        userRepository.save(user);
    }

}
