/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.billingNinsurance;

import lk.jiat.oodp.globemed.hms.billingNinsurance.logs.AuditLoggerService;
import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.enums.BillingStatus;

/**
 *
 * @author Manujaya
 */
public class FinalBillingHandler extends AbstractBillingHandler {

    private final AuditLoggerService loggerService;
    
    public FinalBillingHandler() {
        super("Final Invoice Generation");
        this.loggerService = new AuditLoggerService();
    }

    @Override
    public void handleRequest(BillingClaim claim) {
        notifyStarted();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        System.out.println("HANDLER [Billing]: Generating final invoice...");

        claim.getResult().setApprovedAmount(claim.getAmount()); 
        claim.setStatus(BillingStatus.COMPLETED);
        String notes = String.format("Invoice generated. Patient '%s' is responsible for LKR %.2f.",
                claim.getPatient().getName(), claim.getResult().getPatientResponsibility());
        claim.getResult().setProcessingNotes(notes);

        String details = String.format("Claim for Patient ID: %d, Final Amount: %.2f", claim.getPatient().getId(), claim.getResult().getPatientResponsibility());
        loggerService.log("BILLING_PROCESS_COMPLETED", details, true);
        
        System.out.println("HANDLER [Billing]: Process finished successfully.");
        notifyCompleted(true, "Invoice created successfully.");

        if (statusListener != null) {
            statusListener.onProcessingComplete(claim);
        }

        if (nextHandler != null) {
            nextHandler.handleRequest(claim);
        }
    }
}
