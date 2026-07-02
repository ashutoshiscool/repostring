package com.rajdhani.vqda.repository;

import com.rajdhani.vqda.model.Patient;
import com.rajdhani.vqda.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser(User user);
}
