/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lk.jiat.oodp.globemed.hms.model.entity.Appointment;
import lk.jiat.oodp.globemed.hms.model.entity.Doctor;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.enums.AppointmentStatus;
import lk.jiat.oodp.globemed.hms.util.PersistenceManagement;

/**
 *
 * @author Manujaya
 */
public class AppointmentDAO {

    public boolean isSlotAvailable(Doctor doctor, LocalDateTime slot, String location) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Long count = em.createNamedQuery("Appointment.findConflict", Long.class)
                    .setParameter("doctor", doctor)
                    .setParameter("slot", slot)
                    .setParameter("location", location)
                    .setParameter("status", AppointmentStatus.BOOKED)
                    .getSingleResult();
            return count == 0;
        } finally {
            em.close();
        }
    }

    public void save(Appointment appointment) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(appointment);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<String> findDistinctSpecializations() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT d.specialization FROM Doctor d ORDER BY d.specialization", String.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Appointment> findByDoctorAndDate(int doctorId, LocalDate date) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            return em.createQuery("SELECT a FROM Appointment a WHERE a.doctor.id = :docId AND a.appointmentTime >= :start AND a.appointmentTime <= :end AND a.status = :status ORDER BY a.appointmentTime", Appointment.class)
                    .setParameter("docId", doctorId)
                    .setParameter("start", startOfDay)
                    .setParameter("end", endOfDay)
                    .setParameter("status", AppointmentStatus.BOOKED)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Appointment> findByPatientAndDateRange(Patient patient, Date fromDate, Date toDate) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            StringBuilder qlString = new StringBuilder("SELECT a FROM Appointment a WHERE a.patient = :patient");

            if (fromDate != null) {
                qlString.append(" AND a.appointmentTime >= :fromDate");
            }
            if (toDate != null) {
                qlString.append(" AND a.appointmentTime <= :toDate");
            }
            qlString.append(" ORDER BY a.appointmentTime DESC");

            TypedQuery<Appointment> query = em.createQuery(qlString.toString(), Appointment.class);
            query.setParameter("patient", patient);

            if (fromDate != null) {
                LocalDateTime start = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
                query.setParameter("fromDate", start);
            }
            if (toDate != null) {
                LocalDateTime end = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59);
                query.setParameter("toDate", end);
            }

            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    public List<Appointment> findAllFutureAppointmentsForDoctor(int doctorId, LocalDate startDate) {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            return em.createQuery("SELECT a FROM Appointment a WHERE a.doctor.id = :docId AND a.appointmentTime >= :start AND a.status = :status ORDER BY a.appointmentTime", Appointment.class)
                    .setParameter("docId", doctorId)
                    .setParameter("start", startDateTime)
                    .setParameter("status", AppointmentStatus.BOOKED)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Appointment> findAllForPatient(Patient patient) {
        return Collections.emptyList(); 
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
    
}
