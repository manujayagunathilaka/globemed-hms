/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.patientManagement;

import java.util.List;
import lk.jiat.oodp.globemed.hms.patientManagement.PatientRecordAccessor;
import lk.jiat.oodp.globemed.hms.exception.ValidationException;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;

/**
 *
 * @author Manujaya
 */
public abstract class RecordAccessorDecorator implements PatientRecordAccessor {

    protected PatientRecordAccessor wrappedAccessor;

    public RecordAccessorDecorator(PatientRecordAccessor accessor) {
        this.wrappedAccessor = accessor;
    }

    @Override
    public List<Object[]> getPersonalDetails(int patientId) {
        return wrappedAccessor.getPersonalDetails(patientId);
    }

    @Override
    public List<Object[]> getMedicalHistory(int patientId) {
        return wrappedAccessor.getMedicalHistory(patientId);
    }

    @Override
    public List<Object[]> getTreatmentPlan(int patientId) {
        return wrappedAccessor.getTreatmentPlan(patientId);
    }
    
    @Override
    public void savePatient(Patient patient) throws ValidationException {
        wrappedAccessor.savePatient(patient);
    }
    
}
