/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.billingNinsurance;

/**
 *
 * @author Manujaya
 */
public class BillingChainBuilder {
    
    public static AbstractBillingHandler buildInsuranceChain() {
        AbstractBillingHandler validation = new InitialValidationHandler();
        AbstractBillingHandler pricing = new PricingAndDiscountHandler();
        AbstractBillingHandler deductible = new DeductibleCheckHandler();
        AbstractBillingHandler insurance = new InsuranceHandler();
        AbstractBillingHandler copay = new CoPaymentHandler();
        AbstractBillingHandler finalBilling = new FinalBillingHandler();

        validation.setNextHandler(pricing);
        pricing.setNextHandler(deductible);
        deductible.setNextHandler(insurance);
        insurance.setNextHandler(copay);
        copay.setNextHandler(finalBilling);

        return validation;
    }
    
    public static AbstractBillingHandler buildDirectChain() {
        AbstractBillingHandler validation = new InitialValidationHandler();
        AbstractBillingHandler pricing = new PricingAndDiscountHandler(); 
        AbstractBillingHandler finalBilling = new FinalBillingHandler();

        validation.setNextHandler(pricing);
        pricing.setNextHandler(finalBilling);

        return validation;
    }
      
}
