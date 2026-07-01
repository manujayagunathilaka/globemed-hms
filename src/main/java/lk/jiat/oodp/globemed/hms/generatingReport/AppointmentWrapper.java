/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.generatingReport;

import lk.jiat.oodp.globemed.hms.model.entity.Appointment;

/**
 *
 * @author Manujaya
 */
public class AppointmentWrapper implements Visitable {

    private final Appointment appointment;

    public AppointmentWrapper(Appointment app) {
        this.appointment = app;
    }

    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this.appointment);
    }
}
