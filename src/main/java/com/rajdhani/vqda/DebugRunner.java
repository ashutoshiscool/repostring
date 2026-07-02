package com.rajdhani.vqda;

import com.rajdhani.vqda.model.Appointment;
import com.rajdhani.vqda.model.Queue;
import com.rajdhani.vqda.repository.AppointmentRepository;
import com.rajdhani.vqda.repository.QueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DebugRunner implements CommandLineRunner {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private QueueRepository queueRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== APPOINTMENTS IN DB ======");
        List<Appointment> apps = appointmentRepository.findAll();
        for (Appointment a : apps) {
            System.out.println("ID: " + a.getId() + ", Doc: " + a.getDoctor().getUser().getEmail() + 
                               ", Date: " + a.getAppointmentDate() + ", Status: " + a.getStatus());
        }
        
        System.out.println("====== QUEUES IN DB ======");
        List<Queue> qs = queueRepository.findAll();
        for (Queue q : qs) {
            System.out.println("ID: " + q.getId() + ", AppID: " + q.getAppointment().getId() + 
                               ", QNum: " + q.getQueueNumber() + ", Status: " + q.getStatus());
        }
    }
}
