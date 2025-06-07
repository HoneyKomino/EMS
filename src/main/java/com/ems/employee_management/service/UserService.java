package com.ems.employee_management.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    // ✅ İlk kullanıcı admin ve superadmin, diğerleri user
    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN bulunamadı."));
            user.setRoles(Collections.singleton(adminRole));
            user.setSuperAdmin(true);
        } else {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER bulunamadı."));
            user.setRoles(Collections.singleton(userRole));
            user.setSuperAdmin(false);
        }

        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void updateUser(User user) {
        if (user.getId() != null) {
            User existingUser = userRepository.findById(user.getId()).orElse(null);

            if (existingUser != null && existingUser.isSuperAdmin()) {
                // Super admin'in rollerini ve flag'ini değiştirme
                user.setSuperAdmin(true);
                user.setRoles(existingUser.getRoles());
            }

            if (existingUser != null && (user.getPassword() == null || user.getPassword().isEmpty())) {
                user.setPassword(existingUser.getPassword());
            } else {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }

        Set<Role> attachedRoles = user.getRoles().stream()
                .map(role -> roleRepository.findById(role.getId())
                        .orElseThrow(() -> new RuntimeException("Rol bulunamadı: " + role.getId())))
                .collect(Collectors.toSet());

        user.setRoles(attachedRoles);
        userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
        if (user.isSuperAdmin()) {
            throw new RuntimeException("Ana admin silinemez.");
        }
        userRepository.deleteById(id);
    }

    public List<User> findAdmins() {
        return userRepository.findAllAdmins();
    }

    // ✅ Bir kullanıcıya rol atar (örneğin admin yapmak için)
    public void assignRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        if (user.isSuperAdmin()) {
            throw new RuntimeException("Ana admin'in rolü değiştirilemez.");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Rol bulunamadı."));

        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }
}
