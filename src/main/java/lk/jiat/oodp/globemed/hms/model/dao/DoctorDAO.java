/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lk.jiat.oodp.globemed.hms.model.entity.Doctor;
import lk.jiat.oodp.globemed.hms.model.entity.User;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class DoctorDAO {

    public List<Doctor> findAll() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createNamedQuery("Doctor.findAll", Doctor.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Optional<Doctor> findById(int doctorId) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Doctor doc = em.find(Doctor.class, doctorId);
            return Optional.ofNullable(doc);
        } finally {
            em.close();
        }
    }
    
    public List<String> findDistinctSpecializations() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT d.specialization FROM Doctor d WHERE d.specialization IS NOT NULL ORDER BY d.specialization", String.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Doctor> searchByName(String searchText) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT d FROM Doctor d WHERE LOWER(d.name) LIKE LOWER(:text)", Doctor.class)
                    .setParameter("text", "%" + searchText + "%")
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }
    
    public List<Doctor> findBySpecialization(String specialization) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT d FROM Doctor d WHERE d.specialization = :spec ORDER BY d.name", Doctor.class)
                    .setParameter("spec", specialization)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Doctor> findDoctorsFiltered(String name, String specialization, String location) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {

            String qlString = "SELECT DISTINCT d FROM Doctor d JOIN DoctorSchedule ds ON d.id = ds.doctor.id WHERE "
                    + "d.name LIKE :name "
                    + "AND (:spec IS NULL OR d.specialization = :spec) "
                    + "AND (:loc IS NULL OR ds.location = :loc) " 
                    + "ORDER BY d.name";

            TypedQuery<Doctor> query = em.createQuery(qlString, Doctor.class);

            query.setParameter("name", "%" + name + "%");

            if (specialization == null || specialization.equals("All Specializations") || specialization.isEmpty()) {
                query.setParameter("spec", null);
            } else {
                query.setParameter("spec", specialization);
            }

            if (location == null || location.equals("All Locations") || location.isEmpty()) {
                query.setParameter("loc", null);
            } else {
                query.setParameter("loc", location);
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public Optional<Doctor> findByUser(User user) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Doctor doctor = em.createQuery("SELECT d FROM Doctor d WHERE d.user = :user", Doctor.class)
                    .setParameter("user", user)
                    .getSingleResult();
            return Optional.of(doctor);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
    
    public long getDoctorCount() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(d) FROM Doctor d", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
    
    public void save(Doctor doctor) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(doctor);
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
