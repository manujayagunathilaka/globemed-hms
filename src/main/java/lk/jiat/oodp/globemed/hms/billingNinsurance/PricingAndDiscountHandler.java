/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.billingNinsurance;

import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.util.AgeCalculator;

/**
 *
 * @author Manujaya
 */
public class PricingAndDiscountHandler extends AbstractBillingHandler {

    private static final int SENIOR_CITIZEN_AGE = 60;
    private static final double SENIOR_DISCOUNT_RATE = 0.10; // 10%

    public PricingAndDiscountHandler() {
        super("Pricing & Discounts");
    }

    @Override
    public void handleRequest(BillingClaim claim) {
        notifyStarted();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        System.out.println("HANDLER [Pricing]: Checking for applicable discounts...");

        int patientAge = AgeCalculator.calculateAge(claim.getPatient().getDateOfBirth());

        if (patientAge >= SENIOR_CITIZEN_AGE) {
            double originalAmount = claim.getAmount();
            double discount = originalAmount * SENIOR_DISCOUNT_RATE;
            double newAmount = originalAmount - discount;

            claim.setAmount(newAmount);

            String message = String.format("Senior Citizen Discount (10%%) applied. New amount: %.2f", newAmount);
            System.out.println("HANDLER [Pricing]: " + message);
            notifyCompleted(true, message);
        } else {
            System.out.println("HANDLER [Pricing]: No discounts applicable.");
            notifyCompleted(true, "No discounts applicable.");
        }

        if (nextHandler != null) {
            nextHandler.handleRequest(claim);
        }
    }
}
