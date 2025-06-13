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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping
    public String employees(@AuthenticationPrincipal UserDetails p,
                            @RequestParam(required = false) String keyword,
                            Model m) {

        User me = userService.findByUsername(p.getUsername());
        Long deptId = managerService.getDepartmentIdByUser(me);

        m.addAttribute("employees",
                employeeService.findByDepartmentAndKeyword(deptId, keyword));
        m.addAttribute("keyword", keyword);
        return "manager-employee-list";
    }

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

    @GetMapping("/reports")
    public String reports(@AuthenticationPrincipal UserDetails p, Model m) {
        Long deptId = managerService.getDepartmentIdByUser(
                userService.findByUsername(p.getUsername()));
        m.addAttribute("stats", reportService.stats(deptId));
        return "manager-reports";
    }

    @GetMapping("/employees/{empId}/edit-job")
    public String showJobForm(@PathVariable Long empId,
                              @AuthenticationPrincipal UserDetails p,
                              Model model) {

        User me      = userService.findByUsername(p.getUsername());
        Long deptId  = managerService.getDepartmentIdByUser(me);
        Employee emp = employeeService.findById(empId)
                .orElseThrow(() -> new RuntimeException("Çalışan bulunamadı"));

        if (!emp.getDepartment().getId().equals(deptId)) {
            return "redirect:/manager";
        }

        model.addAttribute("employee", emp);
        model.addAttribute("jobs",    employeeService.findAllJobs()); // simple helper
        return "manager-assign-job";
    }

    @PostMapping("/employees/{empId}/edit-job")
    public String saveJob(@PathVariable Long empId,
                          @RequestParam Long jobId,
                          @AuthenticationPrincipal UserDetails p,
                          RedirectAttributes ra) {

        User me      = userService.findByUsername(p.getUsername());
        Long deptId  = managerService.getDepartmentIdByUser(me);
        Employee emp = employeeService.findById(empId)
                .orElseThrow(() -> new RuntimeException("Çalışan bulunamadı"));

        if (!emp.getDepartment().getId().equals(deptId)) {
            return "redirect:/manager";
        }

        employeeService.assignJob(empId, jobId);
        ra.addFlashAttribute("success", "Pozisyon güncellendi.");
        return "redirect:/manager";
    }

}
