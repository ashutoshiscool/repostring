package com.rajdhani.vqda.repository;

import com.rajdhani.vqda.model.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueRepository extends JpaRepository<Queue, Long> {
    List<Queue> findByStatus(String status);
    List<Queue> findAllByOrderByQueueNumberAsc();
    long countByAppointmentDoctorAndStatus(com.rajdhani.vqda.model.Doctor doctor, String status);
    java.util.Optional<Queue> findFirstByAppointmentDoctorAndStatusOrderByQueueNumberAsc(com.rajdhani.vqda.model.Doctor doctor, String status);
    List<Queue> findByAppointmentDoctorOrderByQueueNumberAsc(com.rajdhani.vqda.model.Doctor doctor);
}
