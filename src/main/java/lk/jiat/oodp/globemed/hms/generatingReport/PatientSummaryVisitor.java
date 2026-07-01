/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.generatingReport;

import lk.jiat.oodp.globemed.hms.model.entity.Appointment;
import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.entity.DiagnosticResult;

/**
 *
 * @author Manujaya
 */
public class PatientSummaryVisitor implements ReportVisitor {

    private StringBuilder report = new StringBuilder();

    @Override
    public void visit(Patient patient) {
        report.append("--- PATIENT TREATMENT SUMMARY ---\n");
        report.append("Patient Name: ").append(patient.getName()).append("\n");
    }

    @Override
    public void visit(Appointment appointment) {
        report.append("\n  -> Appointment on ").append(appointment.getAppointmentTime().toLocalDate());
        report.append(" with ").append(appointment.getDoctor());
    }

    @Override
    public void visit(BillingClaim claim) {
        report.append("\n  -> Bill on ").append(claim.getCreatedDate());
        report.append(" for LKR ").append(claim.getAmount());
    }

    @Override
    public void visit(DiagnosticResult result) {
        report.append("\n  -> Diagnostic Result (").append(result.getReportType()).append(") on ").append(result.getReportDate());
    }

    public String getReport() {
        return report.toString();
    }
}
