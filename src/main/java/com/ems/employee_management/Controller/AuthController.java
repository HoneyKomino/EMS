package com.ems.employee_management.controller;

import com.ems.employee_management.model.User;
import com.ems.employee_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               Model model) {

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            bindingResult.reject("password.mismatch", "Passwords don't match");
        }

        if (!bindingResult.hasFieldErrors("username")) {
            if (userService.existsByUsername(user.getUsername())) {
                bindingResult.rejectValue("username", null, "This username is already in use");
            }
        }

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> System.out.println("⚠ Error: " + error));
            return "register";
        }

        userService.registerUser(user);
        System.out.println("User saved.");

        return "redirect:/login?registered=true";
    }
}
