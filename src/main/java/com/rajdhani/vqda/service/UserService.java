package com.rajdhani.vqda.service;

import com.rajdhani.vqda.model.Role;
import com.rajdhani.vqda.model.User;
import com.rajdhani.vqda.repository.RoleRepository;
import com.rajdhani.vqda.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerPatientUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role patientRole = roleRepository.findByName("PATIENT")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.getRoles().add(patientRole);
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
