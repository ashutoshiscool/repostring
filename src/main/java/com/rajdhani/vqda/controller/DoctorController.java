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
                model.addAttribute("doctor", doctor);
                // Get all appointments for this doctor
                model.addAttribute("appointmentsToday", appointmentRepository.countByDoctor(doctor));
                model.addAttribute("waiting", queueRepository.countByAppointmentDoctorAndStatus(doctor, "WAITING"));
                model.addAttribute("completed", queueRepository.countByAppointmentDoctorAndStatus(doctor, "COMPLETED"));
                
                // Next patient in queue
                com.rajdhani.vqda.model.Queue nextInQueue = queueRepository.findFirstByAppointmentDoctorAndStatusOrderByPriorityScoreDescQueueNumberAsc(doctor, "WAITING").orElse(null);
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
                model.addAttribute("queueList", queueRepository.findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(doctor));
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
        java.util.Optional<com.rajdhani.vqda.model.Queue> currentOpt = queueRepository.findFirstByAppointmentDoctorAndStatusOrderByPriorityScoreDescQueueNumberAsc(doctor, "IN_PROGRESS");
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
        java.util.Optional<com.rajdhani.vqda.model.Queue> nextOpt = queueRepository.findFirstByAppointmentDoctorAndStatusOrderByPriorityScoreDescQueueNumberAsc(doctor, "WAITING");
        if (nextOpt.isPresent()) {
            com.rajdhani.vqda.model.Queue next = nextOpt.get();
            next.setStatus("IN_PROGRESS");
            next.setActualStartTime(java.time.LocalDateTime.now());
            queueRepository.save(next);
        }
        
        return "redirect:/doctor/queue";
    }

    @org.springframework.web.bind.annotation.PostMapping("/availability")
    public String toggleAvailability(org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Doctor doctor = doctorRepository.findByUser(user).orElse(null);
            if (doctor != null) {
                doctor.setAvailabilityStatus(!doctor.isAvailabilityStatus());
                doctorRepository.save(doctor);
            }
        }
        return "redirect:/doctor/dashboard?success=availability_updated";
    }

    @org.springframework.web.bind.annotation.PostMapping("/queue/{queueId}/status")
    public String updateQueueStatus(@org.springframework.web.bind.annotation.PathVariable Long queueId, @org.springframework.web.bind.annotation.RequestParam("status") String status, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Doctor doctor = doctorRepository.findByUser(user).orElse(null);
            if (doctor != null) {
                com.rajdhani.vqda.model.Queue q = queueRepository.findById(queueId).orElse(null);
                if (q != null && q.getAppointment().getDoctor().getId().equals(doctor.getId())) {
                    q.setStatus(status);
                    if ("IN_PROGRESS".equals(status)) {
                        q.setActualStartTime(java.time.LocalDateTime.now());
                    } else if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
                        q.setActualEndTime(java.time.LocalDateTime.now());
                        com.rajdhani.vqda.model.Appointment app = q.getAppointment();
                        app.setStatus(status);
                        appointmentRepository.save(app);
                    }
                    queueRepository.save(q);
                }
            }
        }
        return "redirect:/doctor/queue";
    }

    @Autowired
    private com.rajdhani.vqda.repository.PatientRepository patientRepository;

    @GetMapping("/patient/{id}")
    public String viewPatientDetails(@org.springframework.web.bind.annotation.PathVariable Long id, Model model, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Doctor doctor = doctorRepository.findByUser(user).orElse(null);
            if (doctor != null) {
                com.rajdhani.vqda.model.Patient patient = patientRepository.findById(id).orElse(null);
                if (patient != null) {
                    model.addAttribute("patient", patient);
                    return "doctor-patient-details";
                }
            }
        }
        return "redirect:/doctor/queue";
    }

    @GetMapping("/queue/{id}/reschedule")
    public String rescheduleQueueForm(@org.springframework.web.bind.annotation.PathVariable Long id, Model model, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Doctor doctor = doctorRepository.findByUser(user).orElse(null);
            if (doctor != null) {
                com.rajdhani.vqda.model.Queue q = queueRepository.findById(id).orElse(null);
                if (q != null && q.getAppointment().getDoctor().getId().equals(doctor.getId()) && "WAITING".equals(q.getStatus())) {
                    model.addAttribute("queue", q);
                    return "doctor-reschedule";
                }
            }
        }
        return "redirect:/doctor/queue?error=InvalidQueue";
    }

    @org.springframework.web.bind.annotation.PostMapping("/queue/{id}/reschedule")
    public String submitQueueReschedule(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam("appointmentDate") String appointmentDateStr,
            @org.springframework.web.bind.annotation.RequestParam("timeSlot") String timeSlotStr,
            org.springframework.security.core.Authentication authentication) {
        
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Doctor doctor = doctorRepository.findByUser(user).orElse(null);
            if (doctor != null) {
                com.rajdhani.vqda.model.Queue q = queueRepository.findById(id).orElse(null);
                if (q != null && q.getAppointment().getDoctor().getId().equals(doctor.getId())) {
                    java.time.LocalDate newDate = java.time.LocalDate.parse(appointmentDateStr);
                    java.time.LocalTime newTime = java.time.LocalTime.parse(timeSlotStr);
                    
                    com.rajdhani.vqda.model.Appointment appointment = q.getAppointment();
                    appointment.setAppointmentDate(newDate);
                    appointment.setTimeSlot(newTime);
                    appointmentRepository.save(appointment);
                    
                    boolean isToday = newDate.equals(java.time.LocalDate.now());
                    if (!isToday) {
                        q.setStatus("CANCELLED");
                        queueRepository.save(q);
                    }
                    
                    return "redirect:/doctor/queue?success=Rescheduled";
                }
            }
        }
        return "redirect:/doctor/queue?error=Failed";
    }
}
