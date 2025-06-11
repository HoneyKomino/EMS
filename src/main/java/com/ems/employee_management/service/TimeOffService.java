package com.ems.employee_management.service;

import com.ems.employee_management.model.TimeOffRequest;
import com.ems.employee_management.repository.TimeOffRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimeOffService {

    private final TimeOffRequestRepository repo;

    public TimeOffService(TimeOffRequestRepository repo) { this.repo = repo; }

    public List<TimeOffRequest> deptRequests(Long deptId) {
        return repo.findByDepartment(deptId);
    }

    @Transactional
    public void changeStatus(Long requestId, TimeOffRequest.Status newStatus) {
        TimeOffRequest req = repo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("İstek bulunamadı"));
        req.setStatus(newStatus);
    }
}
