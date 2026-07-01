/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.appointment;

import lk.jiat.oodp.globemed.hms.model.entity.Patient;

/**
 *
 * @author Manujaya
 */
public class PatientColleague implements AppointmentColleague {

    private Patient patient; // Wraps the Patient entity

    public PatientColleague(Patient patient) {
        this.patient = patient;
    }

    @Override
    public String getName() {
        return patient.getName(); 
    }

    @Override
    public void receiveNotification(String message) {
        System.out.println("MEDIATOR-INFO [for Patient " + patient.getName() + "]: " + message);
    }

    @Override
    public int getId() {
        return patient.getId();
    }

    public Patient getPatientEntity() {
        return this.patient;
    }
}
