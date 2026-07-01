/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.billingNinsurance;

import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;

/**
 *
 * @author Manujaya
 */
public interface HandlerStatusListener {
    void onHandlerStarted(String handlerName);

    void onHandlerCompleted(String handlerName, boolean success, String message);

    void onProcessingComplete(BillingClaim claim);
}
