package com.rajdhani.vqda.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGlobalException(Exception ex, Model model) {
        model.addAttribute("errorMsg", ex.getMessage());
        return "500"; // Will return 500.html template
    }

    // Spring Boot automatically handles 404 with error mapping if templates/error/404.html is present,
    // or we can implement ErrorController for finer control. We will use the templates/error folder approach.
}
