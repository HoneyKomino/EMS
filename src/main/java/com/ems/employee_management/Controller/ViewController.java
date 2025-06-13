package com.ems.employee_management.controller;

import com.ems.employee_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    private final UserService userService;

    @Autowired
    public ViewController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "registered", defaultValue = "false") boolean registered,
                            Model model) {
        System.out.println("🟢 Login. registered = " + registered);

        if (registered) {
            model.addAttribute("successMessage", "Kayıt başarılı! Giriş yapabilirsiniz.");
        }

        return "login";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }
}
