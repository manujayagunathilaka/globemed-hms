/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.appointment;

import java.time.LocalDateTime;
import lk.jiat.oodp.globemed.hms.model.dao.AppointmentDAO;
import lk.jiat.oodp.globemed.hms.model.entity.Appointment;
import lk.jiat.oodp.globemed.hms.model.enums.AppointmentStatus;
import lk.jiat.oodp.globemed.hms.model.enums.AppointmentType;

/**
 *
 * @author Manujaya
 */
public class AppointmentScheduler implements AppointmentMediator {

    private final AppointmentDAO appointmentDAO;

    public AppointmentScheduler() {
        this.appointmentDAO = new AppointmentDAO();
    }

    @Override
    public boolean requestAppointment(PatientColleague patient, DoctorColleague doctor, LocalDateTime slot, String location, AppointmentType type) {
        System.out.println("SCHEDULER: Request from " + patient.getName() + " for " + doctor.getName() + " at " + location);
        System.out.println("BACKEND-MEDIATOR: Received booking request. Checking for conflicts...");

        if (appointmentDAO.isSlotAvailable(doctor.getDoctorEntity(), slot, location)) {
            Appointment newAppointment = new Appointment();
            newAppointment.setPatient(patient.getPatientEntity());
            newAppointment.setDoctor(doctor.getDoctorEntity());
            newAppointment.setAppointmentTime(slot);
            newAppointment.setLocation(location); 
            newAppointment.setAppointmentType(type); 
            newAppointment.setStatus(AppointmentStatus.BOOKED);

            appointmentDAO.save(newAppointment);

            patient.receiveNotification("Your " + type + " at " + location + " is confirmed.");
            doctor.receiveNotification("New " + type + " with " + patient.getName() + " at " + location);
            return true;
        } else {
            patient.receiveNotification("The requested time slot at " + location + " is already booked.");
            return false;
        }
    }
}
