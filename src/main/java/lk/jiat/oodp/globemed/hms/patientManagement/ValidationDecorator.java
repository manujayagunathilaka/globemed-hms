/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.patientManagement;

import lk.jiat.oodp.globemed.hms.patientManagement.PatientRecordAccessor;
import lk.jiat.oodp.globemed.hms.exception.ValidationException;
import lk.jiat.oodp.globemed.hms.model.dao.PatientDAO;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;

/**
 *
 * @author Manujaya
 */
public class ValidationDecorator extends RecordAccessorDecorator {

    private final PatientDAO patientDAO;

    public ValidationDecorator(PatientRecordAccessor accessor) {
        super(accessor);
        this.patientDAO = new PatientDAO();
    }

    @Override
    public void savePatient(Patient patient) throws ValidationException {
        System.out.println("DECORATOR [Validation]: Checking if NIC already exists...");

        if (patientDAO.findByNic(patient.getNic()).isPresent()) {
            throw new ValidationException("A patient with NIC '" + patient.getNic() + "' already exists.");
        }

        System.out.println("DECORATOR [Validation]: Validation successful.");
        super.savePatient(patient);
    }
}
