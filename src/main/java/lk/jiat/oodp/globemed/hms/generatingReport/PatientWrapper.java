/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.generatingReport;

import lk.jiat.oodp.globemed.hms.model.entity.Patient;

/**
 *
 * @author Manujaya
 */
public class PatientWrapper implements Visitable {

    private final Patient patient;

    public PatientWrapper(Patient patient) {
        this.patient = patient;
    }

    @Override
    public void accept(ReportVisitor visitor) {
        visitor.visit(this.patient);
    }
}
