package com.rajdhani.vqda.repository;

import com.rajdhani.vqda.model.Doctor;
import com.rajdhani.vqda.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser(User user);
}
