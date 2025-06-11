package com.ems.employee_management.service;

import java.util.*;
import java.util.stream.Collectors;

import com.ems.employee_management.model.Department;
import com.ems.employee_management.model.Employee;
import com.ems.employee_management.repository.DepartmentRepository;
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
    private final DepartmentRepository departmentRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
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

    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1. Remove this user from employee if exists
        Employee employee = employeeRepository.findByUser(user);
        if (employee != null) {
            employee.setUser(null);
            employeeRepository.save(employee);
            employeeRepository.delete(employee); // Optional: If you want to delete employee too
        }

        // 2. Remove manager from department if any
        Department dept = departmentRepository.findByManager(user);
        if (dept != null) {
            dept.setManager(null);
            departmentRepository.save(dept);
        }

        // 3. Now safe to delete user
        userRepository.delete(user);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> findByEmailOptional(String email) {
        return userRepository.findByEmail(email);
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

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public String encode(String raw) { return passwordEncoder.encode(raw); }

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

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

    public void removeRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        roleRepository.findByName(roleName).ifPresent(role -> {
            user.getRoles().remove(role);
            userRepository.save(user);
        });
    }

    public void updateUserWithRoles(User user, List<Long> roleIds) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        // Password update
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(existingUser.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Role update from IDs
        Set<Role> roles = roleIds.stream()
                .map(id -> roleRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Rol bulunamadı: " + id)))
                .collect(Collectors.toSet());

        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setRoles(roles);
        existingUser.setSuperAdmin(user.isSuperAdmin());

        userRepository.save(existingUser);
    }
}
