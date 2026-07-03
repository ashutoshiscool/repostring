package com.rajdhani.vqda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/queue")
public class QueueApiController {

    @Autowired
    private com.rajdhani.vqda.repository.QueueRepository queueRepository;

    @Autowired
    private com.rajdhani.vqda.repository.PatientRepository patientRepository;

    @Autowired
    private com.rajdhani.vqda.repository.UserRepository userRepository;

    @Autowired
    private com.rajdhani.vqda.repository.AppointmentRepository appointmentRepository;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getQueueStatus(org.springframework.security.core.Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null) {
            response.put("error", "Not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        String email = authentication.getName();
        com.rajdhani.vqda.model.User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            com.rajdhani.vqda.model.Patient patient = patientRepository.findByUser(user).orElse(null);
            if (patient != null) {
                java.util.List<com.rajdhani.vqda.model.Appointment> apps = appointmentRepository.findByPatientOrderByAppointmentDateDesc(patient);
                if (!apps.isEmpty()) {
                    com.rajdhani.vqda.model.Appointment latestApp = apps.get(0);
                    // Check if there is an active queue for this appointment
                    java.util.Optional<com.rajdhani.vqda.model.Queue> qOpt = queueRepository.findFirstByAppointmentDoctorAndStatusOrderByPriorityScoreDescQueueNumberAsc(latestApp.getDoctor(), "WAITING");
                    
                    // Actually, we want to know the *patient's* position in the queue
                    // First let's find the patient's queue entry
                    java.util.Optional<com.rajdhani.vqda.model.Queue> patientQueueOpt = queueRepository.findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(latestApp.getDoctor())
                        .stream().filter(q -> q.getAppointment().getPatient().getId().equals(patient.getId()) && !q.getStatus().equals("COMPLETED")).findFirst();
                    
                    if (patientQueueOpt.isPresent()) {
                        com.rajdhani.vqda.model.Queue patientQueue = patientQueueOpt.get();
                        
                        // How many are currently waiting before this patient?
                        long ahead = queueRepository.findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(latestApp.getDoctor())
                                .stream().filter(q -> q.getStatus().equals("WAITING") && q.getQueueNumber() < patientQueue.getQueueNumber()).count();
                        
                        response.put("status", patientQueue.getStatus());
                        response.put("queueNumber", patientQueue.getQueueNumber());
                        response.put("peopleAhead", ahead);
                        response.put("estimatedWaitMins", (ahead + 1) * 15);
                        return ResponseEntity.ok(response);
                    }
                }
            }
        }
        
        response.put("status", "NO_QUEUE");
        return ResponseEntity.ok(response);
    }
}
