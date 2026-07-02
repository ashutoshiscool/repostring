package com.rajdhani.vqda.repository;

import com.rajdhani.vqda.model.Appointment;
import com.rajdhani.vqda.model.Doctor;
import com.rajdhani.vqda.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientOrderByAppointmentDateDesc(Patient patient);
    List<Appointment> findByDoctorOrderByAppointmentDateDesc(Doctor doctor);
    long countByDoctor(Doctor doctor);
}
