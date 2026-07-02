package com.rajdhani.vqda.service;

import com.rajdhani.vqda.model.Doctor;
import com.rajdhani.vqda.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
    
    public Doctor findById(Long id) {
        return doctorRepository.findById(id).orElse(null);
    }
}
