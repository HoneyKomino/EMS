package com.ems.employee_management.model;

import com.ems.employee_management.model.Employee;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "time_off_requests")
public class TimeOffRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "employee_id")
    private Employee employee;

    public Employee getEmployee() {
        return employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private Type type;     // VACATION, SICK, UNPAID …

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, APPROVED, REJECTED

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    // getters / setters …

    public enum Type   { VACATION, SICK, UNPAID }   // extend as needed
    public enum Status { PENDING, APPROVED, REJECTED }
}