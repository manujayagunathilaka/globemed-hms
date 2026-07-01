/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.patientManagement;

import java.util.List;
import lk.jiat.oodp.globemed.hms.patientManagement.PatientRecordAccessor;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;
import lk.jiat.oodp.globemed.hms.exception.AccessDeniedException;
import lk.jiat.oodp.globemed.hms.exception.ValidationException;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.util.SessionManager;

/**
 *
 * @author Manujaya
 */
public class AccessControlDecorator extends RecordAccessorDecorator {

    private final UserRole currentUserRole;

    public AccessControlDecorator(PatientRecordAccessor accessor) {
        super(accessor);
//        this.currentUserRole = role;
//        System.out.println("Current User : " + role);

        if (SessionManager.getInstance().getCurrentUserProfile() != null) {
            this.currentUserRole = SessionManager.getInstance().getCurrentUserProfile().getUser().getUserRole();
        } else {
            this.currentUserRole = null; 
        }

    }

    
    
    @Override
    public List<Object[]> getPersonalDetails(int patientId) {
//        if (currentUserRole == UserRole.NURSE) {
//            throw new AccessDeniedException("Nurses do not have permission to view detailed patient records.");
//        }

        return super.getPersonalDetails(patientId);

    }

    @Override
    public List<Object[]> getMedicalHistory(int patientId) {
        if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.DOCTOR) {
            return super.getMedicalHistory(patientId);
        } else {
            throw new AccessDeniedException("You do not have permission to view Medical History.");
        }
    }

    @Override
    public List<Object[]> getTreatmentPlan(int patientId) {
        if (currentUserRole == UserRole.RECEPTIONIST) {
            throw new AccessDeniedException("You do not have permission to view the Treatment Plan.");
        }
        return super.getTreatmentPlan(patientId);
    }

    @Override
    public void savePatient(Patient patient) throws ValidationException {
        System.out.println("DECORATOR [Access Control]: Checking permission for saving patient...");

        if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.RECEPTIONIST) {
            System.out.println("DECORATOR [Access Control]: Permission GRANTED for role: " + currentUserRole);
            super.savePatient(patient);
        } else {
            System.out.println("DECORATOR [Access Control]: Permission DENIED for role: " + currentUserRole);
            throw new AccessDeniedException("You do not have permission to register new patients. Required roles: ADMIN, RECEPTIONIST.");
        }
    }
    
    
}
