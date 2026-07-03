package com.rajdhani.vqda.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Queue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;
    
    @Column(name = "queue_number", nullable = false)
    private Integer queueNumber;
    
    @Column(nullable = false)
    private String status; // WAITING, IN_PROGRESS, COMPLETED, CANCELLED
    
    @Column(name = "estimated_waiting_time")
    private Integer estimatedWaitingTime; // in minutes
    
    @Column(name = "priority_level")
    private String priorityLevel; // NORMAL, EMERGENCY
    
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;
    
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;
    
    @Column(name = "priority_score")
    private Integer priorityScore = 0; // Higher is higher priority (e.g. 1 for Emergency, 0 for Normal)
}
