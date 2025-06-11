package com.ems.employee_management.controller;

import com.ems.employee_management.model.Department;
import com.ems.employee_management.model.User;
import com.ems.employee_management.repository.DepartmentRepository;
import com.ems.employee_management.repository.RoleRepository;
import com.ems.employee_management.service.UserService;
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

    public AdminController(UserService userService, RoleRepository roleRepository, DepartmentRepository departmentRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
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
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute("user") User updatedUser,
                             BindingResult bindingResult,
                             Model model) {

        if (bindingResult.hasErrors()) {
            return "edit-user";
        }

        User existingUser = userService.findUserById(id);

        // Update basic fields
        existingUser.setSuperAdmin(updatedUser.isSuperAdmin());

        if (updatedUser.isSuperAdmin()) {
            // Ensure ROLE_ADMIN is assigned
            userService.assignRole(id, "ROLE_ADMIN");
        } else {
            // Remove ROLE_ADMIN if it exists
            userService.removeRole(id, "ROLE_ADMIN");
        }

        userService.updateUser(existingUser);

        return "redirect:/admin/users?success";
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
