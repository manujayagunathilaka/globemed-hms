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
import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.enums.BillingStatus;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class BillingClaimDAO {

    public void saveOrUpdate(BillingClaim claim) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(claim);
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
    
    public List<BillingClaim> findAllForPatient(Patient patient) {
        return Collections.emptyList(); 
    }
   
    public List<BillingClaim> findByDateRange(Date startDate, Date endDate) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT bc FROM BillingClaim bc WHERE bc.createdDate BETWEEN :start AND :end", BillingClaim.class)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
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
    
    public List<BillingClaim> findByPatientAndDateRange(Patient patient, Date fromDate, Date toDate) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            StringBuilder qlString = new StringBuilder("SELECT bc FROM BillingClaim bc WHERE bc.patient = :patient");

            if (fromDate != null) {
                qlString.append(" AND bc.createdDate >= :fromDate");
            }
            if (toDate != null) {
                qlString.append(" AND bc.createdDate <= :toDate");
            }
            qlString.append(" ORDER BY bc.createdDate DESC");

            TypedQuery<BillingClaim> query = em.createQuery(qlString.toString(), BillingClaim.class);
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
    
    public Double getTotalIncome() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Double total = em.createQuery("SELECT SUM(bc.amount) FROM BillingClaim bc WHERE bc.status = :status", Double.class)
                    .setParameter("status", BillingStatus.COMPLETED)
                    .getSingleResult();
            return total != null ? total : 0.0;
        } finally {
            em.close();
        }
    }
    
}
