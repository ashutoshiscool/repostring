package com.rajdhani.vqda.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {
    
    @NotEmpty(message = "First name is required")
    private String firstName;
    
    @NotEmpty(message = "Last name is required")
    private String lastName;
    
    @NotEmpty(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotEmpty(message = "Password is required")
    private String password;
    
    @NotEmpty(message = "Phone number is required")
    private String phone;
}
