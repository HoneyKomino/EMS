package com.ems.employee_management.controller;

import com.ems.employee_management.model.TimeOffRequest;
import com.ems.employee_management.model.User;
import com.ems.employee_management.model.Employee;
import com.ems.employee_management.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final ManagerService managerService;
    private final EmployeeService employeeService;
    private final TimeOffService timeOffService;
    private final ReportService reportService;
    private final UserService     userService;

    public ManagerController(ManagerService managerService,
                             EmployeeService employeeService, TimeOffService timeOffService, ReportService reportService,
                             UserService userService) {
        this.managerService = managerService;
        this.employeeService = employeeService;
        this.timeOffService = timeOffService;
        this.reportService = reportService;
        this.userService = userService;
    }

//    // 🟡 Dashboard → kendi departmanındaki çalışanları göster
//    // Dashboard – employees in my department
//    @GetMapping
//    public String viewMyDepartmentEmployees(@AuthenticationPrincipal UserDetails principal,
//                                            Model model) {
//        User me = userService.findByUsername(principal.getUsername());
//        Long deptId = managerService.getDepartmentIdByUser(me);
//        List<Employee> emps = employeeService.findEmployeesByDepartmentId(deptId);
//        model.addAttribute("employees", emps);
//        return "manager-employee-list";   // <── NEW TEMPLATE NAME
//    }

    /** 📋 Çalışanlarım (view‑only) */
    @GetMapping
    public String employees(@AuthenticationPrincipal UserDetails p, Model m) {
        Long deptId = managerService.getDepartmentIdByUser(
                userService.findByUsername(p.getUsername()));
        m.addAttribute("employees",
                employeeService.findEmployeesByDepartmentId(deptId));
        return "manager-employee-list";
    }

    /** 📅 İzin İstekleri */
    @GetMapping("/timeoff")
    public String timeOff(@AuthenticationPrincipal UserDetails p, Model m) {
        Long deptId = managerService.getDepartmentIdByUser(
                userService.findByUsername(p.getUsername()));
        m.addAttribute("requests", timeOffService.deptRequests(deptId));
        return "manager-timeoff-list";
    }

    @PostMapping("/timeoff/{id}/approve")
    public String approve(@PathVariable Long id) {
        timeOffService.changeStatus(id, TimeOffRequest.Status.APPROVED);
        return "redirect:/manager/timeoff";
    }
    @PostMapping("/timeoff/{id}/reject")
    public String reject(@PathVariable Long id) {
        timeOffService.changeStatus(id, TimeOffRequest.Status.REJECTED);
        return "redirect:/manager/timeoff";
    }

    /** 📊 Raporlar */
    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal UserDetails p, Model m) {
        Long deptId = managerService.getDepartmentIdByUser(
                userService.findByUsername(p.getUsername()));
        m.addAttribute("stats", reportService.stats(deptId));
        return "manager-reports";
    }

}
