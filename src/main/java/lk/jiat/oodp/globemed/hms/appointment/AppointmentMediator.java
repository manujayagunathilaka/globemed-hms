/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.appointment;

import java.time.LocalDateTime;
import lk.jiat.oodp.globemed.hms.model.enums.AppointmentType;

/**
 *
 * @author Manujaya
 */
public interface AppointmentMediator {
    boolean requestAppointment(PatientColleague patient, DoctorColleague doctor, LocalDateTime slot, String location, AppointmentType type);
}
