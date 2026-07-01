/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.generatingReport;

import lk.jiat.oodp.globemed.hms.model.entity.Appointment;
import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.entity.DiagnosticResult;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;

/**
 *
 * @author Manujaya
 */
public class DiagnosticResultsVisitor implements ReportVisitor {

    private StringBuilder report = new StringBuilder();

    @Override
    public void visit(Patient patient) {
        report.append("--- DIAGNOSTIC RESULTS REPORT FOR: ").append(patient.getName()).append(" ---\n");
    }

    @Override
    public void visit(Appointment appointment) {
        }

    @Override
    public void visit(BillingClaim claim) {
       }

    @Override
    public void visit(DiagnosticResult result) {
        report.append("\n----------------------------------------\n");
        report.append("Report Type: ").append(result.getReportType()).append("\n");
        report.append("Report Date: ").append(result.getReportDate()).append("\n");
        report.append("Ordered By: Dr. ").append(result.getOrderedBy().getName()).append("\n");
        report.append("Findings:\n").append(result.getFindings()).append("\n");
    }

    public String getReport() {
        return report.toString();
    }
}
