package com.rajdhani.vqda.service;

import com.rajdhani.vqda.model.Patient;
import com.rajdhani.vqda.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientService {
    
    @Autowired
    private PatientRepository patientRepository;

    public void save(Patient patient) {
        patientRepository.save(patient);
    }
}
