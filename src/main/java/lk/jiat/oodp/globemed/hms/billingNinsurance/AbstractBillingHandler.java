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
public abstract class AbstractBillingHandler {

    protected AbstractBillingHandler nextHandler;
    protected HandlerStatusListener statusListener; 
    public String handlerName;

    public AbstractBillingHandler(String handlerName) {
        this.handlerName = handlerName;
    }

    public AbstractBillingHandler getNextHandler() {
        return this.nextHandler;
    }
    
    public void setNextHandler(AbstractBillingHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void setStatusListener(HandlerStatusListener listener) {
        this.statusListener = listener;
        if (nextHandler != null) {
            nextHandler.setStatusListener(listener);
        }
    }

    public abstract void handleRequest(BillingClaim claim);

    protected void notifyStarted() {
        if (statusListener != null) {
            statusListener.onHandlerStarted(this.handlerName);
        }
    }

    protected void notifyCompleted(boolean success, String message) {
        if (statusListener != null) {
            statusListener.onHandlerCompleted(this.handlerName, success, message);
        }
    }
}
