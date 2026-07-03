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

    @org.springframework.web.bind.annotation.PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(@org.springframework.web.bind.annotation.PathVariable Long id) {
        doctorRepository.deleteById(id);
        return "redirect:/admin/doctors?success=deleted";
    }

    @org.springframework.web.bind.annotation.PostMapping("/patients/delete/{id}")
    public String deletePatient(@org.springframework.web.bind.annotation.PathVariable Long id) {
        patientRepository.deleteById(id);
        return "redirect:/admin/patients?success=deleted";
    }

    @GetMapping("/appointments")
    public String manageAppointments(Model model) {
        model.addAttribute("appointments", appointmentRepository.findAll());
        return "manage-appointments";
    }

    @GetMapping("/appointments/{id}/reschedule")
    public String rescheduleAppointmentForm(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        com.rajdhani.vqda.model.Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment != null && !"CANCELLED".equals(appointment.getStatus()) && !"COMPLETED".equals(appointment.getStatus())) {
            model.addAttribute("appointment", appointment);
            return "admin-reschedule";
        }
        return "redirect:/admin/appointments?error=InvalidAppointment";
    }

    @org.springframework.web.bind.annotation.PostMapping("/appointments/{id}/reschedule")
    public String submitAdminReschedule(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam("appointmentDate") String appointmentDateStr,
            @org.springframework.web.bind.annotation.RequestParam("timeSlot") String timeSlotStr) {
        
        com.rajdhani.vqda.model.Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment != null) {
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
                    existingQueue.setStatus("CANCELLED");
                    queueRepository.save(existingQueue);
                }
            } else if (isToday && "CONFIRMED".equals(appointment.getStatus())) {
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
            
            return "redirect:/admin/appointments?success=Rescheduled";
        }
        
        return "redirect:/admin/appointments?error=Failed";
    }

    @org.springframework.web.bind.annotation.PostMapping("/appointments/delete/{id}")
    public String deleteAppointment(@org.springframework.web.bind.annotation.PathVariable Long id) {
        appointmentRepository.deleteById(id);
        return "redirect:/admin/appointments?success=deleted";
    }

    @GetMapping("/queues")
    public String viewQueues(Model model) {
        model.addAttribute("queues", queueRepository.findAllByOrderByPriorityScoreDescQueueNumberAsc());
        return "admin-queues";
    }
}
