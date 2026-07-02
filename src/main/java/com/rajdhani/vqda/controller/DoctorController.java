package com.rajdhani.vqda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private com.rajdhani.vqda.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private com.rajdhani.vqda.repository.QueueRepository queueRepository;

    @Autowired
    private com.rajdhani.vqda.repository.DoctorRepository doctorRepository;

    @Autowired
    private com.rajdhani.vqda.repository.UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Doctor doctor = doctorRepository.findByUser(user).orElse(null);
            if (doctor != null) {
                // Get all appointments for this doctor
                model.addAttribute("appointmentsToday", appointmentRepository.countByDoctor(doctor));
                model.addAttribute("waiting", queueRepository.countByAppointmentDoctorAndStatus(doctor, "WAITING"));
                model.addAttribute("completed", queueRepository.countByAppointmentDoctorAndStatus(doctor, "COMPLETED"));
                
                // Next patient in queue
                com.rajdhani.vqda.model.Queue nextInQueue = queueRepository.findFirstByAppointmentDoctorAndStatusOrderByQueueNumberAsc(doctor, "WAITING").orElse(null);
                model.addAttribute("nextPatient", nextInQueue);
            }
        } else {
            model.addAttribute("appointmentsToday", 0);
            model.addAttribute("waiting", 0);
            model.addAttribute("completed", 0);
            model.addAttribute("nextPatient", null);
        }
        
        return "doctor-dashboard";
    }

    @GetMapping("/queue")
    public String todaysQueue(Model model, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Doctor doctor = doctorRepository.findByUser(user).orElse(null);
            if (doctor != null) {
                model.addAttribute("queueList", queueRepository.findByAppointmentDoctorOrderByQueueNumberAsc(doctor));
            }
        }
        return "queue";
    }
    @org.springframework.web.bind.annotation.PostMapping("/queue/next")
    public String callNextPatient(org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/login";
        
        com.rajdhani.vqda.model.Doctor doctor = doctorRepository.findByUser(user).orElse(null);
        if (doctor == null) return "redirect:/login";
        
        // 1. Find currently IN_PROGRESS and mark as COMPLETED
        java.util.Optional<com.rajdhani.vqda.model.Queue> currentOpt = queueRepository.findFirstByAppointmentDoctorAndStatusOrderByQueueNumberAsc(doctor, "IN_PROGRESS");
        if (currentOpt.isPresent()) {
            com.rajdhani.vqda.model.Queue current = currentOpt.get();
            current.setStatus("COMPLETED");
            current.setActualEndTime(java.time.LocalDateTime.now());
            queueRepository.save(current);
            
            // Also update the appointment status
            com.rajdhani.vqda.model.Appointment app = current.getAppointment();
            app.setStatus("COMPLETED");
            appointmentRepository.save(app);
        }
        
        // 2. Find next WAITING and mark as IN_PROGRESS
        java.util.Optional<com.rajdhani.vqda.model.Queue> nextOpt = queueRepository.findFirstByAppointmentDoctorAndStatusOrderByQueueNumberAsc(doctor, "WAITING");
        if (nextOpt.isPresent()) {
            com.rajdhani.vqda.model.Queue next = nextOpt.get();
            next.setStatus("IN_PROGRESS");
            next.setActualStartTime(java.time.LocalDateTime.now());
            queueRepository.save(next);
        }
        
        return "redirect:/doctor/queue";
    }
}
