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
        // Şifreyi hashle
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // "ROLE_USER" rolünü getir
       Role userRole = roleRepository.findByName("ROLE_USER")
    .orElseThrow(() -> new RuntimeException("ROLE_USER bulunamadı."));

        user.setRoles(Collections.singleton(userRole));

        // Kullanıcıyı veritabanına kaydet
        userRepository.save(user);
    }
}
