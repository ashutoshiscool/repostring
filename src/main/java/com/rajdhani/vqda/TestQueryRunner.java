package com.rajdhani.vqda;

import com.rajdhani.vqda.model.User;
import com.rajdhani.vqda.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestQueryRunner implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("================== USERS IN DATABASE ==================");
        List<User> users = userRepository.findAll();
        for (User u : users) {
            System.out.println("User: " + u.getEmail() + " | Enabled: " + u.isEnabled() + " | Password: " + u.getPassword());
        }
        System.out.println("=======================================================");
    }
}
