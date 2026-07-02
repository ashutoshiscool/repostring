package com.rajdhani.vqda.controller;

import com.rajdhani.vqda.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (role.equals("ROLE_DOCTOR")) {
                return "redirect:/doctor/dashboard";
            } else if (role.equals("ROLE_PATIENT")) {
                return "redirect:/patient/dashboard";
            }
        }
        return "redirect:/login";
    }
}
