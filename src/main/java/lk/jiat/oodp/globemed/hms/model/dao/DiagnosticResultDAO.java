/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lk.jiat.oodp.globemed.hms.model.entity.DiagnosticResult;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class DiagnosticResultDAO {
    
    public void save(DiagnosticResult result) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(result);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<DiagnosticResult> findByPatientAndDateRange(Patient patient, Date fromDate, Date toDate) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            StringBuilder qlString = new StringBuilder("SELECT dr FROM DiagnosticResult dr WHERE dr.patient = :patient");

            if (fromDate != null) {
                qlString.append(" AND dr.reportDate >= :fromDate");
            }
            if (toDate != null) {
                qlString.append(" AND dr.reportDate <= :toDate");
            }
            qlString.append(" ORDER BY dr.reportDate DESC");

            TypedQuery<DiagnosticResult> query = em.createQuery(qlString.toString(), DiagnosticResult.class);
            query.setParameter("patient", patient);

            if (fromDate != null) {
                query.setParameter("fromDate", fromDate);
            }
            if (toDate != null) {
                query.setParameter("toDate", toDate);
            }

            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<DiagnosticResult> findAllForPatient(Patient patient) {
        return findByPatientAndDateRange(patient, null, null);
    }
    
}
