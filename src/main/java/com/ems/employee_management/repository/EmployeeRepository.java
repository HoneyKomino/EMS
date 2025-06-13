package com.ems.employee_management.repository;

import com.ems.employee_management.model.Department;
import com.ems.employee_management.model.Employee;
import com.ems.employee_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Belirli bir kullanıcıya ait çalışanı bul
    Employee findByUser_Id(Long userId);

    List<Employee> findByDepartment_Id(Long departmentId);

    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrDepartment_DepartmentNameContainingIgnoreCase(
            String firstName, String lastName, String email, String departmentName);
    Employee findByUser(User user);

    @Query("""
   select e from Employee e
   where e.department.id = :dept
     and ( lower(e.firstName) like :kw
        or lower(e.lastName)  like :kw
        or lower(e.email)     like :kw )
""")
    List<Employee> searchInDept(@Param("dept") Long deptId,
                                @Param("kw")   String keyword);

}
