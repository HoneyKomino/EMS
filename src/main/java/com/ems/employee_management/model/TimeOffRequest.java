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