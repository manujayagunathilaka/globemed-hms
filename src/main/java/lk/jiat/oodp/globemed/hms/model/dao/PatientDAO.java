/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class PatientDAO {

    public Optional<Patient> findById(int patientId) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Patient patient = em.createNamedQuery("Patient.findById", Patient.class)
                    .setParameter("patientId", patientId)
                    .getSingleResult();
            return Optional.of(patient);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    public List<Patient> findAll() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createNamedQuery("Patient.findAll", Patient.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace(); 
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }
    
    public List<Patient> searchByNameOrNic(String searchText) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Patient p WHERE p.name LIKE :text OR p.nic LIKE :text", Patient.class)
                    .setParameter("text", "%" + searchText + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
    public Optional<Patient> findByNic(String nic) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return Optional.of(em.createQuery("SELECT p FROM Patient p WHERE p.nic = :nic", Patient.class)
                    .setParameter("nic", nic)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    public List<Patient> findAndSortPatients(String searchText, String sortBy) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            StringBuilder qlString = new StringBuilder("SELECT p FROM Patient p");

            if (searchText != null && !searchText.trim().isEmpty()) {
                qlString.append(" WHERE p.name LIKE :text OR p.nic LIKE :text");
            }

            switch (sortBy) {
                case "Name (A-Z)":
                    qlString.append(" ORDER BY p.name ASC");
                    break;
                case "Name (Z-A)":
                    qlString.append(" ORDER BY p.name DESC");
                    break;
                case "Date (Newest First)":
                    qlString.append(" ORDER BY p.registeredDate DESC");
                    break;
                case "Date (Oldest First)":
                    qlString.append(" ORDER BY p.registeredDate ASC");
                    break;
                default:
                    qlString.append(" ORDER BY p.name ASC"); 
            }

            TypedQuery<Patient> query = em.createQuery(qlString.toString(), Patient.class);

            if (searchText != null && !searchText.trim().isEmpty()) {
                query.setParameter("text", "%" + searchText + "%");
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public void save(Patient patient) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(patient);
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
    
    public int findLastPatientId() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Integer lastId = em.createQuery("SELECT p.id FROM Patient p ORDER BY p.id DESC", Integer.class)
                    .setMaxResults(1)
                    .getSingleResult();
            return lastId;
        } catch (NoResultException e) {
            return 0;
        } finally {
            em.close();
        }
    }
    
    public long getPatientCount() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM Patient p", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
    
}
