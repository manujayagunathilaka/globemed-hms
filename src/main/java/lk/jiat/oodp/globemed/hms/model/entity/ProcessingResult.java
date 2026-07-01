/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 *
 * @author Manujaya
 */
@Embeddable
public class ProcessingResult {

    private double approvedAmount;
    private double patientResponsibility;
    private double insuranceCovered;
    private String approvalCode;
    @Column(length = 500) 
    private String processingNotes;

    public ProcessingResult() {
    }

    public ProcessingResult(double approvedAmount, double patientResponsibility, double insuranceCovered, String approvalCode, String processingNotes) {
        this.approvedAmount = approvedAmount;
        this.patientResponsibility = patientResponsibility;
        this.insuranceCovered = insuranceCovered;
        this.approvalCode = approvalCode;
        this.processingNotes = processingNotes;
    }

    public double getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(double approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public double getPatientResponsibility() {
        return patientResponsibility;
    }

    public void setPatientResponsibility(double patientResponsibility) {
        this.patientResponsibility = patientResponsibility;
    }

    public double getInsuranceCovered() {
        return insuranceCovered;
    }

    public void setInsuranceCovered(double insuranceCovered) {
        this.insuranceCovered = insuranceCovered;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }

    public String getProcessingNotes() {
        return processingNotes;
    }

    public void setProcessingNotes(String processingNotes) {
        this.processingNotes = processingNotes;
    }

    
}
