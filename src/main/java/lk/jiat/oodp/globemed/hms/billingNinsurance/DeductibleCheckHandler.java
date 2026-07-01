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
public class DeductibleCheckHandler extends AbstractBillingHandler {

    public DeductibleCheckHandler() {
        super("Deductible Check");
    }

    @Override
    public void handleRequest(BillingClaim claim) {
        notifyStarted();
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
        }

        if (claim.getBillingType() == BillingType.INSURANCE) {
            System.out.println("HANDLER [Deductible]: Checking patient's annual deductible status...");

            boolean deductibleMet = Math.random() > 0.2; 

            if (deductibleMet) {
                System.out.println("HANDLER [Deductible]: Deductible has been met. Proceeding.");
                notifyCompleted(true, "Deductible check passed.");
                if (nextHandler != null) {
                    nextHandler.handleRequest(claim);
                }
            } else {
                System.out.println("HANDLER [Deductible]: FAILED. Patient has not met the annual deductible.");
                claim.setStatus(BillingStatus.REJECTED);
                claim.setErrorMessage("Annual deductible not met. Insurance cannot be applied.");
                notifyCompleted(false, "Failed: Annual deductible not met.");
            }
        } else {
            notifyCompleted(true, "Skipped (Direct Billing).");
            if (nextHandler != null) {
                nextHandler.handleRequest(claim);
            }
        }
    }
}
