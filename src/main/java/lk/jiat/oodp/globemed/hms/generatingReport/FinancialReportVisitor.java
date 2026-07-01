/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.generatingReport;

import lk.jiat.oodp.globemed.hms.model.entity.Appointment;
import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.entity.DiagnosticResult;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.enums.BillingStatus;
import lk.jiat.oodp.globemed.hms.model.enums.BillingType;

/**
 *
 * @author Manujaya
 */
public class FinancialReportVisitor implements ReportVisitor {

    private double totalRevenue = 0;
    private double insuranceRevenue = 0;
    private double directRevenue = 0;

    @Override
    public void visit(Patient patient) {
        }

    @Override
    public void visit(Appointment appointment) {
        }

    @Override
    public void visit(DiagnosticResult result) {
        }

    @Override
    public void visit(BillingClaim claim) {
        if (claim.getStatus() == BillingStatus.COMPLETED) {
            totalRevenue += claim.getAmount();
            if (claim.getBillingType() == BillingType.INSURANCE) {
                insuranceRevenue += claim.getAmount();
            } else {
                directRevenue += claim.getAmount();
            }
        }
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getInsuranceRevenue() {
        return insuranceRevenue;
    }

    public double getDirectRevenue() {
        return directRevenue;
    }
    
    public String getReport() {
        return String.format(
                "--- FINANCIAL REPORT ---\nTotal Revenue: LKR %.2f\nFrom Insurance: LKR %.2f\nFrom Direct Payments: LKR %.2f\n",
                totalRevenue, insuranceRevenue, directRevenue
        );
    }
}
