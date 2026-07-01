/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.billingNinsurance;

import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.enums.BillingStatus;

/**
 *
 * @author Manujaya
 */
public class InitialValidationHandler extends AbstractBillingHandler {

    public InitialValidationHandler() {
        super("Initial Validation");
    }

    @Override
    public void handleRequest(BillingClaim claim) {
        notifyStarted();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        } 

        if (claim.getPatient() != null && claim.getAmount() > 0) {
            notifyCompleted(true, "Patient and amount are valid.");
            if (nextHandler != null) {
                nextHandler.handleRequest(claim);
            }
        } else {
            claim.setStatus(BillingStatus.FAILED);
            claim.setErrorMessage("Invalid patient or amount.");
            notifyCompleted(false, "Validation Failed!");
        }
    }
}
