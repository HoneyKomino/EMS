package com.ems.employee_management.controller;

import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.User;
import com.ems.employee_management.repository.EmployeeRepository;
import com.ems.employee_management.repository.UserRepository;
import com.ems.employee_management.repository.DepartmentRepository;
import com.ems.employee_management.repository.JobRepository;

import com.ems.employee_management.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRepository jobRepository;
    private final UserService userService;

    public EmployeeController(EmployeeRepository employeeRepository,
                              UserRepository userRepository,
                              DepartmentRepository departmentRepository,
                              JobRepository jobRepository, UserService userService) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.jobRepository = jobRepository;
        this.userService = userService;
    }

    @GetMapping
    public String listEmployees(@RequestParam(value = "keyword", required = false) String keyword,
                                Model model) {
        List<Employee> employees;

        if (keyword != null && !keyword.isEmpty()) {
            employees = employeeRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrDepartment_DepartmentNameContainingIgnoreCase(
                            keyword, keyword, keyword, keyword);
        } else {
            employees = employeeRepository.findAll();
        }

        model.addAttribute("employees", employees);
        model.addAttribute("keyword", keyword); // to preserve search term in view
        return "employee-list";
    }

    @GetMapping("/new")
    public String showEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("jobs", jobRepository.findAll());
        return "employee-form";
    }

    @PostMapping("/save")
    public String saveEmployee(@ModelAttribute("employee") Employee employee,
                               BindingResult bindingResult,
                               Model model) {

        boolean isCreatingNew = (employee.getEmployeeId() == null);

        // Check if email is already used by a *different* user
        User existingUser = userService.findByEmailOptional(employee.getEmail()).orElse(null);
        boolean isUsedByOtherUser = existingUser != null &&
                (employee.getUser() == null || !existingUser.getId().equals(employee.getUser().getId()));

        if (isUsedByOtherUser) {
            bindingResult.rejectValue("email", "error.employee", "Bu e-posta zaten kullanımda.");
        }

        if (isCreatingNew && employee.getUser() == null && !bindingResult.hasErrors()) {
            String baseUsername = employee.getFirstName().toLowerCase().replaceAll("\\s+", "");
            String username = baseUsername;
            int counter = 1;
            while (userService.existsByUsername(username)) {
                username = baseUsername + counter;
                counter++;
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(employee.getEmail());
            user.setPassword("default123");
            user.setConfirmPassword("default123");
            user.setDepartment(employee.getDepartment());

            userService.registerUser(user);
            userService.assignRole(user.getId(), "ROLE_USER");

            employee.setUser(user);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("jobs", jobRepository.findAll());
            return "employee-form";
        }

        employeeRepository.save(employee);
        return "redirect:/admin/employees";
    }

    @GetMapping("/edit/{id}")
    public String editEmployee(@PathVariable("id") Long id, Model model) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz çalışan ID: " + id));
        model.addAttribute("employee", employee);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("jobs", jobRepository.findAll());
        return "employee-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id) {
        employeeRepository.deleteById(id);
        return "redirect:/admin/employees";
    }
}
