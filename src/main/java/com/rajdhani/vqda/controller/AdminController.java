package com.rajdhani.vqda.controller;

import com.rajdhani.vqda.model.*;
import com.rajdhani.vqda.repository.*;
import com.rajdhani.vqda.service.EmailService;
import com.rajdhani.vqda.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalDoctors", doctorRepository.count());
        model.addAttribute("totalPatients", patientRepository.count());
        model.addAttribute("appointmentsToday", appointmentRepository.count());
        model.addAttribute("inQueue", queueRepository.count());
        model.addAttribute("recentAppointments", appointmentRepository.findAll());
        return "admin-dashboard";
    }

    @GetMapping("/doctors")
    public String manageDoctors(Model model) {
        model.addAttribute("doctors", doctorRepository.findAll());
        return "manage-doctors";
    }

    @PostMapping("/doctors/add")
    public String addDoctor(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("specialization") String specialization,
            @RequestParam("phone") String phone,
            @RequestParam(value = "experienceYears", defaultValue = "5") Integer experienceYears) {

        if (userRepository.existsByEmail(email)) {
            return "redirect:/admin/doctors?error=EmailExists";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);

        Role docRole = roleRepository.findByName("ROLE_DOCTOR").orElse(null);
        if (docRole != null) {
            user.getRoles().add(docRole);
        }
        userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        doctor.setSpecialization(specialization);
        doctor.setPhone(phone);
        doctor.setExperienceYears(experienceYears);
        doctor.setAvailabilityStatus(true);
        doctorRepository.save(doctor);

        return "redirect:/admin/doctors?success=DoctorAdded";
    }

    @PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorRepository.deleteById(id);
        return "redirect:/admin/doctors?success=deleted";
    }

    @GetMapping("/patients")
    public String managePatients(Model model) {
        model.addAttribute("patients", patientRepository.findAll());
        return "manage-patients";
    }

    @PostMapping("/patients/add")
    public String addPatient(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            @RequestParam(value = "gender", defaultValue = "Other") String gender,
            @RequestParam(value = "bloodGroup", defaultValue = "O+") String bloodGroup) {

        if (userRepository.existsByEmail(email)) {
            return "redirect:/admin/patients?error=EmailExists";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);

        Role patientRole = roleRepository.findByName("ROLE_PATIENT").orElse(null);
        if (patientRole != null) {
            user.getRoles().add(patientRole);
        }
        userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setPhone(phone);
        patient.setGender(gender);
        patient.setBloodGroup(bloodGroup);
        patientRepository.save(patient);

        return "redirect:/admin/patients?success=PatientAdded";
    }

    @PostMapping("/patients/delete/{id}")
    public String deletePatient(@PathVariable Long id) {
        patientRepository.deleteById(id);
        return "redirect:/admin/patients?success=deleted";
    }

    @GetMapping("/appointments")
    public String manageAppointments(Model model) {
        model.addAttribute("appointments", appointmentRepository.findAll());
        model.addAttribute("doctors", doctorRepository.findAll());
        model.addAttribute("patients", patientRepository.findAll());
        return "manage-appointments";
    }

    @PostMapping("/appointments/add")
    public String addAppointment(
            @RequestParam("patientId") Long patientId,
            @RequestParam("doctorId") Long doctorId,
            @RequestParam("appointmentDate") String appointmentDateStr,
            @RequestParam("timeSlot") String timeSlotStr,
            @RequestParam(value = "type", defaultValue = "NORMAL") String type,
            @RequestParam(value = "reason", defaultValue = "General Checkup") String reason) {

        Patient patient = patientRepository.findById(patientId).orElse(null);
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);

        if (patient != null && doctor != null) {
            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentDate(LocalDate.parse(appointmentDateStr));
            appointment.setTimeSlot(LocalTime.parse(timeSlotStr));
            appointment.setType(type);
            appointment.setReason(reason);
            appointment.setStatus("CONFIRMED");
            appointmentRepository.save(appointment);

            // Queue logic
            if (LocalDate.parse(appointmentDateStr).equals(LocalDate.now())) {
                com.rajdhani.vqda.model.Queue queue = new com.rajdhani.vqda.model.Queue();
                queue.setAppointment(appointment);
                queue.setStatus("WAITING");
                queue.setPriorityLevel(type);
                queue.setPriorityScore("EMERGENCY".equalsIgnoreCase(type) ? 1 : 0);

                List<com.rajdhani.vqda.model.Queue> docQueue = queueRepository.findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(doctor);
                int nextNum = docQueue.isEmpty() ? 1 : docQueue.get(docQueue.size() - 1).getQueueNumber() + 1;
                queue.setQueueNumber(nextNum);
                queue.setEstimatedWaitingTime(15 * nextNum);
                queueRepository.save(queue);
            }

            return "redirect:/admin/appointments?success=Booked";
        }

        return "redirect:/admin/appointments?error=Failed";
    }

    @GetMapping("/appointments/{id}/reschedule")
    public String rescheduleAppointmentForm(@PathVariable Long id, Model model) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment != null && !"CANCELLED".equals(appointment.getStatus()) && !"COMPLETED".equals(appointment.getStatus())) {
            model.addAttribute("appointment", appointment);
            return "admin-reschedule";
        }
        return "redirect:/admin/appointments?error=InvalidAppointment";
    }

    @PostMapping("/appointments/{id}/reschedule")
    public String submitAdminReschedule(
            @PathVariable Long id,
            @RequestParam("appointmentDate") String appointmentDateStr,
            @RequestParam("timeSlot") String timeSlotStr) {
        
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment != null) {
            LocalDate newDate = LocalDate.parse(appointmentDateStr);
            LocalTime newTime = LocalTime.parse(timeSlotStr);
            
            appointment.setAppointmentDate(newDate);
            appointment.setTimeSlot(newTime);
            appointmentRepository.save(appointment);
            
            Optional<com.rajdhani.vqda.model.Queue> existingQueueOpt = queueRepository.findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(appointment.getDoctor())
                .stream().filter(q -> q.getAppointment().getId().equals(id) && !"COMPLETED".equals(q.getStatus()) && !"CANCELLED".equals(q.getStatus())).findFirst();
            
            boolean isToday = newDate.equals(LocalDate.now());
            
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
                
                List<com.rajdhani.vqda.model.Queue> docQueue = queueRepository.findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(appointment.getDoctor());
                int nextNum = docQueue.isEmpty() ? 1 : docQueue.get(docQueue.size() - 1).getQueueNumber() + 1;
                queue.setQueueNumber(nextNum);
                queue.setEstimatedWaitingTime(15 * nextNum);
                queueRepository.save(queue);
            }
            
            return "redirect:/admin/appointments?success=Rescheduled";
        }
        
        return "redirect:/admin/appointments?error=Failed";
    }

    @PostMapping("/appointments/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        appointmentRepository.deleteById(id);
        return "redirect:/admin/appointments?success=deleted";
    }

    @GetMapping("/queues")
    public String viewQueues(Model model) {
        model.addAttribute("queues", queueRepository.findAllByOrderByPriorityScoreDescQueueNumberAsc());
        return "admin-queues";
    }

    @GetMapping("/appointment/{appointmentId}/prescription")
    public String adminPrescriptionForm(@PathVariable Long appointmentId, Model model) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment != null) {
            model.addAttribute("appointment", appointment);
            Prescription prescription = prescriptionRepository.findByAppointment(appointment).orElse(new Prescription());
            model.addAttribute("prescription", prescription);
            return "admin-prescription";
        }
        return "redirect:/admin/appointments?error=NotFound";
    }

    @PostMapping("/appointment/{appointmentId}/prescription")
    public String submitAdminPrescription(
            @PathVariable Long appointmentId,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment != null) {
            Prescription prescription = prescriptionRepository.findByAppointment(appointment).orElse(new Prescription());
            prescription.setAppointment(appointment);
            prescription.setDoctor(appointment.getDoctor());
            prescription.setPatient(appointment.getPatient());
            prescription.setNotes(notes);

            if (image != null && !image.isEmpty()) {
                try {
                    String uploadDir = "./uploads/prescriptions/";
                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();

                    String originalFilename = image.getOriginalFilename();
                    String ext = "";
                    if (originalFilename != null && originalFilename.contains(".")) {
                        ext = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }
                    String filename = UUID.randomUUID().toString() + ext;
                    Path filePath = Paths.get(uploadDir, filename);
                    Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    prescription.setImagePath("/uploads/prescriptions/" + filename);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            prescriptionRepository.save(prescription);

            try {
                Patient p = appointment.getPatient();
                if (p != null && p.getUser() != null && p.getUser().getEmail() != null) {
                    emailService.sendPrescriptionEmail(
                            p.getUser().getEmail(),
                            p.getFullName(),
                            appointment.getDoctor().getFullName(),
                            appointment.getDoctor().getSpecialization(),
                            appointment.getAppointmentDate().toString(),
                            appointment.getTimeSlot().toString(),
                            prescription.getNotes(),
                            prescription.getImagePath()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "redirect:/admin/appointments?success=PrescriptionSaved";
        }

        return "redirect:/admin/appointments?error=Failed";
    }

    @Autowired
    private GmailIntegrationRepository gmailIntegrationRepository;

    @GetMapping("/smtp")
    public String smtpConfigForm(Model model) {
        GmailIntegration integration = gmailIntegrationRepository.findAll().stream()
                .filter(GmailIntegration::isConnected)
                .findFirst().orElse(null);
        
        model.addAttribute("gmailIntegration", integration);
        return "admin-smtp";
    }
}
