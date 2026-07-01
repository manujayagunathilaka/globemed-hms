/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

/**
 *
 * @author Manujaya
 */
@Entity
public class DiagnosticResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    private Patient patient;

    @ManyToOne
    private Doctor orderedBy;

    private String reportType;

    @Lob 
    private String findings;

    @Temporal(TemporalType.DATE)
    private Date reportDate;

    public DiagnosticResult() {
    }
    
    public DiagnosticResult(Patient patient, Doctor orderedBy, String reportType, String findings, Date reportDate) {
        this.patient = patient;
        this.orderedBy = orderedBy;
        this.reportType = reportType;
        this.findings = findings;
        this.reportDate = reportDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(Doctor orderedBy) {
        this.orderedBy = orderedBy;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }
    
    
    
}
