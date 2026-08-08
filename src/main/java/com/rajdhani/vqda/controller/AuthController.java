package com.rajdhani.vqda.controller;

import com.rajdhani.vqda.dto.UserRegistrationDto;
import com.rajdhani.vqda.model.OtpToken;
import com.rajdhani.vqda.model.Patient;
import com.rajdhani.vqda.model.User;
import com.rajdhani.vqda.repository.OtpTokenRepository;
import com.rajdhani.vqda.repository.UserRepository;
import com.rajdhani.vqda.service.EmailService;
import com.rajdhani.vqda.service.PatientService;
import com.rajdhani.vqda.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        // Generate Registration OTP Code
        String otpCode = String.format("%06d", new java.util.Random().nextInt(900000) + 100000);
        OtpToken token = new OtpToken();
        token.setEmail(user.getEmail());
        token.setOtpCode(otpCode);
        token.setType("REGISTRATION");
        token.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        otpTokenRepository.save(token);

        // Send Email
        emailService.sendRegistrationOtp(user.getEmail(), otpCode);

        return "redirect:/verify-otp?email=" + user.getEmail();
    }

    @GetMapping("/verify-otp")
    public String verifyOtpForm(@RequestParam(value = "email", required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String submitVerifyOtp(@RequestParam("email") String email,
                                  @RequestParam("otpCode") String otpCode,
                                  Model model) {
        OtpToken token = otpTokenRepository.findFirstByEmailAndOtpCodeAndTypeAndVerifiedFalse(email, otpCode, "REGISTRATION")
                .orElse(null);

        if (token == null || token.isExpired()) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Invalid or expired OTP code. Please check your email and try again.");
            return "verify-otp";
        }

        token.setVerified(true);
        otpTokenRepository.save(token);

        User user = userService.findByEmail(email);
        if (user != null) {
            user.setEnabled(true);
            userRepository.save(user);
        }

        return "redirect:/login?verified=true";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String submitForgotPassword(@RequestParam("email") String email, Model model) {
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "No account registered with that email address.");
            return "forgot-password";
        }

        // Generate Password Reset OTP
        String otpCode = String.format("%06d", new java.util.Random().nextInt(900000) + 100000);
        OtpToken token = new OtpToken();
        token.setEmail(email);
        token.setOtpCode(otpCode);
        token.setType("PASSWORD_RESET");
        token.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        otpTokenRepository.save(token);

        // Send Email
        emailService.sendPasswordResetOtp(email, otpCode);

        return "redirect:/reset-password?email=" + email;
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(value = "email", required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String submitResetPassword(@RequestParam("email") String email,
                                      @RequestParam("otpCode") String otpCode,
                                      @RequestParam("newPassword") String newPassword,
                                      Model model) {
        OtpToken token = otpTokenRepository.findFirstByEmailAndOtpCodeAndTypeAndVerifiedFalse(email, otpCode, "PASSWORD_RESET")
                .orElse(null);

        if (token == null || token.isExpired()) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Invalid or expired OTP code. Please try again.");
            return "reset-password";
        }

        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "User account not found.");
            return "reset-password";
        }

        token.setVerified(true);
        otpTokenRepository.save(token);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "redirect:/login?resetSuccess=true";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
