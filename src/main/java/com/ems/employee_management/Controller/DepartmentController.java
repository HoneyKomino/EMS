package com.ems.employee_management.controller;

import com.ems.employee_management.model.Department;
import com.ems.employee_management.model.User;
import com.ems.employee_management.repository.DepartmentRepository;

import com.ems.employee_management.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String listDepartments(@RequestParam(value = "keyword", required = false) String keyword,
                                  Model model) {
        List<Department> departments;

        if (keyword != null && !keyword.isEmpty()) {
            departments = departmentRepository.findByDepartmentNameContainingIgnoreCaseOrManager_UsernameContainingIgnoreCase(keyword, keyword);
        } else {
            departments = departmentRepository.findAll();
        }

        model.addAttribute("departments", departments);
        model.addAttribute("keyword", keyword);
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
                                 @RequestParam(value = "managerId", required = false) Long managerId,
                                 RedirectAttributes redirectAttributes) {

        boolean isEdit = department.getId() != null;

        if (managerId != null && managerId > 0) {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));

            Department assignedDept = manager.getDepartment();

            // 🛑 Prevent assigning manager already managing another department (not this one)
            if (assignedDept != null && (department.getId() == null || !assignedDept.getId().equals(department.getId()))) {
                redirectAttributes.addFlashAttribute("error", "Seçilen yönetici zaten başka bir departmana atanmış.");
                return isEdit
                        ? "redirect:/admin/departments/edit/" + department.getId()
                        : "redirect:/admin/departments/new";
            }

            department.setManager(manager);
        } else {
            department.setManager(null);
        }

        departmentRepository.save(department);

        if (department.getManager() != null) {
            User manager = department.getManager();
            manager.setDepartment(department);
            userRepository.save(manager);
        }

        return "redirect:/admin/departments";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable("id") Long id) {
        Department department = departmentRepository.findById(id).orElse(null);
        if (department != null) {

            // Unset manager
            if (department.getManager() != null) {
                User manager = department.getManager();
                manager.setDepartment(null);
                department.setManager(null);
                userRepository.save(manager);
            }

            // Unset employees (if applicable)
            if (department.getEmployees() != null) {
                department.getEmployees().forEach(emp -> {
                    if (emp.getUser() != null) {
                        User empUser = emp.getUser();
                        empUser.setDepartment(null);
                        userRepository.save(empUser);
                    }
                    emp.setDepartment(null);
                });
            }

            departmentRepository.save(department); // persist null references
            departmentRepository.delete(department); // now safe to delete
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
