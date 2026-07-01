/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.appointment;

import lk.jiat.oodp.globemed.hms.model.entity.Doctor;

/**
 *
 * @author Manujaya
 */
public class DoctorColleague implements AppointmentColleague {

    private Doctor doctor; // Wraps the Doctor entity

    public DoctorColleague(Doctor doctor) {
        this.doctor = doctor;
    }

    @Override
    public String getName() {
        return doctor.toString();
    }

    @Override
    public void receiveNotification(String message) {
        System.out.println("MEDIATOR-INFO [for " + doctor.getName() + "]: " + message);
    }

    @Override
    public int getId() {
        return doctor.getId();
    }

    public Doctor getDoctorEntity() {
        return this.doctor;
    }
}
