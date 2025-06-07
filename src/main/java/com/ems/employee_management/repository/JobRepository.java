package com.ems.employee_management.repository;

import com.ems.employee_management.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}
