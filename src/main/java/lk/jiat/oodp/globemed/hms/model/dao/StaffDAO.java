/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import lk.jiat.oodp.globemed.hms.model.entity.Department;
import lk.jiat.oodp.globemed.hms.model.entity.Staff;
import lk.jiat.oodp.globemed.hms.model.entity.User;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class StaffDAO {

    public Optional<Staff> findById(int staffId) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Staff staff = em.find(Staff.class, staffId);
            return Optional.ofNullable(staff);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public Optional<Staff> findByUser(User user) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Staff staff = em.createQuery("SELECT s FROM Staff s WHERE s.user = :user", Staff.class)
                    .setParameter("user", user)
                    .getSingleResult();
            return Optional.of(staff);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Staff> findByDepartment(Department department) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT s FROM Staff s WHERE s.department = :dept", Staff.class)
                    .setParameter("dept", department)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void save(Staff staff) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(staff); 
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
    
}
