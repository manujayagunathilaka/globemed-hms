/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.dao;

import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import lk.jiat.oodp.globemed.hms.model.entity.MedicalHistory;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class MedicalHistoryDAO {
    
    public void save(MedicalHistory history) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(history);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MedicalHistory> findByPatient(Patient patient) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT mh FROM MedicalHistory mh WHERE mh.patient = :patient ORDER BY mh.recordedDate DESC", MedicalHistory.class)
                    .setParameter("patient", patient)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    public List<MedicalHistory> findByPatientId(int patientId) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createNamedQuery("MedicalHistory.findByPatientId", MedicalHistory.class)
                    .setParameter("patientId", patientId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
}
