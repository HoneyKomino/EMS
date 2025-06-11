package com.ems.employee_management.service;

import java.util.*;
import java.util.stream.Collectors;

import com.ems.employee_management.model.Employee;
import com.ems.employee_management.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final EmployeeRepository employeeRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeRepository = employeeRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));
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

    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
        if (user.isSuperAdmin()) {
            throw new RuntimeException("Ana admin silinemez.");
        }
        userRepository.deleteById(id);
    }

    public List<User> findAdmins() {
        return userRepository.findAllAdmins();
    }

    public List<User> findUsersByManagerUsername(String username) {
        User manager = findByUsername(username);
        if (manager.getDepartment() == null) {
            return List.of();
        }
        return userRepository.findByDepartmentId(manager.getDepartment().getId());
    }

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user); // Save to generate ID

        Set<Role> roleSet = new HashSet<>();
        if (user.isSuperAdmin()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN bulunamadı."));
            roleSet.add(adminRole);
        } else {
            Role role = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER bulunamadı."));
            roleSet.add(role);
        }

        user.setRoles(roleSet);
        userRepository.save(user); // Save again with roles

        // ✅ Auto-create Employee
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setEmail(user.getEmail()); // Set fields based on user
        employee.setFirstName(user.getUsername()); // Or split name/email as needed
        employeeRepository.save(employee);
    }

    @Transactional
    public void assignRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Rol bulunamadı: " + roleName));

        if (!user.getRoles().contains(role)) {     // 👈 skip if already present
            user.getRoles().add(role);
            userRepository.save(user);
        }
    }

    public void updateUserWithRoles(User user, List<String> selectedRoleNames) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(existingUser.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        Set<Role> updatedRoles = selectedRoleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Rol bulunamadı: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(new HashSet<>(updatedRoles)); // 🔧 Hata burada da olabilir
        user.setSuperAdmin(user.isSuperAdmin());
        userRepository.save(user);
    }
}
