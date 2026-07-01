/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.dao;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lk.jiat.oodp.globemed.hms.model.entity.Department;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class DepartmentDAO {
    public List<Department> findRootDepartments() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT d FROM Department d WHERE d.parentDepartment IS NULL", Department.class)
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

    public Optional<Department> findById(int id) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Department department = em.find(Department.class, id);
            return Optional.ofNullable(department);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void save(Department department) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(department); 
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
    
    public List<Department> findAll() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            // ComboBox එකේ පෙන්වීමට, අකාරාදී පිළිවෙලට (alphabetical order) ලබාගැනීම හොඳයි
            return em.createQuery("SELECT d FROM Department d ORDER BY d.name", Department.class)
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
    
    public long getTodaysAppointmentCount() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
            return em.createQuery("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentTime BETWEEN :start AND :end", Long.class)
                    .setParameter("start", startOfDay)
                    .setParameter("end", endOfDay)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public long getDepartmentCount() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(dp) FROM Department dp", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }    
}
