/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.dao;

import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.entity.TreatmentPlan;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class TreatmentPlanDAO {
    
    public void save(TreatmentPlan plan) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(plan);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TreatmentPlan> findByPatient(Patient patient) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT tp FROM TreatmentPlan tp WHERE tp.patient = :patient ORDER BY tp.startDate DESC", TreatmentPlan.class)
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
    
    public List<TreatmentPlan> findByPatientId(int patientId) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createNamedQuery("TreatmentPlan.findByPatientId", TreatmentPlan.class)
                    .setParameter("patientId", patientId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
}
