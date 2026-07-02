package com.rajdhani.vqda.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(nullable = false)
    private String specialization;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(name = "availability_status")
    private boolean availabilityStatus = true;
    
    @Transient
    public String getFullName() {
        return "Dr. " + firstName + " " + lastName;
    }
}
