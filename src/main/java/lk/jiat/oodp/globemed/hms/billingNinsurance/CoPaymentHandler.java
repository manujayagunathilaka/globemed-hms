/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.billingNinsurance;

import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.enums.BillingType;

/**
 *
 * @author Manujaya
 */
public class CoPaymentHandler extends AbstractBillingHandler {

    private static final double COPAYMENT_AMOUNT = 500.00; 

    public CoPaymentHandler() {
        super("Co-Payment Calculation");
    }

    @Override
    public void handleRequest(BillingClaim claim) {
        notifyStarted();
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
        }

        if (claim.getBillingType() == BillingType.INSURANCE) {
            System.out.println("HANDLER [CoPay]: Calculating co-payment...");
            double currentPatientPays = claim.getResult().getPatientResponsibility();
            claim.getResult().setPatientResponsibility(currentPatientPays + COPAYMENT_AMOUNT);

            String message = String.format("Co-payment of LKR %.2f added.", COPAYMENT_AMOUNT);
            System.out.println("HANDLER [CoPay]: " + message);
            notifyCompleted(true, message);
        } else {
            notifyCompleted(true, "Skipped (Direct Billing).");
        }

        if (nextHandler != null) {
            nextHandler.handleRequest(claim);
        }
    }
}
