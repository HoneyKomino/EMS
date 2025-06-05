package com.ems.employee_management.controller;

import com.ems.employee_management.model.User;
import com.ems.employee_management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               Model model) {

        // 🔐 Şifre eşleşme kontrolü burada manuel yapılmalı
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            bindingResult.reject("password.mismatch", "Şifreler uyuşmuyor");
        }

        // 👤 Kullanıcı adı veritabanında var mı kontrol
        if (!bindingResult.hasFieldErrors("username")) {
            if (userService.existsByUsername(user.getUsername())) {
                bindingResult.rejectValue("username", null, "Bu kullanıcı adı zaten mevcut");
            }
        }

        // ❌ Hatalıysa formu tekrar göster
        if (bindingResult.hasErrors()) {
            System.out.println("❌ Kayıt formunda hata var, form tekrar gösteriliyor.");
            return "register";
        }

        // ✅ Kullanıcıyı kayıt et
        userService.registerUser(user);

        System.out.println("✅ Kullanıcı kayıt edildi, login sayfasına yönlendiriliyor: /login?registered=true");

        return "redirect:/login?registered=true";
    }
}
