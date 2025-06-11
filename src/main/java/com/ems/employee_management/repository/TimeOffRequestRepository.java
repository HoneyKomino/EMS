package com.ems.employee_management.repository;

import com.ems.employee_management.model.TimeOffRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TimeOffRequestRepository
        extends JpaRepository<TimeOffRequest, Long> {

    // all requests in a department ordered by status
    @Query("""
           select r from TimeOffRequest r
           where r.employee.department.id = :deptId
           order by r.status, r.startDate
           """)
    List<TimeOffRequest> findByDepartment(Long deptId);

    List<TimeOffRequest>
    findByEmployeeEmployeeIdOrderByStartDateDesc(Long empId);
}
