package com.rajdhani.vqda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/patient")
public class PatientController {
    @Autowired
    private com.rajdhani.vqda.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private com.rajdhani.vqda.repository.QueueRepository queueRepository;

    @Autowired
    private com.rajdhani.vqda.repository.PatientRepository patientRepository;

    @Autowired
    private com.rajdhani.vqda.repository.UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                // Get patient's name
                model.addAttribute("patientName", patient.getFirstName());
                
                // Get next appointment
                java.util.List<com.rajdhani.vqda.model.Appointment> apps = appointmentRepository.findByPatientOrderByAppointmentDateDesc(patient);
                if (!apps.isEmpty()) {
                    model.addAttribute("upcomingApp", apps.get(0));
                    
                    // See if they are in queue for this doc today
                    com.rajdhani.vqda.model.Queue q = queueRepository.findFirstByAppointmentDoctorAndStatusOrderByQueueNumberAsc(apps.get(0).getDoctor(), "WAITING").orElse(null);
                    if (q != null) {
                        model.addAttribute("queuePosition", q.getQueueNumber());
                        model.addAttribute("doctorName", "Dr. " + apps.get(0).getDoctor().getLastName());
                        model.addAttribute("specialization", apps.get(0).getDoctor().getSpecialization());
                    } else {
                        model.addAttribute("queuePosition", null);
                    }
                } else {
                    model.addAttribute("upcomingApp", null);
                    model.addAttribute("queuePosition", null);
                }
            }
        }
        return "patient-dashboard";
    }

    @Autowired
    private com.rajdhani.vqda.repository.DoctorRepository doctorRepository;

    @GetMapping("/book-appointment")
    public String bookAppointmentForm(Model model, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                model.addAttribute("patientName", patient.getFirstName());
            }
        }
        
        // Pass available doctors
        model.addAttribute("doctors", doctorRepository.findAll());
        return "appointment";
    }

    @org.springframework.web.bind.annotation.PostMapping("/book-appointment")
    public String submitAppointment(
            @org.springframework.web.bind.annotation.RequestParam("doctorId") Long doctorId,
            @org.springframework.web.bind.annotation.RequestParam("appointmentDate") String appointmentDateStr,
            @org.springframework.web.bind.annotation.RequestParam("timeSlot") String timeSlotStr,
            @org.springframework.web.bind.annotation.RequestParam("type") String type,
            @org.springframework.web.bind.annotation.RequestParam("reason") String reason,
            org.springframework.security.core.Authentication authentication) {
        
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/login";
        
        com.rajdhani.vqda.model.Patient patient = patientRepository.findByUser(user).orElse(null);
        if (patient == null) return "redirect:/login";
        
        com.rajdhani.vqda.model.Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) return "redirect:/patient/book-appointment?error=InvalidDoctor";

        // Create Appointment
        com.rajdhani.vqda.model.Appointment appointment = new com.rajdhani.vqda.model.Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(java.time.LocalDate.parse(appointmentDateStr));
        appointment.setTimeSlot(java.time.LocalTime.parse(timeSlotStr));
        appointment.setType(type);
        appointment.setReason(reason);
        appointment.setStatus("CONFIRMED"); // Auto-confirm for this flow
        
        appointment = appointmentRepository.save(appointment);

        // If it's for today, put them in queue
        if (appointment.getAppointmentDate().equals(java.time.LocalDate.now())) {
            com.rajdhani.vqda.model.Queue queue = new com.rajdhani.vqda.model.Queue();
            queue.setAppointment(appointment);
            queue.setStatus("WAITING");
            queue.setPriorityLevel(type);
            
            // Generate queue number
            java.util.List<com.rajdhani.vqda.model.Queue> docQueue = queueRepository.findByAppointmentDoctorOrderByQueueNumberAsc(doctor);
            int nextNum = 1;
            if (!docQueue.isEmpty()) {
                nextNum = docQueue.get(docQueue.size() - 1).getQueueNumber() + 1;
            }
            queue.setQueueNumber(nextNum);
            queue.setEstimatedWaitingTime(15 * nextNum);
            
            queueRepository.save(queue);
        }

        return "redirect:/patient/dashboard?success=booked";
    }

    @GetMapping("/appointments")
    public String appointments(Model model, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                model.addAttribute("patientName", patient.getFirstName());
                model.addAttribute("appointments", appointmentRepository.findByPatientOrderByAppointmentDateDesc(patient));
            }
        }
        return "appointments";
    }
}
