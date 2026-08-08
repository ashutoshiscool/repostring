package com.rajdhani.vqda.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private GmailApiService gmailApiService;

    public void sendRegistrationOtp(String toEmail, String otpCode) {
        String subject = "VirtualQueue - Your Registration OTP Code";
        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                + "<h2 style='color: #2563eb;'>Welcome to VirtualQueue System!</h2>"
                + "<p>Thank you for registering. Please use the following 6-digit OTP code to verify your email address:</p>"
                + "<div style='background-color: #f3f4f6; padding: 15px; text-align: center; border-radius: 8px; font-size: 28px; font-weight: bold; letter-spacing: 5px; color: #1d4ed8;'>"
                + otpCode
                + "</div>"
                + "<p style='margin-top: 20px; font-size: 13px; color: #6b7280;'>This code will expire in 10 minutes. If you did not request this, please ignore this email.</p>"
                + "</div>";

        String plainText = "Welcome to VirtualQueue! Your Registration OTP Code is: " + otpCode;
        gmailApiService.sendEmail(toEmail, subject, htmlContent, plainText, null, null);
    }

    public void sendPasswordResetOtp(String toEmail, String otpCode) {
        String subject = "VirtualQueue - Password Reset OTP Code";
        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                + "<h2 style='color: #dc2626;'>Password Reset Request</h2>"
                + "<p>We received a request to reset your VirtualQueue account password. Use the OTP code below to set a new password:</p>"
                + "<div style='background-color: #fef2f2; padding: 15px; text-align: center; border-radius: 8px; font-size: 28px; font-weight: bold; letter-spacing: 5px; color: #b91c1c;'>"
                + otpCode
                + "</div>"
                + "<p style='margin-top: 20px; font-size: 13px; color: #6b7280;'>This code will expire in 10 minutes.</p>"
                + "</div>";

        String plainText = "VirtualQueue Password Reset OTP Code: " + otpCode;
        gmailApiService.sendEmail(toEmail, subject, htmlContent, plainText, null, null);
    }

    public void sendPrescriptionEmail(String toEmail, String patientName, String doctorName, String specialization, String appointmentDate, String timeSlot, String notes, String imagePath) {
        String subject = "VirtualQueue - Medical Prescription from " + doctorName;
        String imageUrl = (imagePath != null && !imagePath.isEmpty()) ? "http://localhost:8080" + imagePath : null;

        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333; max-width: 600px; border: 1px solid #e5e7eb; border-radius: 12px;'>"
                + "<div style='background-color: #2563eb; padding: 15px; text-align: center; border-radius: 8px 8px 0 0; color: white;'>"
                + "<h2 style='margin: 0;'>Medical Prescription</h2>"
                + "<p style='margin: 5px 0 0 0; font-size: 14px; opacity: 0.9;'>VirtualQueue Health Portal</p>"
                + "</div>"
                + "<div style='padding: 20px;'>"
                + "<p>Dear <strong>" + patientName + "</strong>,</p>"
                + "<p>Below are your prescription notes from your recent appointment:</p>"
                + "<table style='width: 100%; border-collapse: collapse; margin-bottom: 20px; text-align: left; font-size: 14px;'>"
                + "<tr><td style='padding: 8px; color: #6b7280;'>Doctor:</td><td style='padding: 8px; font-weight: bold;'>" + doctorName + " (" + specialization + ")</td></tr>"
                + "<tr><td style='padding: 8px; color: #6b7280;'>Date & Time:</td><td style='padding: 8px;'>" + appointmentDate + " at " + timeSlot + "</td></tr>"
                + "</table>"
                + "<div style='background-color: #f9fafb; padding: 15px; border-left: 4px solid #2563eb; border-radius: 4px; margin-bottom: 20px;'>"
                + "<h4 style='margin: 0 0 10px 0; color: #1e40af;'>Diagnosis & Prescribed Medicines:</h4>"
                + "<pre style='font-family: Arial, sans-serif; white-space: pre-wrap; margin: 0; font-size: 14px; color: #374151;'>" + (notes != null ? notes : "No typed notes attached.") + "</pre>"
                + "</div>"
                + (imageUrl != null ? "<p><strong>Prescription Image Attachment:</strong> <a href='" + imageUrl + "' target='_blank' style='color: #2563eb; font-weight: bold;'>Click to View Prescription Image</a></p>" : "")
                + "<p style='font-size: 12px; color: #9ca3af; margin-top: 30px; text-align: center;'>VirtualQueue Health Management System</p>"
                + "</div>"
                + "</div>";

        String plainText = "Medical Prescription from " + doctorName + "\nNotes: " + notes;
        gmailApiService.sendEmail(toEmail, subject, htmlContent, plainText, null, null);
    }

    public void sendTestEmail(String toEmail) {
        String subject = "VirtualQueue - Gmail Integration Test Email";
        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333; max-width: 500px; border: 1px solid #e5e7eb; border-radius: 12px;'>"
                + "<div style='background-color: #10b981; padding: 15px; text-align: center; border-radius: 8px 8px 0 0; color: white;'>"
                + "<h2 style='margin: 0;'>✓ Gmail API Connected</h2>"
                + "</div>"
                + "<div style='padding: 20px; text-align: center;'>"
                + "<p>This is a test email sent from the VirtualQueue Admin Portal using the official <strong>Google Gmail API (OAuth 2.0)</strong>.</p>"
                + "<p style='color: #059669; font-weight: bold;'>Your Gmail integration is operating perfectly!</p>"
                + "</div>"
                + "</div>";

        String plainText = "Gmail Integration Test Email from VirtualQueue Admin. Gmail API connected successfully!";
        gmailApiService.sendEmail(toEmail, subject, htmlContent, plainText, null, null);
    }
}
