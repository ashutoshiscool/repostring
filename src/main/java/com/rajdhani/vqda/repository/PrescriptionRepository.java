package com.rajdhani.vqda.repository;

import com.rajdhani.vqda.model.Appointment;
import com.rajdhani.vqda.model.Patient;
import com.rajdhani.vqda.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByAppointment(Appointment appointment);
    List<Prescription> findByPatientOrderByCreatedAtDesc(Patient patient);
}
