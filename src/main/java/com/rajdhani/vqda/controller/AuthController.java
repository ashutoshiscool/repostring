package com.rajdhani.vqda.controller;

import com.rajdhani.vqda.dto.UserRegistrationDto;
import com.rajdhani.vqda.model.Patient;
import com.rajdhani.vqda.model.User;
import com.rajdhani.vqda.service.PatientService;
import com.rajdhani.vqda.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PatientService patientService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@Valid @ModelAttribute("userDto") UserRegistrationDto registrationDto,
                                      BindingResult result,
                                      Model model) {
        User existingUser = userService.findByEmail(registrationDto.getEmail());
        if (existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()) {
            result.rejectValue("email", null, "There is already an account registered with that email");
        }

        if (result.hasErrors()) {
            model.addAttribute("userDto", registrationDto);
            return "register";
        }

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(registrationDto.getPassword());
        userService.registerPatientUser(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(registrationDto.getFirstName());
        patient.setLastName(registrationDto.getLastName());
        patient.setPhone(registrationDto.getPhone());
        patientService.save(patient);

        return "redirect:/register?success";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
