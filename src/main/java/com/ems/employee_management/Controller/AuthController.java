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

    // ✅ Kayıt formunu göstermek için GET endpoint'i
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // ✅ Kayıt işlemini gerçekleştiren POST endpoint
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               Model model) {

        // 🔐 Şifre eşleşme kontrolü
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            bindingResult.reject("password.mismatch", "Şifreler uyuşmuyor");
        }

        // 👤 Kullanıcı adı zaten var mı kontrolü
        if (!bindingResult.hasFieldErrors("username")) {
            if (userService.existsByUsername(user.getUsername())) {
                bindingResult.rejectValue("username", null, "Bu kullanıcı adı zaten mevcut");
            }
        }

        // ❌ Validasyon hatası varsa form tekrar gösterilsin
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> System.out.println("⚠️ Hata: " + error));
            return "register";
        }

        // ✅ Kullanıcıyı kaydet
        userService.registerUser(user);
        System.out.println("✅ Kullanıcı kayıt edildi, login sayfasına yönlendiriliyor.");

        return "redirect:/login?registered=true";
    }
}
