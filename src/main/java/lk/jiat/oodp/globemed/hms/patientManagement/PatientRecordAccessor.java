/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.patientManagement;

import java.util.List;
import lk.jiat.oodp.globemed.hms.exception.ValidationException;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;

/**
 *
 * @author Manujaya
 */
public interface PatientRecordAccessor {

    List<Object[]> getPersonalDetails(int patientId);

    List<Object[]> getMedicalHistory(int patientId);

    List<Object[]> getTreatmentPlan(int patientId);
    
    void savePatient(Patient patient) throws ValidationException;

}
