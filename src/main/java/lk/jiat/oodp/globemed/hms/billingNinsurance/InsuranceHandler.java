/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.billingNinsurance;

import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.enums.BillingStatus;
import lk.jiat.oodp.globemed.hms.model.enums.BillingType;

/**
 *
 * @author Manujaya
 */
public class InsuranceHandler extends AbstractBillingHandler {

    public InsuranceHandler() {
        super("Insurance Eligibility Check");
    }

    @Override
    public void handleRequest(BillingClaim claim) {
        notifyStarted();
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        if (claim.getBillingType() == BillingType.INSURANCE) {
            System.out.println("HANDLER [Insurance]: Insurance claim found. Validating policy...");

            boolean isEligible = Math.random() > 0.5; // 50% chance of success

            if (isEligible) {
                double coveredAmount = claim.getAmount() * 0.9; // 90% coverage
                claim.getResult().setInsuranceCovered(coveredAmount);
                claim.getResult().setPatientResponsibility(claim.getAmount() - coveredAmount);
                notifyCompleted(true, "Policy is valid. Coverage applied.");
                if (nextHandler != null) {
                    nextHandler.handleRequest(claim);
                }
            } else {
                claim.setStatus(BillingStatus.REJECTED);
                claim.setErrorMessage("Patient is not eligible for insurance coverage.");
                notifyCompleted(false, "Eligibility Check Failed!");
            }
        } else {
            System.out.println("HANDLER [Insurance]: No insurance claim. Skipping.");
            notifyCompleted(true, "Skipped (Direct Billing).");
            if (nextHandler != null) {
                nextHandler.handleRequest(claim);
            }
        }
    }
}
