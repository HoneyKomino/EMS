package com.ems.employee_management.controller;

import com.ems.employee_management.model.Job;
import com.ems.employee_management.repository.JobRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/jobs")
public class JobController {

    private final JobRepository jobRepository;

    public JobController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping
    public String listJobs(Model model) {
        model.addAttribute("jobs", jobRepository.findAll());
        return "job-list";
    }

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("job", new Job());
        return "job-form";
    }

    @PostMapping("/save")
    public String saveJob(@ModelAttribute("job") Job job) {
        jobRepository.save(job);
        return "redirect:/admin/jobs";
    }

    @GetMapping("/delete/{id}")
    public String deleteJob(@PathVariable("id") Long id) {
        jobRepository.deleteById(id);
        return "redirect:/admin/jobs";
    }
}
