package com.ems.employee_management.config;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addCsrfToken(Model model, CsrfToken token) {
        model.addAttribute("_csrf", token);
    }
}
