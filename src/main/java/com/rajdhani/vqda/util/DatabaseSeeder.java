package com.rajdhani.vqda.util;

import com.rajdhani.vqda.model.*;
import com.rajdhani.vqda.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            seedData();
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void seedData() {
        // Roles
        Role adminRole = new Role(null, "ADMIN", null);
        Role doctorRole = new Role(null, "DOCTOR", null);
        Role patientRole = new Role(null, "PATIENT", null);

        List<Role> savedRoles = roleRepository.saveAll(Arrays.asList(adminRole, doctorRole, patientRole));
        adminRole = savedRoles.get(0);
        doctorRole = savedRoles.get(1);
        patientRole = savedRoles.get(2);

        // Admin User
        User admin = new User();
        admin.setEmail("admin@hospital.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        // Seed Doctors
        String[] specializations = {"Cardiologist", "Neurologist", "Pediatrician", "Orthopedist", "Dermatologist"};
        String[] firstNames = {"Sanjay", "Bikash", "Prabin", "Anil", "Deepak", "Sandesh", "Nabin", "Prakash", "Ramesh", "Kiran"};
        String[] lastNames = {"Thapa", "Shrestha", "Karki", "Maharjan", "Gurung", "Tamang", "Magar", "Rai", "Giri", "Sharma"};
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            User doctorUser = new User();
            // e.g., sanjay.thapa@rajdhanihealthline.com
            String email = firstNames[i].toLowerCase() + "." + lastNames[i].toLowerCase() + "@rajdhanihealthline.com";
            doctorUser.setEmail(email);
            doctorUser.setPassword(passwordEncoder.encode("doctor123"));
            doctorUser.getRoles().add(doctorRole);

            Doctor doctor = new Doctor();
            doctor.setUser(doctorUser);
            doctor.setFirstName(firstNames[i]);
            doctor.setLastName(lastNames[i]);
            doctor.setSpecialization(specializations[random.nextInt(specializations.length)]);
            doctor.setExperienceYears(random.nextInt(20) + 1);
            doctor.setPhone("98" + (10000000 + random.nextInt(89999999))); // Nepali mobile number format
            doctor.setAvailabilityStatus(true);
            doctorRepository.save(doctor);
        }

        // Seed Patients
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
        String[] patientFirstNames = {"Aarav", "Aayush", "Bishal", "Chetan", "Dinesh", "Gaurav", "Hari", "Ishwor", "Kamal", "Laxman", 
                                      "Manoj", "Niraj", "Om", "Pawan", "Roshan", "Suman", "Sushil", "Umesh", "Yubaraj", "Binod",
                                      "Anjali", "Bina", "Chanda", "Deepa", "Gita", "Hema", "Indira", "Jamuna", "Kopila", "Laxmi",
                                      "Manisha", "Nita", "Pooja", "Roshni", "Sita", "Sushma", "Urmila", "Yamuna", "Bimala", "Kalpana"};
        String[] patientLastNames = {"Sharma", "Adhikari", "Nepal", "Koirala", "Dahal", "Poudel", "Ghimire", "Bhattarai", "Oli", "Bhandari",
                                     "Khatri", "Basnet", "Rana", "Shah", "Thakuri", "Singh", "Yadav", "Chaudhary", "Tharu", "Lama"};

        for (int i = 0; i < 50; i++) {
            User patientUser = new User();
            String firstName = (i == 0) ? "Aarav" : patientFirstNames[random.nextInt(patientFirstNames.length)];
            String lastName = (i == 0) ? "Sharma" : patientLastNames[random.nextInt(patientLastNames.length)];
            
            // e.g. aarav.sharma1@gmail.com
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + (i + 1) + "@gmail.com";
            
            patientUser.setEmail(email);
            patientUser.setPassword(passwordEncoder.encode("patient123"));
            patientUser.getRoles().add(patientRole);

            Patient patient = new Patient();
            patient.setUser(patientUser);
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setPhone("98" + (40000000 + random.nextInt(59999999))); // Nepali number
            patient.setDob(LocalDate.of(1960 + random.nextInt(50), 1 + random.nextInt(12), 1 + random.nextInt(28)));
            
            // Guess gender based on index roughly
            boolean isFemale = Arrays.asList(patientFirstNames).indexOf(firstName) >= 20;
            patient.setGender(isFemale ? "Female" : "Male");
            
            patient.setBloodGroup(bloodGroups[random.nextInt(bloodGroups.length)]);
            patientRepository.save(patient);
        }
    }
}
