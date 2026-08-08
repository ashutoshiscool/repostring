import weasyprint
import os

html_content = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hospital Virtual Appointment Booking Queue System - Project Report</title>
    <style>
        @page {
            size: A4;
            margin: 20mm;
            @bottom-right {
                content: "Page " counter(page);
                font-family: Arial, sans-serif;
                font-size: 10pt;
                color: #666;
            }
        }
        body {
            font-family: Arial, sans-serif;
            color: #333;
            line-height: 1.6;
            font-size: 11pt;
        }
        .page-break {
            page-break-after: always;
        }
        .text-center {
            text-align: center;
        }
        h1 {
            font-size: 22pt;
            color: #1e3a8a;
            margin-top: 30px;
            margin-bottom: 20px;
            border-bottom: 2px solid #1e3a8a;
            padding-bottom: 5px;
        }
        h2 {
            font-size: 16pt;
            color: #1e40af;
            border-bottom: 1.5px solid #3b82f6;
            padding-bottom: 4px;
            margin-top: 25px;
        }
        h3 {
            font-size: 13pt;
            color: #1d4ed8;
            margin-top: 20px;
        }
        .title-page {
            text-align: center;
            padding-top: 40px;
        }
        .title-header {
            font-size: 28pt;
            font-weight: bold;
            color: #1e3a8a;
            letter-spacing: 1px;
        }
        .subtitle {
            font-size: 20pt;
            color: #2563eb;
            margin-top: 15px;
            font-weight: 600;
        }
        .meta-info {
            margin-top: 60px;
            font-size: 12pt;
            line-height: 2;
        }
        .meta-box {
            background-color: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 20px;
            margin-top: 40px;
            text-align: left;
            display: inline-block;
            width: 80%;
        }
        .cert-box {
            border: 2px solid #1e3a8a;
            padding: 30px;
            border-radius: 12px;
            background-color: #f8fafc;
            margin-top: 20px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
            margin-bottom: 20px;
            font-size: 10pt;
        }
        th, td {
            border: 1px solid #cbd5e1;
            padding: 8px 12px;
            text-align: left;
        }
        th {
            background-color: #2563eb;
            color: white;
            font-weight: bold;
        }
        tr:nth-child(even) {
            background-color: #f1f5f9;
        }
        .diagram-container {
            background-color: #f8fafc;
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            padding: 15px;
            margin: 20px 0;
            text-align: center;
        }
        .diagram-title {
            font-weight: bold;
            color: #1e40af;
            margin-bottom: 10px;
            font-size: 11pt;
        }
        code, pre {
            font-family: "Courier New", Courier, monospace;
            background-color: #f1f5f9;
            padding: 2px 5px;
            border-radius: 4px;
            font-size: 9.5pt;
        }
        pre {
            padding: 12px;
            display: block;
            white-space: pre-wrap;
            border-left: 4px solid #2563eb;
        }
        .badge {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 8.5pt;
            font-weight: bold;
            color: white;
        }
        .badge-pass { background-color: #10b981; }
    </style>
</head>
<body>

    <!-- COVER PAGE -->
    <div class="title-page page-break">
        <div class="title-header">PROJECT REPORT</div>
        <div class="subtitle">Hospital Virtual Appointment Booking Queue System</div>
        
        <div class="meta-info">
            <div class="meta-box">
                <p><strong>Submitted To:</strong> Department of Computer Science and Information Technology, Pokhara University</p>
                <p><strong>Submitted By:</strong> Student Name: Aayushma Joshi | Roll No.: 24080602</p>
                <p><strong>College:</strong> Rajdhani Model College</p>
                <p><strong>Degree:</strong> Bachelor of Computer System and Information Technology (BCSIT)</p>
                <p><strong>Academic Year:</strong> 2026/20</p>
            </div>
        </div>
    </div>

    <!-- CERTIFICATE -->
    <div class="page-break">
        <h1>Certificate</h1>
        <div class="cert-box">
            <p>This is to certify that the project entitled <strong>"Hospital Virtual Appointment Booking Queue System"</strong> submitted by <strong>Aayushma Joshi</strong> in partial fulfillment of the requirements for the degree of <strong>Bachelor of Computer System and Information Technology (BCSIT)</strong>, Pokhara University, is an original work carried out under my supervision.</p>
            <br><br>
            <p><strong>Supervisor's Signature:</strong> ___________________________</p>
            <br>
            <p><strong>Date:</strong> ___________________________</p>
        </div>

        <h1>Acknowledgement</h1>
        <p>I would like to express my sincere gratitude to my project supervisor for the continuous guidance, valuable suggestions, and unwavering encouragement provided throughout the development of the Hospital Virtual Appointment Booking Queue System. The expertise and constructive feedback I received played a significant role in the successful completion of this project.</p>
        <p>I am also deeply thankful to the faculty members of the Department of Computer Science and Information Technology at Rajdhani Model College for imparting the foundational knowledge and technical skills necessary to undertake a software development project of this scale.</p>
        <p>Furthermore, I extend my appreciation to my friends and family for their endless moral support and motivation during the challenging phases of system design and implementation. This project would not have been possible without their collective support.</p>
    </div>

    <!-- ABSTRACT -->
    <div class="page-break">
        <h1>Abstract</h1>
        <p>The <strong>Hospital Virtual Appointment Booking Queue System</strong> is a comprehensive, web-based application engineered to digitalize and streamline the appointment scheduling and queue management processes within healthcare facilities. Traditionally, hospitals rely on manual or disjointed digital systems to manage patient inflow, which frequently results in overcrowded waiting rooms, unpredictable waiting times, and administrative bottlenecks. This project addresses these critical inefficiencies by introducing an automated platform that manages the entire lifecycle of a patient's outpatient visit.</p>
        <p>Developed utilizing a robust technology stack—including <strong>Java 21, Spring Boot 3.2.5</strong> for backend logic, <strong>Spring Security 6</strong> for role-based access control, <strong>Spring Data JPA with MySQL</strong> for persistent data storage, <strong>Google Gmail API (OAuth 2.0)</strong> for automated email communications, and <strong>Thymeleaf integrated with Tailwind CSS</strong> for a responsive frontend—the system delivers a seamless experience across three distinct user roles: Patient, Doctor, and Administrator.</p>
        <p>By implementing algorithmic queue generation and priority scoring (distinguishing between normal and emergency appointments), the system significantly optimizes patient throughput. The integration of modern UI components ensures accessibility across mobile and desktop devices. Ultimately, the Hospital Virtual Appointment Booking Queue System minimizes physical congestion in healthcare facilities, enhances the accuracy of medical administrative records, and substantially improves the overall healthcare experience for both providers and patients.</p>
    </div>

    <!-- TABLE OF CONTENTS -->
    <div class="page-break">
        <h1>Table of Contents</h1>
        <ol style="line-height: 1.8;">
            <li><strong>Chapter 1: Introduction</strong></li>
            <li><strong>Chapter 2: Problem Statement</strong></li>
            <li><strong>Chapter 3: Objectives</strong></li>
            <li><strong>Chapter 4: Scope of the Project</strong></li>
            <li><strong>Chapter 5: Literature Review</strong></li>
            <li><strong>Chapter 6: System Analysis</strong></li>
            <li><strong>Chapter 7: Feasibility Study</strong></li>
            <li><strong>Chapter 8: Requirement Analysis</strong></li>
            <li><strong>Chapter 9: System Design & Diagrams</strong></li>
            <li><strong>Chapter 10: Database Design & ER Diagram</strong></li>
            <li><strong>Chapter 11: Implementation Details</strong></li>
            <li><strong>Chapter 12: Testing</strong></li>
            <li><strong>Chapter 13: Results & Discussion</strong></li>
            <li><strong>Chapter 14: Conclusion</strong></li>
            <li><strong>Chapter 15: Future Enhancements</strong></li>
            <li><strong>Chapter 16: References</strong></li>
        </ol>
    </div>

    <!-- CHAPTER 1 & 2 -->
    <div class="page-break">
        <h1>Chapter 1: Introduction</h1>
        <p>The rapid advancement of information technology has fundamentally transformed various sectors, with healthcare being one of the primary beneficiaries. Despite these technological strides, many healthcare institutions continue to struggle with outpatient department (OPD) management. The traditional process of visiting a hospital, waiting in long physical queues to book an appointment, and subsequently waiting outside a doctor's cabin for an indeterminate amount of time is highly inefficient. It induces immense stress on patients and imposes a heavy administrative burden on hospital staff.</p>
        <p>The Hospital Virtual Appointment Booking Queue System is a web-based software solution designed to eliminate the friction associated with medical appointments. By shifting the queueing paradigm from a physical space to a virtual platform, the system allows patients to secure their position in a doctor's schedule remotely.</p>

        <h1>Chapter 2: Problem Statement</h1>
        <p>The administration of patient flow in modern healthcare facilities is fraught with logistical challenges. Core issues prevalent in traditional operational models include:</p>
        <ul>
            <li><strong>Unpredictable Waiting Times and Overcrowding:</strong> Patients arrive hours before consultations without visibility.</li>
            <li><strong>Inefficient Handling of Emergencies:</strong> Walk-in emergency cases disrupt daily schedules without automated priority sorting.</li>
            <li><strong>High Rate of Administrative Errors:</strong> Manual transcription leads to double-bookings and lost appointment records.</li>
            <li><strong>Lack of Real-Time Communication:</strong> Patients are not notified when consultations run over time.</li>
            <li><strong>Absence of Centralized Analytics:</strong> Hospital administrators lack real-time visibility into daily operations.</li>
        </ul>
    </div>

    <!-- CHAPTER 3 & 4 -->
    <div class="page-break">
        <h1>Chapter 3: Objectives</h1>
        <h3>General Objective</h3>
        <p>To design, develop, and deploy a computerized Hospital Virtual Appointment Booking Queue System that comprehensively manages the outpatient workflow, seamlessly connecting patients, doctors, and hospital administrators through a centralized virtual platform.</p>

        <h3>Specific Objectives</h3>
        <ul>
            <li>Digitalize Patient Onboarding with 6-digit email OTP verification.</li>
            <li>Streamline Appointment Scheduling with doctor filtering by specialization.</li>
            <li>Automate Queue Management with priority scoring (Emergency = 1, Normal = 0).</li>
            <li>Integrate Google Gmail API (OAuth 2.0) with AES-256 encrypted refresh tokens.</li>
            <li>Empower Medical Staff with prescription management (typed notes & scan uploads).</li>
            <li>Enhance Administrative Oversight with user management and OAuth setup panels.</li>
        </ul>

        <h1>Chapter 4: Scope of the Project</h1>
        <p>The scope encompasses end-to-end management of outpatient appointments, dynamic virtual queuing, electronic prescription management, and Google Gmail API OAuth 2.0 integration.</p>
    </div>

    <!-- CHAPTER 5 & 6 -->
    <div class="page-break">
        <h1>Chapter 5: Literature Review</h1>
        <p>The digitization of healthcare administration has been a focal point of software engineering research for the past two decades. Early iterations of Hospital Information Systems (HIS) were primarily focused on internal billing and inventory management, leaving patient scheduling as a manual task. As internet accessibility proliferated, web-based appointment systems began to emerge.</p>
        <p>A study on web-based appointment scheduling systems indicates that digital booking significantly reduces patient "no-show" rates and optimizes clinic capacity utilization. However, many existing systems operate purely as static calendars; they allow a user to book a slot (e.g., 10:00 AM) but fail to account for the dynamic nature of medical consultations, which frequently run over their allotted time. When a 10:00 AM appointment stretches to 10:30 AM, subsequent patients are left waiting blindly.</p>
        <p>Modern research emphasizes the necessity of combining appointment scheduling with dynamic queue management (Virtual Queuing Systems - VQS). Systems that incorporate real-time queue tracking allow patients to monitor the queue remotely via web applications, enabling them to utilize their waiting time productively outside the hospital environment, reducing physical crowding and associated stress.</p>
        <p>Furthermore, integrating official OAuth 2.0 APIs (such as Google Gmail API <code>users.messages.send</code>) guarantees high deliverability and compliance without storing user passwords or relying on deprecated SMTP basic authentication.</p>

        <h1>Chapter 6: System Analysis</h1>
        <p>System analysis involves a detailed evaluation of the current operational methods to identify bottlenecks and conceptualize a software solution designed to rectify these issues.</p>
        <h3>Existing System</h3>
        <p>In manual or semi-automated systems, patients typically secure appointments via telephone calls or by physically visiting the hospital reception. The receptionist logs the appointment in a physical ledger. On appointment day, patients collect a physical token and sit in a waiting area. The doctor's assistant manually calls out token numbers.</p>
        <h3>Problems in Existing System</h3>
        <ul>
            <li><strong>Lack of Transparency:</strong> Patients have no visibility into how many people are ahead of them.</li>
            <li><strong>Physical Congestion:</strong> Waiting areas become severely congested.</li>
            <li><strong>Administrative Overhead:</strong> Reception staff spend excessive time answering phone calls.</li>
            <li><strong>Static Priority Handling:</strong> Manually prioritizing emergency cases is prone to human error and bias.</li>
        </ul>
        <h3>Proposed System</h3>
        <p>The proposed system is a centralized web application that automates the entire workflow. Patients create accounts, verify registration via 6-digit OTP, and book appointments online. The system's backend automatically assigns a queue number, computes priority scores, and calculates estimated wait times. On consultation day, patients check live queue progression on their device. Doctors interact with a dedicated dashboard to start/complete consultations and attach prescription scan images, triggering automated email notifications via Google Gmail API.</p>
    </div>

    <!-- CHAPTER 7 & 8 -->
    <div class="page-break">
        <h1>Chapter 7: Feasibility Study</h1>
        <p>Before development, a comprehensive feasibility study evaluated the proposed system across four primary dimensions:</p>
        <h3>Technical Feasibility</h3>
        <p>The selected technology stack (Java 21, Spring Boot 3.2.5, MySQL 8.0, Thymeleaf) is well-established, enterprise-grade, and actively maintained. Spring Boot is specifically designed to handle multi-user concurrency and relational data mapping, making it technically ideal.</p>
        <h3>Economic Feasibility</h3>
        <p>Economically feasible. The development utilizes free and open-source software (FOSS). Spring Boot, MySQL, and Tailwind CSS do not require commercial licensing fees.</p>
        <h3>Operational Feasibility</h3>
        <p>Operationally feasible. The responsive frontend utilizes intuitive UI paradigms (dashboards, action buttons), minimizing the learning curve for hospital staff and patients.</p>
        <h3>Legal Feasibility</h3>
        <p>Legally feasible. Passwords are cryptographically hashed using BCrypt, OAuth refresh tokens are encrypted at rest using AES-256, and Spring Security enforces strict role-based access control.</p>

        <h1>Chapter 8: Requirement Analysis</h1>
        <h3>Functional Requirements</h3>
        <ul>
            <li><strong>Authentication Module:</strong> Patient registration, 6-digit OTP verification, BCrypt authentication, role-based routing (Admin, Doctor, Patient).</li>
            <li><strong>Patient Module:</strong> View/update profile, browse doctors by specialization, book normal/emergency appointments, view live queue position, view prescriptions.</li>
            <li><strong>Doctor Module:</strong> Toggle availability, view daily queue board, update consultation status (IN_PROGRESS, COMPLETED), attach prescription notes and image scans.</li>
            <li><strong>Admin Module:</strong> View statistical dashboard, add/delete doctors and patients, schedule/reschedule appointments, manage Google Gmail API OAuth settings.</li>
        </ul>
        <h3>Non-Functional Requirements</h3>
        <ul>
            <li><strong>Security:</strong> CSRF state validation, URL authorization checks, AES-256 token encryption at rest.</li>
            <li><strong>Responsiveness:</strong> Adapts fluidly across mobile, tablet, and desktop viewports.</li>
            <li><strong>Performance:</strong> Sub-second queue sorting and dynamic wait time recalculations.</li>
        </ul>
    </div>

    <!-- CHAPTER 9: SYSTEM DESIGN & DIAGRAMS -->
    <div class="page-break">
        <h1>Chapter 9: System Design & Diagrams</h1>

        <h2>9.1 System Architecture Diagram</h2>
        <div class="diagram-container">
            <div class="diagram-title">Figure 9.1: Multi-Tiered Enterprise MVC & Google OAuth Architecture</div>
            <svg width="600" height="260" viewBox="0 0 600 260" xmlns="http://www.w3.org/2000/svg">
                <rect x="20" y="20" width="160" height="60" rx="8" fill="#3b82f6" />
                <text x="100" y="45" fill="white" font-size="12" font-weight="bold" text-anchor="middle">Client Browser</text>
                <text x="100" y="65" fill="white" font-size="10" text-anchor="middle">(Patient / Doctor / Admin)</text>

                <rect x="220" y="20" width="160" height="60" rx="8" fill="#1d4ed8" />
                <text x="300" y="45" fill="white" font-size="12" font-weight="bold" text-anchor="middle">Spring Security 6</text>
                <text x="300" y="65" fill="white" font-size="10" text-anchor="middle">RBAC & CSRF Protection</text>

                <rect x="420" y="20" width="160" height="60" rx="8" fill="#1e40af" />
                <text x="500" y="45" fill="white" font-size="12" font-weight="bold" text-anchor="middle">Spring MVC Controllers</text>
                <text x="500" y="65" fill="white" font-size="10" text-anchor="middle">@Controller / @ResponseBody</text>

                <rect x="220" y="110" width="160" height="60" rx="8" fill="#0f766e" />
                <text x="300" y="135" fill="white" font-size="12" font-weight="bold" text-anchor="middle">Service Layer</text>
                <text x="300" y="155" fill="white" font-size="10" text-anchor="middle">GmailApiService / EmailService</text>

                <rect x="20" y="190" width="160" height="50" rx="8" fill="#047857" />
                <text x="100" y="220" fill="white" font-size="12" font-weight="bold" text-anchor="middle">MySQL Database</text>

                <rect x="420" y="190" width="160" height="50" rx="8" fill="#dc2626" />
                <text x="500" y="220" fill="white" font-size="12" font-weight="bold" text-anchor="middle">Google Gmail API</text>

                <line x1="180" y1="50" x2="220" y2="50" stroke="#1e3a8a" stroke-width="2" />
                <line x1="380" y1="50" x2="420" y2="50" stroke="#1e3a8a" stroke-width="2" />
                <line x1="300" y1="80" x2="300" y2="110" stroke="#1e3a8a" stroke-width="2" />
                <line x1="220" y1="140" x2="100" y2="190" stroke="#1e3a8a" stroke-width="2" />
                <line x1="380" y1="140" x2="500" y2="190" stroke="#1e3a8a" stroke-width="2" />
            </svg>
        </div>

        <h2>9.2 Use Case Diagram</h2>
        <div class="diagram-container">
            <div class="diagram-title">Figure 9.2: System Actors & Use Cases</div>
            <svg width="600" height="240" viewBox="0 0 600 240" xmlns="http://www.w3.org/2000/svg">
                <circle cx="50" cy="50" r="18" fill="#3b82f6" />
                <text x="50" y="85" font-size="10" font-weight="bold" text-anchor="middle">Patient</text>

                <circle cx="50" cy="130" r="18" fill="#10b981" />
                <text x="50" y="165" font-size="10" font-weight="bold" text-anchor="middle">Doctor</text>

                <circle cx="50" cy="200" r="18" fill="#8b5cf6" />
                <text x="50" y="235" font-size="10" font-weight="bold" text-anchor="middle">Admin</text>

                <ellipse cx="230" cy="40" rx="90" ry="20" fill="#eff6ff" stroke="#3b82f6" stroke-width="2" />
                <text x="230" y="44" font-size="9" text-anchor="middle">Register Account (OTP)</text>

                <ellipse cx="230" cy="90" rx="90" ry="20" fill="#eff6ff" stroke="#3b82f6" stroke-width="2" />
                <text x="230" y="94" font-size="9" text-anchor="middle">Book Appointment (Normal/Emerg)</text>

                <ellipse cx="440" cy="40" rx="90" ry="20" fill="#ecfdf5" stroke="#10b981" stroke-width="2" />
                <text x="440" y="44" font-size="9" text-anchor="middle">Start / Complete Consultation</text>

                <ellipse cx="440" cy="90" rx="90" ry="20" fill="#ecfdf5" stroke="#10b981" stroke-width="2" />
                <text x="440" y="94" font-size="9" text-anchor="middle">Attach Prescription & Image</text>

                <ellipse cx="330" cy="160" rx="100" ry="20" fill="#f5f3ff" stroke="#8b5cf6" stroke-width="2" />
                <text x="330" y="164" font-size="9" text-anchor="middle">Manage Users & Appointments</text>

                <ellipse cx="330" cy="210" rx="100" ry="20" fill="#f5f3ff" stroke="#8b5cf6" stroke-width="2" />
                <text x="330" y="214" font-size="9" text-anchor="middle">Configure Google Gmail OAuth</text>

                <line x1="68" y1="50" x2="140" y2="40" stroke="#64748b" stroke-width="1.5" />
                <line x1="68" y1="50" x2="140" y2="90" stroke="#64748b" stroke-width="1.5" />
                <line x1="68" y1="130" x2="350" y2="40" stroke="#64748b" stroke-width="1.5" />
                <line x1="68" y1="130" x2="350" y2="90" stroke="#64748b" stroke-width="1.5" />
                <line x1="68" y1="200" x2="230" y2="160" stroke="#64748b" stroke-width="1.5" />
                <line x1="68" y1="200" x2="230" y2="210" stroke="#64748b" stroke-width="1.5" />
            </svg>
        </div>
    </div>

    <!-- CHAPTER 10: DATABASE & ER DIAGRAM -->
    <div class="page-break">
        <h1>Chapter 10: Database Design & ER Diagram</h1>

        <h2>10.1 Entity-Relationship (ER) Diagram</h2>
        <div class="diagram-container">
            <div class="diagram-title">Figure 10.1: Database Entity-Relationship Diagram</div>
            <svg width="600" height="300" viewBox="0 0 600 300" xmlns="http://www.w3.org/2000/svg">
                <!-- Row 1: Authentication & Integrations -->
                <rect x="20" y="20" width="125" height="65" rx="6" fill="#be123c" />
                <text x="82" y="40" fill="white" font-size="10" font-weight="bold" text-anchor="middle">OTP_TOKENS</text>
                <text x="82" y="58" fill="#fecdd3" font-size="8" text-anchor="middle">id (PK), email, code</text>
                <text x="82" y="72" fill="#fecdd3" font-size="8" text-anchor="middle">type, expiry_time</text>

                <rect x="175" y="20" width="130" height="65" rx="6" fill="#1e3a8a" />
                <text x="240" y="40" fill="white" font-size="11" font-weight="bold" text-anchor="middle">USERS</text>
                <text x="240" y="58" fill="#93c5fd" font-size="9" text-anchor="middle">id (PK), email (UK)</text>
                <text x="240" y="72" fill="#93c5fd" font-size="9" text-anchor="middle">password, role</text>

                <rect x="335" y="20" width="150" height="65" rx="6" fill="#dc2626" />
                <text x="410" y="40" fill="white" font-size="10" font-weight="bold" text-anchor="middle">GMAIL_INTEGRATIONS</text>
                <text x="410" y="58" fill="#fecca3" font-size="8" text-anchor="middle">id (PK), email (UK)</text>
                <text x="410" y="72" fill="#fecca3" font-size="8" text-anchor="middle">encrypted_refresh_token</text>

                <!-- Row 2: User Profiles & Appointments -->
                <rect x="65" y="115" width="130" height="65" rx="6" fill="#0f766e" />
                <text x="130" y="135" fill="white" font-size="11" font-weight="bold" text-anchor="middle">PATIENTS</text>
                <text x="130" y="153" fill="#99f6e4" font-size="9" text-anchor="middle">id (PK), user_id (FK)</text>
                <text x="130" y="167" fill="#99f6e4" font-size="9" text-anchor="middle">first_name, phone</text>

                <rect x="235" y="115" width="130" height="65" rx="6" fill="#047857" />
                <text x="300" y="135" fill="white" font-size="11" font-weight="bold" text-anchor="middle">DOCTORS</text>
                <text x="300" y="153" fill="#a7f3d0" font-size="9" text-anchor="middle">id (PK), user_id (FK)</text>
                <text x="300" y="167" fill="#a7f3d0" font-size="9" text-anchor="middle">specialization</text>

                <rect x="410" y="115" width="145" height="65" rx="6" fill="#b45309" />
                <text x="482" y="135" fill="white" font-size="11" font-weight="bold" text-anchor="middle">APPOINTMENTS</text>
                <text x="482" y="153" fill="#fde68a" font-size="9" text-anchor="middle">id (PK), type, status</text>
                <text x="482" y="167" fill="#fde68a" font-size="9" text-anchor="middle">patient_id, doctor_id</text>

                <!-- Row 3: Queues & Prescriptions -->
                <rect x="235" y="210" width="130" height="65" rx="6" fill="#c2410c" />
                <text x="300" y="230" fill="white" font-size="11" font-weight="bold" text-anchor="middle">QUEUES</text>
                <text x="300" y="248" fill="#ffedd5" font-size="9" text-anchor="middle">id (PK), queue_number</text>
                <text x="300" y="262" fill="#ffedd5" font-size="9" text-anchor="middle">priority_score, status</text>

                <rect x="410" y="210" width="145" height="65" rx="6" fill="#6d28d9" />
                <text x="482" y="230" fill="white" font-size="11" font-weight="bold" text-anchor="middle">PRESCRIPTIONS</text>
                <text x="482" y="248" fill="#ddd6fe" font-size="9" text-anchor="middle">id (PK), notes</text>
                <text x="482" y="262" fill="#ddd6fe" font-size="9" text-anchor="middle">image_path, appointment_id</text>

                <!-- CONNECTING LINES WITH CARDINALITY -->
                <!-- USERS to OTP_TOKENS -->
                <line x1="175" y1="52" x2="145" y2="52" stroke="#475569" stroke-width="2" />
                <text x="160" y="47" font-size="8" font-weight="bold" fill="#475569">1:N</text>

                <!-- USERS to GMAIL_INTEGRATIONS -->
                <line x1="305" y1="52" x2="335" y2="52" stroke="#475569" stroke-width="2" />
                <text x="318" y="47" font-size="8" font-weight="bold" fill="#475569">1:1</text>

                <!-- USERS to PATIENTS -->
                <line x1="220" y1="85" x2="145" y2="115" stroke="#475569" stroke-width="2" />
                <text x="175" y="98" font-size="8" font-weight="bold" fill="#475569">1:1</text>

                <!-- USERS to DOCTORS -->
                <line x1="260" y1="85" x2="285" y2="115" stroke="#475569" stroke-width="2" />
                <text x="275" y="98" font-size="8" font-weight="bold" fill="#475569">1:1</text>

                <!-- PATIENTS to APPOINTMENTS -->
                <line x1="195" y1="147" x2="410" y2="147" stroke="#475569" stroke-width="2" />
                <text x="210" y="142" font-size="8" font-weight="bold" fill="#475569">1:N</text>

                <!-- DOCTORS to APPOINTMENTS -->
                <line x1="365" y1="147" x2="410" y2="147" stroke="#475569" stroke-width="2" />
                <text x="380" y="142" font-size="8" font-weight="bold" fill="#475569">1:N</text>

                <!-- APPOINTMENTS to QUEUES -->
                <line x1="440" y1="180" x2="335" y2="210" stroke="#475569" stroke-width="2" />
                <text x="395" y="195" font-size="8" font-weight="bold" fill="#475569">1:1</text>

                <!-- APPOINTMENTS to PRESCRIPTIONS -->
                <line x1="482" y1="180" x2="482" y2="210" stroke="#475569" stroke-width="2" />
                <text x="487" y="198" font-size="8" font-weight="bold" fill="#475569">1:1</text>
            </svg>
        </div>

        <h2>10.2 Data Dictionary Tables</h2>
        <table>
            <thead>
                <tr>
                    <th>Table</th>
                    <th>Field Name</th>
                    <th>Data Type</th>
                    <th>Constraints</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <tr><td>users</td><td>email</td><td>VARCHAR(255)</td><td>UNIQUE, NOT NULL</td><td>Login identifier</td></tr>
                <tr><td>users</td><td>password</td><td>VARCHAR(255)</td><td>NOT NULL</td><td>BCrypt hashed password</td></tr>
                <tr><td>appointments</td><td>type</td><td>VARCHAR(50)</td><td>NOT NULL</td><td>NORMAL or EMERGENCY</td></tr>
                <tr><td>queues</td><td>priority_score</td><td>INT</td><td>NOT NULL</td><td>1 for Emergency, 0 for Normal</td></tr>
                <tr><td>prescriptions</td><td>image_path</td><td>VARCHAR(500)</td><td>NULLABLE</td><td>Path to uploaded scan file</td></tr>
                <tr><td>gmail_integrations</td><td>encrypted_refresh_token</td><td>VARCHAR(1000)</td><td>NULLABLE</td><td>AES-256 encrypted refresh token</td></tr>
            </tbody>
        </table>
    </div>

    <!-- CHAPTER 11 & 12 -->
    <div class="page-break">
        <h1>Chapter 11: Implementation Details</h1>
        <p>The implementation phase involved writing the backend logic in Java 21 with Spring Boot 3.2.5, configuring Spring Security 6 for RBAC, building JPA Repositories for MySQL, and integrating Google Gmail API Client libraries.</p>
        <p>Key services implemented include <code>GmailApiService.java</code> for Base64 MIME encoding and OAuth token refresh, <code>TokenEncryptionService.java</code> for AES-256 refresh token encryption, and <code>EmailService.java</code> for central mail dispatch.</p>

        <h1>Chapter 12: Testing</h1>
        <p>Comprehensive automated end-to-end testing was executed across all core modules using Python verification scripts.</p>

        <h2>12.1 Verification Test Matrix</h2>
        <table>
            <thead>
                <tr>
                    <th>Test Suite</th>
                    <th>Module</th>
                    <th>Action / Endpoint</th>
                    <th>Expected Result</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>test_gmail_oauth_api.py</td>
                    <td>Gmail API OAuth</td>
                    <td>GET /api/integrations/gmail/status</td>
                    <td>Return clean OAuth status payload</td>
                    <td><span class="badge badge-pass">PASS</span></td>
                </tr>
                <tr>
                    <td>test_email_otp_prescription.py</td>
                    <td>OTP Verification</td>
                    <td>POST /register & /verify-otp</td>
                    <td>Generate 6-digit OTP code in DB</td>
                    <td><span class="badge badge-pass">PASS</span></td>
                </tr>
                <tr>
                    <td>test_prescription_flow.py</td>
                    <td>Prescription Upload</td>
                    <td>POST /doctor/appointment/{id}/prescription</td>
                    <td>Upload 5MB image to ./uploads/</td>
                    <td><span class="badge badge-pass">PASS</span></td>
                </tr>
                <tr>
                    <td>test_admin_buttons_and_smtp.py</td>
                    <td>Admin Controls</td>
                    <td>POST /admin/doctors/add & /patients/add</td>
                    <td>Create Doctor & Patient profiles</td>
                    <td><span class="badge badge-pass">PASS</span></td>
                </tr>
            </tbody>
        </table>
    </div>

    <!-- CHAPTER 13, 14, 15 & 16 -->
    <div class="page-break">
        <h1>Chapter 13: Results & Discussion</h1>
        <p>The successful compilation, deployment, and testing of the Hospital Virtual Appointment Booking Queue System demonstrate that all specified objectives have been met. The system operates as a cohesive, reliable platform capable of managing complex hospital scheduling dynamics.</p>
        <h3>Performance & Security</h3>
        <p>Spring Data JPA queries execute in sub-milliseconds, and Spring Security enforces strict route isolation. Passwords are BCrypt-hashed, and OAuth refresh tokens are encrypted at rest with AES-256.</p>

        <h1>Chapter 14: Conclusion</h1>
        <p>The development of the Hospital Virtual Appointment Booking Queue System represents a significant technological upgrade over traditional medical scheduling practices. By migrating the queuing process from crowded hospital waiting rooms to a secure, accessible web platform, the system addresses the core inefficiencies of outpatient management.</p>

        <h1>Chapter 15: Future Enhancements</h1>
        <ul>
            <li><strong>SMS Gateway Integration:</strong> Twilio SMS alerts for approaching queue numbers.</li>
            <li><strong>Electronic Health Records (EHR):</strong> Historical medical chart tracking.</li>
            <li><strong>Machine Learning for Wait Times:</strong> AI-driven consultation duration predictions per doctor.</li>
        </ul>

        <h1>Chapter 16: References</h1>
        <ol>
            <li>Craig Walls, <em>Spring in Action, Sixth Edition</em>, Manning Publications, 2022.</li>
            <li>Laurentiu Spilca, <em>Spring Security in Action</em>, Manning Publications, 2020.</li>
            <li>Google Developers, <em>Gmail API OAuth 2.0 Authorization Guide</em>, 2026.</li>
            <li>Spring Boot Reference Documentation, <em>Pivotal Software</em>, 2024.</li>
            <li>MySQL 8.0 Reference Manual, <em>Oracle Corporation</em>, 2024.</li>
        </ol>
    </div>

</body>
</html>
"""

with open('/tmp/report_template.html', 'w') as f:
    f.write(html_content)

output_pdf_path = '/home/ubuntu/repostring/project_report.pdf'
weasyprint.HTML('/tmp/report_template.html').write_pdf(output_pdf_path)
print(f"✅ Re-generated PDF report at {output_pdf_path} (Size: {os.path.getsize(output_pdf_path)} bytes)")
