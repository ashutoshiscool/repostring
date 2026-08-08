# Hospital Virtual Appointment Booking & Queue Management System

**Degree:** Bachelor of Computer System and Information Technology (BCSIT)  
**University:** Pokhara University  
**College:** Rajdhani Model College  
**Student Name:** Aayushma Joshi  
**Roll No.:** 24080602  
**Academic Year:** 2026/20  

---

## 📌 Project Overview

The **Hospital Virtual Appointment Booking & Queue Management System** is a web application developed using Java 21, Spring Boot 3, MySQL, and Thymeleaf with Tailwind CSS. It is designed to digitalize outpatient department (OPD) operations, enabling patients to register online, verify their email via 6-digit OTP, book appointments with specialists, and track their live queue position remotely.

Medical staff and administrators can manage doctors, patient records, priority queuing (Emergency vs Normal), prescription scan uploads, and automated email notifications through Google Gmail API (OAuth 2.0).

---

## ✨ Features

- **Patient Portal:**
  - Account registration with 6-digit email OTP verification.
  - Browse doctors by specialization (Cardiologist, Neurologist, Orthopedic, etc.).
  - Book normal or emergency appointments.
  - Live queue tracker with real-time waiting time estimation.
  - View and print medical prescriptions attached by doctors.

- **Doctor Portal:**
  - View daily consultation schedule and live patient queue.
  - Next patient calling mechanism (`Call Next`).
  - Attach medical notes and upload prescription scan images.

- **Admin Dashboard:**
  - System overview metrics (Doctors, Patients, Appointments, Active Queues).
  - Add/delete doctors and patient accounts.
  - Manage global schedules and priority queue status.
  - Google OAuth 2.0 Client Credentials configuration & Gmail API integration panel.

---

## 🛠️ Technology Stack

- **Backend:** Java 21, Spring Boot 3.2.5, Spring Security 6 (RBAC), Spring Data JPA
- **Database:** MySQL 8.0
- **Frontend:** Thymeleaf, HTML5, Tailwind CSS, JavaScript
- **Email & Security:** Google Gmail API (OAuth 2.0), AES-256 Refresh Token Encryption, BCrypt Password Hashing
- **Build System:** Apache Maven

---

## 🚀 Quick Start Instructions

### Windows 11 (Automated)
Right-click `string.ps1` and select **Run with PowerShell** (or run `start.ps1`).  
The script automatically installs required dependencies via `winget`, configures MySQL, starts the Spring Boot application, and opens `http://localhost:8080` in your browser.

```powershell
.\string.ps1
```

### Linux / Mac
Run the startup bash script:

```bash
chmod +x start.sh
./start.sh
```

---

## 🔐 Default Test Credentials

- **Admin:** `admin@hospital.com` / `admin123`
- **Doctor:** `sanjay.thapa@rajdhanihealthline.com` / `doctor123`
- **Patient:** `aarav.sharma1@gmail.com` / `patient123`

---

## 📄 Project Documentation

- Full Project Report: `PROJECT_REPORT.md` (and compiled `project_report.pdf`)
- Google Cloud OAuth Setup Guide: `GOOGLE_CLOUD_SETUP.md`
