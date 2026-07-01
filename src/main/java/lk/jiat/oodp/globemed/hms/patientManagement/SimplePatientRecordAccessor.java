/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.patientManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lk.jiat.oodp.globemed.hms.patientManagement.PatientRecordAccessor;
import lk.jiat.oodp.globemed.hms.exception.ValidationException;
import lk.jiat.oodp.globemed.hms.model.dao.MedicalHistoryDAO;
import lk.jiat.oodp.globemed.hms.model.dao.PatientDAO;
import lk.jiat.oodp.globemed.hms.model.dao.TreatmentPlanDAO;
import lk.jiat.oodp.globemed.hms.model.entity.MedicalHistory;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.entity.TreatmentPlan;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;

/**
 *
 * @author Manujaya
 */
public class SimplePatientRecordAccessor implements PatientRecordAccessor {
    
    private final PatientDAO patientDAO;
    private final MedicalHistoryDAO medicalHistoryDAO;
    private final TreatmentPlanDAO treatmentPlanDAO;

    public SimplePatientRecordAccessor() {
        this.patientDAO = new PatientDAO();
        this.medicalHistoryDAO = new MedicalHistoryDAO();
        this.treatmentPlanDAO = new TreatmentPlanDAO();
    }

    @Override
    public List<Object[]> getPersonalDetails(int patientId) {
        Optional<Patient> patientOptional = patientDAO.findById(patientId);

        if (patientOptional.isPresent()) {
            
            Patient p = patientOptional.get();
            List<Object[]> details = new ArrayList<>();
            details.add(new Object[]{"Patient UID", p.getPatientUID()});
            details.add(new Object[]{"Name", p.getName()});
            details.add(new Object[]{"NIC", p.getNic()});
            details.add(new Object[]{"Date of Birth", p.getDateOfBirth()});
            details.add(new Object[]{"Gender", p.getGender()});
            details.add(new Object[]{"Blood Type", p.getBloodType()});
            details.add(new Object[]{"Mobile Number", p.getMobileNumber()});
            details.add(new Object[]{"Address", p.getAddress()});
            details.add(new Object[]{"Emergency Contact Name", p.getEmergencyContactName()});
            details.add(new Object[]{"Emergency Contact Number", p.getEmergencyContactNumber()});            
            details.add(new Object[]{"Reg Date", p.getRegisteredDate()});            
            
            return details;
        }
        return Collections.emptyList();
    }

    @Override
    public List<Object[]> getMedicalHistory(int patientId) {
        List<MedicalHistory> historyRecords = medicalHistoryDAO.findByPatientId(patientId);
        return historyRecords.stream()
                .map(history -> new Object[]{
            history.getRecordedDate(),
            history.getConditionDetails(),
            history.getRecordedBy()
        })
                .collect(Collectors.toList());
    }

    @Override
    public List<Object[]> getTreatmentPlan(int patientId) {
        List<TreatmentPlan> planRecords = treatmentPlanDAO.findByPatientId(patientId);
        return planRecords.stream()
                .map(plan -> new Object[]{
            plan.getStartDate(),
            plan.getMedicationName(),
            plan.getDosage(),
            plan.getFrequency()
        })
                .collect(Collectors.toList());
    }
    
    @Override
    public void savePatient(Patient patient) throws ValidationException {
        new PatientDAO().save(patient);
        System.out.println("CORE ACTION: Patient saved to database via PatientRecordAccessor.");
    }
    
}
