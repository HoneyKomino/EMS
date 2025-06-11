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
                                 @RequestParam("managerId") Long managerId) {
        if (managerId != null && managerId > 0) {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            department.setManager(manager);
            manager.setDepartment(department); // optional
        }
        departmentRepository.save(department);
        return "redirect:/admin/departments";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable("id") Long id) {
        departmentRepository.deleteById(id);
        return "redirect:/admin/departments";
    }
}
