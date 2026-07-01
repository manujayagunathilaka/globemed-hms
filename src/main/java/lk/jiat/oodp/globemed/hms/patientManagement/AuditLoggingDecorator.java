/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.patientManagement;

import java.util.Date;
import java.util.List;
import lk.jiat.oodp.globemed.hms.patientManagement.PatientRecordAccessor;
import lk.jiat.oodp.globemed.hms.patientManagement.RecordAccessorDecorator;
import lk.jiat.oodp.globemed.hms.exception.ValidationException;
import lk.jiat.oodp.globemed.hms.model.dao.AuditLogDAO;
import lk.jiat.oodp.globemed.hms.model.entity.AuditLog;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.entity.UserProfile;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;
import lk.jiat.oodp.globemed.hms.util.SessionManager;

/**
 *
 * @author Manujaya
 */
public class AuditLoggingDecorator extends RecordAccessorDecorator {
    
    private final AuditLogDAO auditLogDAO;

    public AuditLoggingDecorator(PatientRecordAccessor accessor) {
        super(accessor);
        this.auditLogDAO = new AuditLogDAO();
    }

    @Override
    public List<Object[]> getPersonalDetails(int patientId) {
        String action = "GET_PERSONAL_DETAILS";
        String details = "Patient ID: " + patientId;

        try {
            List<Object[]> result = super.getPersonalDetails(patientId);
            logAction(action, details, true);
            return result;
        } catch (Exception e) {
            logAction(action, details, false);
            throw e; 
        }
    }

    @Override
    public List<Object[]> getMedicalHistory(int patientId) {
        String action = "GET_MEDICAL_HISTORY";
        String details = "Patient ID: " + patientId;
        try {
            List<Object[]> result = super.getMedicalHistory(patientId);
            logAction(action, details, true);
            return result;
        } catch (Exception e) {
            logAction(action, details, false);
            throw e;
        }
    }

    @Override
    public List<Object[]> getTreatmentPlan(int patientId) {
        String action = "GET_TREATMENT_PLAN";
        String details = "Patient ID: " + patientId;
        try {
            List<Object[]> result = super.getTreatmentPlan(patientId);
            logAction(action, details, true);
            return result;
        } catch (Exception e) {
            logAction(action, details, false);
            throw e;
        }
    }

    @Override
    public void savePatient(Patient patient) throws ValidationException {
        String action = "REGISTER_PATIENT";
        String details = "New Patient NIC: " + patient.getNic();

        try {
            super.savePatient(patient);
            logAction(action, details, true);
        } catch (Exception e) {
            logAction(action, details, false);
            throw e;
        }
    }

    private void logAction(String action, String details, boolean success) {
        UserProfile profile = SessionManager.getInstance().getCurrentUserProfile();
        String username = (profile != null && profile.getUser() != null)
                ? profile.getUser().getUsername()
                : "UNKNOWN_USER";

        AuditLog log = new AuditLog(username, action, details, success, new Date());
        auditLogDAO.save(log);
    }
    
}
