package com.ems.employee_management.controller;

import com.ems.employee_management.dto.TimeOffForm;
import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.TimeOffRequest;
import com.ems.employee_management.service.EmployeeService;
import com.ems.employee_management.service.TimeOffService;
import com.ems.employee_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;          // ← correct import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeSelfController {

    private final EmployeeService employeeService;
    private final TimeOffService  timeOffService;
    private final UserService     userService;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails p,
                            Model model) {

        Employee me = employeeService.findByUserId(
                userService.findByUsername(p.getUsername()).getId());

        model.addAttribute("me", me);
        model.addAttribute("myRequests",
                timeOffService.myRequests(me.getEmployeeId()));
        return "employee-dashboard";
    }

    /** show new‑request form */
    @GetMapping("/timeoff/new")
    public String newForm(Model model) {
        model.addAttribute("req", new TimeOffForm());
        return "employee-timeoff-form";
    }

    /** submit request */
    @PostMapping("/timeoff/save")
    public String save(@AuthenticationPrincipal UserDetails p,
                       @ModelAttribute TimeOffForm req,
                       RedirectAttributes ra) {

        Employee me = employeeService.findByUserId(
                userService.findByUsername(p.getUsername()).getId());

        timeOffService.createRequest(
                me, req.getStart(), req.getEnd(), req.getType());

        ra.addFlashAttribute("success", "Success.");
        return "redirect:/employee";
    }
}
