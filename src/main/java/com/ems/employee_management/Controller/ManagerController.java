package com.ems.employee_management.controller;

import com.ems.employee_management.model.User;
import com.ems.employee_management.model.Employee;
import com.ems.employee_management.service.EmployeeService;
import com.ems.employee_management.service.ManagerService;
import com.ems.employee_management.service.UserService;
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
    private final UserService userService;

    public ManagerController(ManagerService managerService,
                             EmployeeService employeeService,
                             UserService userService) {
        this.managerService = managerService;
        this.employeeService = employeeService;
        this.userService = userService;
    }

    // 🟡 Dashboard → kendi departmanındaki çalışanları göster
    @GetMapping
    public String viewMyDepartmentEmployees(@AuthenticationPrincipal UserDetails userDetails,
                                            Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Long departmentId = managerService.getDepartmentIdByUser(user);
        List<Employee> employees = employeeService.findEmployeesByDepartmentId(departmentId);
        model.addAttribute("employees", employees);
        return "manager-dashboard";
    }

    // 🟡 Kendi departmanındaki kullanıcıları listele
    @GetMapping("/users")
    public String viewMyDepartmentUsers(@AuthenticationPrincipal UserDetails userDetails,
                                        Model model) {
        String username = userDetails.getUsername();
        List<User> departmentUsers = userService.findUsersByManagerUsername(username);
        model.addAttribute("users", departmentUsers);
        return "user-list";
    }

    // 🟢 Yeni kullanıcı formu
    @GetMapping("/users/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        return "create-user";
    }

    // 🟢 Kullanıcı oluşturma işlemi
    @PostMapping("/users/create")
    public String createUser(@AuthenticationPrincipal UserDetails userDetails,
                             @ModelAttribute("user") @Valid User user,
                             BindingResult bindingResult,
                             Model model) {
        User manager = userService.findByUsername(userDetails.getUsername());

        if (bindingResult.hasErrors()) {
            return "create-user";
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", null, "Şifreler uyuşmuyor");
            return "create-user";
        }

        if (manager.getDepartment() == null) {
            model.addAttribute("error", "Departmanınız tanımlı değil. Kullanıcı ekleyemezsiniz.");
            return "create-user";
        }

        user.setDepartment(manager.getDepartment());
        userService.registerUser(user); // zaten default olarak USER rolü atanıyor
        return "redirect:/manager/users";
    }
}
