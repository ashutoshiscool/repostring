package com.rajdhani.vqda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private com.rajdhani.vqda.repository.DoctorRepository doctorRepository;
    
    @Autowired
    private com.rajdhani.vqda.repository.PatientRepository patientRepository;
    
    @Autowired
    private com.rajdhani.vqda.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private com.rajdhani.vqda.repository.QueueRepository queueRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalDoctors", doctorRepository.count());
        model.addAttribute("totalPatients", patientRepository.count());
        
        // Let's just pass some base stats
        model.addAttribute("appointmentsToday", appointmentRepository.count());
        model.addAttribute("inQueue", queueRepository.count());
        
        // For recent appointments, just pass all for now (we'll limit to top 5 in real app, but this works for demo)
        model.addAttribute("recentAppointments", appointmentRepository.findAll());
        return "admin-dashboard";
    }

    @GetMapping("/doctors")
    public String manageDoctors(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        return "manage-doctors";
    }
    
    @GetMapping("/patients")
    public String managePatients(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        return "manage-patients";
    }
}
