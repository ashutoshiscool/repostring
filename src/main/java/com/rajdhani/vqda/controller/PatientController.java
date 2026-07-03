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
                    com.rajdhani.vqda.model.Queue q = queueRepository.findFirstByAppointmentDoctorAndStatusOrderByPriorityScoreDescQueueNumberAsc(apps.get(0).getDoctor(), "WAITING").orElse(null);
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
            queue.setPriorityScore("Emergency".equalsIgnoreCase(type) ? 1 : 0);
            
            // Generate queue number
            java.util.List<com.rajdhani.vqda.model.Queue> docQueue = queueRepository.findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(doctor);
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
    @org.springframework.web.bind.annotation.PostMapping("/appointment/{id}/cancel")
    public String cancelAppointment(@org.springframework.web.bind.annotation.PathVariable Long id, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                com.rajdhani.vqda.model.Appointment appointment = appointmentRepository.findById(id).orElse(null);
                if (appointment != null && appointment.getPatient().getId().equals(patient.getId())) {
                    appointment.setStatus("CANCELLED");
                    appointmentRepository.save(appointment);
                    // Also cancel queue if it exists
                    java.util.Optional<com.rajdhani.vqda.model.Queue> qOpt = queueRepository.findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(appointment.getDoctor())
                        .stream().filter(q -> q.getAppointment().getId().equals(id)).findFirst();
                    if (qOpt.isPresent()) {
                        com.rajdhani.vqda.model.Queue q = qOpt.get();
                        q.setStatus("CANCELLED");
                        queueRepository.save(q);
                    }
                }
            }
        }
        return "redirect:/patient/appointments?success=cancelled";
    }

    @GetMapping("/appointment/{id}/reschedule")
    public String rescheduleForm(@org.springframework.web.bind.annotation.PathVariable Long id, Model model, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                com.rajdhani.vqda.model.Appointment appointment = appointmentRepository.findById(id).orElse(null);
                if (appointment != null && appointment.getPatient().getId().equals(patient.getId()) && "CONFIRMED".equals(appointment.getStatus())) {
                    model.addAttribute("patientName", patient.getFirstName());
                    model.addAttribute("appointment", appointment);
                    return "patient-reschedule";
                }
            }
        }
        return "redirect:/patient/appointments?error=InvalidAppointment";
    }

    @org.springframework.web.bind.annotation.PostMapping("/appointment/{id}/reschedule")
    public String submitReschedule(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam("appointmentDate") String appointmentDateStr,
            @org.springframework.web.bind.annotation.RequestParam("timeSlot") String timeSlotStr,
            org.springframework.security.core.Authentication authentication) {
        
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                com.rajdhani.vqda.model.Appointment appointment = appointmentRepository.findById(id).orElse(null);
                if (appointment != null && appointment.getPatient().getId().equals(patient.getId())) {
                    java.time.LocalDate newDate = java.time.LocalDate.parse(appointmentDateStr);
                    java.time.LocalTime newTime = java.time.LocalTime.parse(timeSlotStr);
                    
                    appointment.setAppointmentDate(newDate);
                    appointment.setTimeSlot(newTime);
                    appointmentRepository.save(appointment);
                    
                    // Handle queue logic
                    java.util.Optional<com.rajdhani.vqda.model.Queue> existingQueueOpt = queueRepository.findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(appointment.getDoctor())
                        .stream().filter(q -> q.getAppointment().getId().equals(id) && !"COMPLETED".equals(q.getStatus()) && !"CANCELLED".equals(q.getStatus())).findFirst();
                    
                    boolean isToday = newDate.equals(java.time.LocalDate.now());
                    
                    if (existingQueueOpt.isPresent()) {
                        com.rajdhani.vqda.model.Queue existingQueue = existingQueueOpt.get();
                        if (!isToday) {
                            // Moved to future, cancel queue
                            existingQueue.setStatus("CANCELLED");
                            queueRepository.save(existingQueue);
                        }
                    } else if (isToday) {
                        // Moved to today, create queue
                        com.rajdhani.vqda.model.Queue queue = new com.rajdhani.vqda.model.Queue();
                        queue.setAppointment(appointment);
                        queue.setStatus("WAITING");
                        queue.setPriorityLevel(appointment.getType());
                        queue.setPriorityScore("Emergency".equalsIgnoreCase(appointment.getType()) ? 1 : 0);
                        
                        java.util.List<com.rajdhani.vqda.model.Queue> docQueue = queueRepository.findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(appointment.getDoctor());
                        int nextNum = docQueue.isEmpty() ? 1 : docQueue.get(docQueue.size() - 1).getQueueNumber() + 1;
                        queue.setQueueNumber(nextNum);
                        queue.setEstimatedWaitingTime(15 * nextNum);
                        queueRepository.save(queue);
                    }
                    
                    return "redirect:/patient/appointments?success=Rescheduled";
                }
            }
        }
        return "redirect:/patient/appointments?error=Failed";
    }

    @GetMapping("/profile")
    public String profile(Model model, org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                model.addAttribute("patient", patient);
                return "patient-profile";
            }
        }
        return "redirect:/login";
    }

    @org.springframework.web.bind.annotation.PostMapping("/profile/update")
    public String updateProfile(
            @org.springframework.web.bind.annotation.RequestParam("phone") String phone,
            @org.springframework.web.bind.annotation.RequestParam("dob") String dobStr,
            @org.springframework.web.bind.annotation.RequestParam("bloodGroup") String bloodGroup,
            org.springframework.security.core.Authentication authentication) {
        
        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                patient.setPhone(phone);
                if (dobStr != null && !dobStr.isEmpty()) {
                    patient.setDob(java.time.LocalDate.parse(dobStr));
                }
                patient.setBloodGroup(bloodGroup);
                patientRepository.save(patient);
            }
        }
        return "redirect:/patient/profile?success=updated";
    }
}
