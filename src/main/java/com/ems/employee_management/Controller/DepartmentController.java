package com.ems.employee_management.controller;

import com.ems.employee_management.model.Department;
import com.ems.employee_management.model.User;
import com.ems.employee_management.repository.DepartmentRepository;

import com.ems.employee_management.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentController(DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        return "department-list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("department", new Department());

        // Only show users with ROLE_MANAGER
        List<User> managers = userRepository.findAll()
                .stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_MANAGER")))
                .toList();
        model.addAttribute("managers", managers);

        return "department-form";
    }

    @PostMapping("/save")
    public String saveDepartment(@ModelAttribute("department") Department department,
                                 @RequestParam(value = "managerId", required = false) Long managerId) {

        if (managerId != null && managerId > 0) {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));

            // ✅ Prevent assigning the same manager to multiple departments
            if (manager.getDepartment() != null &&
                    !manager.getDepartment().getId().equals(department.getId())) {
                return "redirect:/admin/departments?error=alreadyAssigned";
            }

            department.setManager(manager);
            manager.setDepartment(department);
        } else {
            department.setManager(null);
        }

        departmentRepository.save(department);
        return "redirect:/admin/departments";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable("id") Long id) {
        Department department = departmentRepository.findById(id).orElse(null);
        if (department != null) {
            // Remove manager reference
            if (department.getManager() != null) {
                User manager = department.getManager();
                manager.setDepartment(null); // also clear from manager
                userRepository.save(manager);
                department.setManager(null);
            }

            // Clear department reference from all employees
            if (department.getEmployees() != null) {
                department.getEmployees().forEach(e -> {
                    e.setDepartment(null);
                    if (e.getUser() != null) {
                        User user = e.getUser();
                        user.setDepartment(null);
                        userRepository.save(user); // save the change
                    }
                });
            }

            departmentRepository.save(department); // persist nullified manager/employees
            departmentRepository.delete(department); // now delete safely
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/edit/{id}")
    public String editDepartment(@PathVariable("id") Long id, Model model) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        model.addAttribute("department", department);

        List<User> managers = userRepository.findAll()
                .stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_MANAGER")))
                .toList();
        model.addAttribute("managers", managers);

        return "department-form";
    }
}
