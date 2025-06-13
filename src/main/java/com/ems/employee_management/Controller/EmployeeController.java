package com.ems.employee_management.controller;

import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.User;
import com.ems.employee_management.repository.EmployeeRepository;
import com.ems.employee_management.repository.UserRepository;
import com.ems.employee_management.repository.DepartmentRepository;
import com.ems.employee_management.repository.JobRepository;

import com.ems.employee_management.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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
        model.addAttribute("keyword", keyword);
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
    @Transactional
    public String saveEmployee(@Valid @ModelAttribute("employee") Employee form,
                               BindingResult binding,
                               Model model) {

        /* 1 – Load or create the managed Employee */
        boolean creating = form.getEmployeeId() == null;

        Employee emp = creating
                ? new Employee()
                : employeeRepository.findById(form.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("No emp found"));

        emp.setFirstName (form.getFirstName());
        emp.setLastName  (form.getLastName());
        emp.setDepartment(form.getDepartment());
        emp.setJob       (form.getJob());
        emp.setEmail     (form.getEmail());

        /* 2 – Find any User that already owns this e‑mail */
        User emailOwner = userService.findByEmailOptional(emp.getEmail()).orElse(null);

        /* 2.a  AUTO‑LINK when editing and user is currently NULL */
        if (!creating && emp.getUser() == null && emailOwner != null) {
            emp.setUser(emailOwner);          // attach the missing link
        }

        /* 3 – Duplicate‑mail guard */
        boolean mailOwnedByOther =
                emailOwner != null &&
                        (emp.getUser() == null ||
                                !emailOwner.getId().equals(emp.getUser().getId()));

        if (mailOwnedByOther) {
            binding.rejectValue("email", "error.employee", "This employee already exists.");
        }

        /* 4 – Create a brand‑new user when adding a NEW employee */
        if (creating && emp.getUser() == null && !binding.hasErrors()) {

            String base = emp.getFirstName().toLowerCase().replaceAll("\\s+", "");
            String uname = base;
            for (int i = 1; userService.existsByUsername(uname); i++) {
                uname = base + i;
            }

            User nu = new User();
            nu.setUsername(uname);
            nu.setEmail   (emp.getEmail());
            nu.setPassword("default123");
            nu.setConfirmPassword("default123");
            nu.setDepartment(emp.getDepartment());

            userService.registerUser(nu);
            userService.assignRole(nu.getId(), "ROLE_USER");

            emp.setUser(nu);
        }

        /* 5 – Sync existing linked User (email, department, username) */
        if (!binding.hasErrors() && emp.getUser() != null) {

            User u = emp.getUser();                        // managed

            u.setEmail(emp.getEmail());
            u.setDepartment(emp.getDepartment());

            String desiredBase = emp.getFirstName().toLowerCase().replaceAll("\\s+", "");
            if (!u.getUsername().equals(desiredBase)) {
                String cand = desiredBase;
                int i = 1;
                while (userService.existsByUsername(cand) &&
                        !cand.equals(u.getUsername())) {
                    cand = desiredBase + i++;
                }
                u.setUsername(cand);
            }

            userService.updateUser(u);
        }

        /* 6 – Redisplay form on validation errors */
        if (binding.hasErrors()) {
            model.addAttribute("users",       userRepository.findAll());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("jobs",        jobRepository.findAll());
            return "employee-form";
        }

        employeeRepository.save(emp);
        return "redirect:/admin/employees";
    }


    private void syncUserFromEmployee(Employee e) {
        User u = e.getUser();
        u.setEmail(e.getEmail());
        u.setDepartment(e.getDepartment());

        String desiredBase = e.getFirstName().toLowerCase().replaceAll("\\s+", "");
        if (!u.getUsername().startsWith(desiredBase)) {
            String candidate = desiredBase;
            for (int i = 1; userService.existsByUsername(candidate)
                    && !candidate.equals(u.getUsername()); i++) {
                candidate = desiredBase + i;
            }
            u.setUsername(candidate);
        }
        userService.updateUser(u);                  // persists the User
    }


    @GetMapping("/edit/{id}")
    public String editEmployee(@PathVariable("id") Long id, Model model) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID: " + id));
        model.addAttribute("employee", employee);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("jobs", jobRepository.findAll());
        return "employee-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID: " + id));

        User user = employee.getUser();

        // Break the link to prevent cascading issues
        employee.setUser(null);
        employeeRepository.delete(employee);

        // Delete the user if exists
        if (user != null) {
            userService.deleteUserById(user.getId());
        }

        return "redirect:/admin/employees";
    }
}
