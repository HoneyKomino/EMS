package com.ems.employee_management.controller;

import com.ems.employee_management.model.Department;
import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.User;
import com.ems.employee_management.repository.DepartmentRepository;
import com.ems.employee_management.repository.EmployeeRepository;
import com.ems.employee_management.repository.RoleRepository;
import com.ems.employee_management.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private EmployeeRepository employeeRepository;

    public AdminController(UserService userService, RoleRepository roleRepository, DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("makeManagerUrlPrefix", "/admin/users/make-manager/"); // ✅ Add this
        return "user-list"; // your HTML page name
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findUserById(id);
        if (user == null) {
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleRepository.findAll());
        return "edit-user";
    }

    @PostMapping("/edit/{id}")
    @Transactional
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") User formUser,   // ← NOT @Valid
                             BindingResult binding,
                             RedirectAttributes redirect,
                             Model model) {

        /* ─── 1. Load managed User ────────────────────────────────────────── */
        User user = userService.findUserById(id);

        /* ─── 2. Duplicate‑mail guard ─────────────────────────────────────── */
        if (!formUser.getEmail().equals(user.getEmail())
                && userService.existsByEmail(formUser.getEmail())) {
            binding.rejectValue("email", "error.user", "Bu e‑posta zaten kullanımda.");
        }

        /* ─── 3. Optional password change ────────────────────────────────── */
        if (formUser.getPassword() != null && !formUser.getPassword().isBlank()) {
            if (formUser.getPassword().length() < 6) {          // simple rule
                binding.rejectValue("password", "error.user",
                        "Şifre en az 6 karakter olmalı.");
            } else {
                user.setPassword(userService.encode(formUser.getPassword())); // helper
            }
        }

        /* ─── 4. Stop here on validation errors ───────────────────────────── */
        if (binding.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "edit-user";
        }

        /* ─── 5. Copy other fields & roles ───────────────────────────────── */
        user.setUsername  (formUser.getUsername());
        user.setEmail     (formUser.getEmail());
        user.setSuperAdmin(formUser.isSuperAdmin());

        if (formUser.isSuperAdmin()) {
            userService.assignRole(id, "ROLE_ADMIN");
        } else {
            userService.removeRole(id, "ROLE_ADMIN");
        }

        userService.updateUser(user);

        /* ─── 6. Keep linked Employee in sync ─────────────────────────────── */
        Employee emp = employeeRepository.findByUser(user);
        if (emp != null) {
            emp.setEmail(user.getEmail());
            emp.setDepartment(user.getDepartment());
            employeeRepository.save(emp);
        }

        /* ─── 7. Flash success & redirect back to the same edit page ─────── */
        redirect.addFlashAttribute("success", "Kullanıcı başarıyla güncellendi.");
        return "redirect:/admin/users/edit/" + id;
    }


    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/make-admin/{id}")
    public String makeAdmin(@PathVariable Long id) {
        userService.assignRole(id, "ROLE_ADMIN");
        return "redirect:/admin/users";
    }

    @GetMapping("/make-manager/{id}")
    public String showDepartmentAssignPage(@PathVariable Long id, Model model) {
        User user = userService.findUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("departments", departmentRepository.findAll());
        return "assign-department-to-manager";
    }

    @PostMapping("/make-manager/{id}")
    public String assignManagerToDepartment(@PathVariable Long id,
                                            @RequestParam("departmentId") Long departmentId,
                                            RedirectAttributes redirectAttributes) {
        userService.assignRole(id, "ROLE_MANAGER");

        User manager = userService.findUserById(id);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Departman bulunamadı"));

        // ✅ Check if department already has a manager
        if (department.getManager() != null && !department.getManager().getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Bu departmanda zaten bir yönetici var.");
            return "redirect:/admin/users/make-manager/" + id;
        }



        department.setManager(manager);
        departmentRepository.save(department);

        manager.setDepartment(department);
        userService.updateUser(manager);

        redirectAttributes.addFlashAttribute("success", "Yönetici başarıyla atandı.");
        return "redirect:/admin/users";
    }

    @PostMapping("/unmake-manager/{id}")
    @Transactional
    public String unmakeManager(@PathVariable Long id,
                                RedirectAttributes redirect) {

        User manager = userService.findUserById(id);

        /* 1.  Detach from department (if any) */
        Department dept = departmentRepository.findByManager(manager);
        if (dept != null) {
            dept.setManager(null);
            departmentRepository.save(dept);
        }
        manager.setDepartment(null);

        /* 2.  Remove ROLE_MANAGER */
        userService.removeRole(id, "ROLE_MANAGER");

        /* 3.  Persist user */
        userService.updateUser(manager);

        redirect.addFlashAttribute("success", "Yönetici rolü kaldırıldı.");
        return "redirect:/admin/users";
    }

    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("formAction", "/admin/users/create");  // 👈 change this depending on context
        model.addAttribute("backUrl", "/admin/users");            // 👈 add this for the back button
        return "create-user";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute("user") @Valid User user,
                             BindingResult bindingResult,
                             @RequestParam("role") String roleName,
                             @RequestParam(name = "superAdmin", required = false) boolean superAdmin,
                             Model model) {

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", null, "Şifreler uyuşmuyor");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "create-user";
        }

        user.setSuperAdmin(superAdmin);
        userService.registerUser(user);
        userService.assignRole(user.getId(), roleName);
        return "redirect:/admin/users";
    }
}
