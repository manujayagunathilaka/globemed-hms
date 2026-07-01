/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.util;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lk.jiat.oodp.globemed.hms.model.entity.Appointment;
import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.entity.Department;
import lk.jiat.oodp.globemed.hms.model.entity.DiagnosticResult;
import lk.jiat.oodp.globemed.hms.model.entity.Doctor;
import lk.jiat.oodp.globemed.hms.model.entity.DoctorSchedule;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.entity.Staff;
import lk.jiat.oodp.globemed.hms.model.entity.User;
import lk.jiat.oodp.globemed.hms.model.enums.AppPermission;
import lk.jiat.oodp.globemed.hms.model.enums.AppointmentStatus;
import lk.jiat.oodp.globemed.hms.model.enums.AppointmentType;
import lk.jiat.oodp.globemed.hms.model.enums.BillingStatus;
import lk.jiat.oodp.globemed.hms.model.enums.BillingType;
import lk.jiat.oodp.globemed.hms.model.enums.DayOfWeek;
import lk.jiat.oodp.globemed.hms.model.enums.Gender;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;

/**
 *
 * @author Manujaya
 */
public class DatabaseSeeder {

    public static void seedMasterData() {
        EntityManager em = PersistenceManagement.getEntityManager();
        try {
            Long userCount = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
            if (userCount > 0) {
                System.out.println("Database already contains data. Seeding skipped.");
                return;
            }

            System.out.println("Database is empty. Seeding all master data...");
            em.getTransaction().begin();

            List<Department> departments = createDepartments(em);
            List<Staff> staffMembers = createUsersAndProfiles(em, departments);

            grantInitialPermissions(staffMembers, departments);

            List<Patient> patients = createPatients(em);

            createDoctorSchedules(em);

            createAppointmentsAndRelatedData(em);

            em.getTransaction().commit();
            System.out.println("Master data seeding completed successfully.");

        } catch (Exception e) {
            System.err.println("Failed to seed master data! Error: " + e.getMessage());
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    private static List<Department> createDepartments(EntityManager em) {
        Department clinicalDept = new Department("Clinical Services", null);
        Department adminDept = new Department("Administration", null);
        em.persist(clinicalDept);
        em.persist(adminDept);

        Department cardioDept = new Department("Cardiology", clinicalDept);
        Department neuroDept = new Department("Neurology", clinicalDept);
        em.persist(cardioDept);
        em.persist(neuroDept);

        return List.of(clinicalDept, adminDept, cardioDept, neuroDept);
    }

    private static List<Staff> createUsersAndProfiles(EntityManager em, List<Department> depts) {
        List<Staff> staffList = new ArrayList<>();

        // Departments from list
        Department adminDept = depts.get(1);
        Department cardioDept = depts.get(2);
        Department neuroDept = depts.get(3);

        // Admin User
        User adminUser = new User("admin", PasswordUtil.encode("1234"), UserRole.ADMIN);
        Staff adminStaff = new Staff("Nimal Perera", adminUser, adminDept);
        em.persist(adminStaff);
        staffList.add(adminStaff);

        // Doctor Users & Profiles
        // 1. Doctor Amal Fernando
        User docUser1 = new User("dramal", PasswordUtil.encode("1234"), UserRole.DOCTOR);

        Doctor doc1 = new Doctor(); 
        doc1.setUser(docUser1);      
        doc1.setName("Amal Fernando"); 
        doc1.setQualification("MBBS, MD");
        doc1.setSpecialization("Cardiologist");
        doc1.setExperienceYears(10);
        doc1.setRegistrationNumber("SLMC-1122");
        doc1.setFee(3000.0);
        em.persist(doc1);

        Staff docStaff1 = new Staff(doc1.getName(), docUser1, cardioDept);
        em.persist(docStaff1);
        staffList.add(docStaff1);

        // 2. Doctor Bimali Silva
        User docUser2 = new User("drbimali", PasswordUtil.encode("1234"), UserRole.DOCTOR);

        Doctor doc2 = new Doctor(); 
        doc2.setUser(docUser2);
        doc2.setName("Bimali Silva");
        doc2.setQualification("MBBS, FRCS");
        doc2.setSpecialization("Neurologist");
        doc2.setExperienceYears(8);
        doc2.setRegistrationNumber("SLMC-1133");
        doc2.setFee(3500.0);
        em.persist(doc2);

        Staff docStaff2 = new Staff(doc2.getName(), docUser2, neuroDept);
        em.persist(docStaff2);
        staffList.add(docStaff2);

        // --- Nurse User ---
        User nurseUser = new User("nursekamala", PasswordUtil.encode("1234"), UserRole.NURSE);
        Staff nurseStaff = new Staff("Kamala Gunasekara", nurseUser, cardioDept);
        em.persist(nurseStaff);
        staffList.add(nurseStaff);

        User receptionistUser = new User("receptionist", PasswordUtil.encode("1234"), UserRole.RECEPTIONIST);
        Staff receptionistStaff = new Staff("Sunitha Gamage", receptionistUser, adminDept);
        em.persist(receptionistStaff);
        staffList.add(receptionistStaff);
        
        return staffList;
    }

    private static void grantInitialPermissions(List<Staff> staff, List<Department> depts) {
        Staff admin = staff.stream().filter(s -> s.getUser().getUserRole() == UserRole.ADMIN).findFirst().get();
        for (AppPermission p : AppPermission.values()) {
            admin.getPermissions().add(p);
        }

        Staff nurse = staff.stream().filter(s -> s.getUser().getUserRole() == UserRole.NURSE).findFirst().get();
        nurse.getPermissions().add(AppPermission.ACCESS_APPOINTMENT_PANEL);
        nurse.getPermissions().add(AppPermission.ACCESS_BILLING_PANEL);

        Department cardio = depts.stream().filter(d -> d.getName().equals("Cardiology")).findFirst().get();
        cardio.getPermissions().add(AppPermission.CAN_VIEW_MEDICAL_HISTORY);

    }

    private static List<Patient> createPatients(EntityManager em) {
        List<Patient> patientList = new ArrayList<>();
        patientList.add(new Patient("P_001", "Sunil Shantha", "851234567V", java.sql.Date.valueOf("1985-05-03"), Gender.MALE, "0771112222", "Colombo"));
        patientList.add(new Patient("P_002", "Anula Kumari", "917894561V", java.sql.Date.valueOf("1991-11-15"), Gender.FEMALE, "0713334444", "Kandy"));
        patientList.forEach(em::persist);
        return patientList;
    }

    private static void createDoctorSchedules(EntityManager em) {
        Doctor doc1 = em.find(Doctor.class, 1); // Amal Fernando
        Doctor doc2 = em.find(Doctor.class, 2); // Bimali Silva

        if (doc1 != null) {
            em.persist(new DoctorSchedule(doc1, DayOfWeek.MONDAY, LocalTime.of(15, 0), LocalTime.of(19, 0), "GlobeMed Colombo"));
            em.persist(new DoctorSchedule(doc1, DayOfWeek.WEDNESDAY, LocalTime.of(15, 0), LocalTime.of(19, 0), "GlobeMed Colombo"));
        }
        if (doc2 != null) {
            em.persist(new DoctorSchedule(doc2, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(13, 0), "GlobeMed Kandy"));
        }
    }

    private static void createAppointmentsAndRelatedData(EntityManager em) {
        Patient p1 = em.find(Patient.class, 1); 
        Doctor d1 = em.find(Doctor.class, 1); 

        if (p1 != null && d1 != null) {
            Appointment app = new Appointment();
            app.setDoctor(d1);
            app.setPatient(p1);
            app.setAppointmentTime(LocalDateTime.now().minusDays(5)); 
            app.setLocation("GlobeMed Colombo");
            app.setAppointmentType(AppointmentType.CONSULTATION);
            app.setStatus(AppointmentStatus.COMPLETED);
            em.persist(app);

            BillingClaim claim = new BillingClaim();
            claim.setPatient(p1);
            claim.setAppointment(app);
            claim.setAmount(d1.getFee());
            claim.setBillingType(BillingType.DIRECT);
            claim.setStatus(BillingStatus.COMPLETED);
            claim.getResult().setPatientResponsibility(d1.getFee());
            em.persist(claim);

            DiagnosticResult result = new DiagnosticResult();
            result.setPatient(p1);
            result.setOrderedBy(d1);
            result.setReportType("ECG Report");
            result.setFindings("Normal sinus rhythm. No abnormalities detected.");
            result.setReportDate(new Date());
            em.persist(result);
        }
    }
}
