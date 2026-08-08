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

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(org.springframework.web.multipart.MaxUploadSizeExceededException ex, Model model) {
        model.addAttribute("errorMsg", "File upload size exceeded. Please select a valid prescription image file.");
        return "500";
    }
}
