package com.rajdhani.vqda.repository;

import com.rajdhani.vqda.model.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueRepository extends JpaRepository<Queue, Long> {
    List<Queue> findByStatus(String status);
    List<Queue> findAllByOrderByPriorityScoreDescQueueNumberAsc();
    long countByAppointmentDoctorAndStatus(com.rajdhani.vqda.model.Doctor doctor, String status);
    java.util.Optional<Queue> findFirstByAppointmentDoctorAndStatusOrderByPriorityScoreDescQueueNumberAsc(com.rajdhani.vqda.model.Doctor doctor, String status);
    List<Queue> findByAppointmentDoctorOrderByPriorityScoreDescQueueNumberAsc(com.rajdhani.vqda.model.Doctor doctor);
}
