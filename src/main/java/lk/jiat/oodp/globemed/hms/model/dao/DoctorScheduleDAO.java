/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.dao;

import jakarta.persistence.EntityManager;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import lk.jiat.oodp.globemed.hms.model.entity.Doctor;
import lk.jiat.oodp.globemed.hms.model.entity.DoctorSchedule;
import lk.jiat.oodp.globemed.hms.model.enums.DayOfWeek;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class DoctorScheduleDAO {
    
    public List<DoctorSchedule> findByDoctorAndDay(int doctorId, DayOfWeek day) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor.id = :docId AND ds.dayOfWeek = :day", DoctorSchedule.class)
                    .setParameter("docId", doctorId)
                    .setParameter("day", day)
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
    public void save(DoctorSchedule schedule) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(schedule);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    public List<DoctorSchedule> findByDoctor(Doctor doctor) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT ds FROM DoctorSchedule ds WHERE ds.doctor = :doctor ORDER BY ds.dayOfWeek", DoctorSchedule.class)
                    .setParameter("doctor", doctor)
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
    
    public boolean hasTimeOverlap(Doctor doctor, DayOfWeek day, LocalTime newStartTime, LocalTime newEndTime) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Long matchingSchedules = em.createQuery(
                    "SELECT COUNT(ds) FROM DoctorSchedule ds WHERE ds.doctor = :doctor AND ds.dayOfWeek = :day "
                    + "AND ds.startTime < :newEndTime AND ds.endTime > :newStartTime", Long.class)
                    .setParameter("doctor", doctor)
                    .setParameter("day", day)
                    .setParameter("newStartTime", newStartTime)
                    .setParameter("newEndTime", newEndTime)
                    .getSingleResult();

            return matchingSchedules > 0; 
        } finally {
            em.close();
        }
    }
    
    public void deleteById(int scheduleId) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            DoctorSchedule schedule = em.find(DoctorSchedule.class, scheduleId);
            if (schedule != null) {
                em.remove(schedule);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
}
