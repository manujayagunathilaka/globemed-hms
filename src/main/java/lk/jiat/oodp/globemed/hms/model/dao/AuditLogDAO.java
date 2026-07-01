/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import lk.jiat.oodp.globemed.hms.model.entity.AuditLog;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class AuditLogDAO {

    public void save(AuditLog log) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(log);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<AuditLog> findAll() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC", AuditLog.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    public List<String> getDistinctActions() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT a.action FROM AuditLog a", String.class).getResultList();
        } finally {
            em.close();
        }
    }
    
}
