package com.ems.employee_management.controller;

import com.ems.employee_management.model.User;
import com.ems.employee_management.repository.RoleRepository;
import com.ems.employee_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public AdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "user-list";
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
                             @RequestParam(name = "superAdmin", required = false) boolean superAdmin,
                             @RequestParam(name = "roles", required = false) List<String> selectedRoles,
                             Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            return "edit-user";
        }

        updatedUser.setId(id);
        updatedUser.setSuperAdmin(superAdmin);
        userService.updateUserWithRoles(updatedUser, selectedRoles);
        return "redirect:/admin/users";
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

    @PostMapping("/make-manager/{id}")
    public String makeManager(@PathVariable Long id) {
        userService.assignRole(id, "ROLE_MANAGER");
        return "redirect:/admin/users";
    }

    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleRepository.findAll());
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
