-- GlobeMed Healthcare Management System - Database Schema
-- Compatible with MySQL 8.0+

CREATE DATABASE IF NOT EXISTS `globemed-hms`;
USE `globemed-hms`;

-- 1. Department
CREATE TABLE IF NOT EXISTS `Department` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `parent_department_id` INT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_dept_name` (`name`),
  CONSTRAINT `FK_dept_parent` FOREIGN KEY (`parent_department_id`) REFERENCES `Department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Department Permissions
CREATE TABLE IF NOT EXISTS `department_permissions` (
  `department_id` INT NOT NULL,
  `permissions` ENUM(
    'ACCESS_APPOINTMENT_PANEL',
    'ACCESS_BILLING_PANEL',
    'ACCESS_DOCTOR_MANAGEMENT',
    'ACCESS_FINANCIAL_REPORTS',
    'ACCESS_PATIENT_MANAGEMENT_PANEL',
    'ACCESS_PERMISSIONS_PANEL',
    'CAN_ADD_MEDICAL_HISTORY',
    'CAN_ADD_TREATMENT_PLAN',
    'CAN_APPROVE_HIGH_VALUE_BILL',
    'CAN_PRESCRIBE_MEDICATION',
    'CAN_REGISTER_NEW_USER',
    'CAN_VIEW_MEDICAL_HISTORY',
    'CAN_VIEW_PATIENT_DETAILS',
    'CAN_VIEW_TREATMENT_PLAN'
  ) NOT NULL,
  KEY `FK_dept_perms` (`department_id`),
  CONSTRAINT `FK_dept_perms_ref` FOREIGN KEY (`department_id`) REFERENCES `Department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. System User
CREATE TABLE IF NOT EXISTS `system_user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `userRole` ENUM('ADMIN', 'DOCTOR', 'NURSE', 'PHARMACIST', 'RECEPTIONIST') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Staff
CREATE TABLE IF NOT EXISTS `Staff` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `department_id` INT DEFAULT NULL,
  `user_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_staff_user` (`user_id`),
  CONSTRAINT `FK_staff_dept` FOREIGN KEY (`department_id`) REFERENCES `Department` (`id`),
  CONSTRAINT `FK_staff_user_ref` FOREIGN KEY (`user_id`) REFERENCES `system_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Staff Permissions
CREATE TABLE IF NOT EXISTS `staff_permissions` (
  `staff_id` INT NOT NULL,
  `permissions` ENUM(
    'ACCESS_APPOINTMENT_PANEL',
    'ACCESS_BILLING_PANEL',
    'ACCESS_DOCTOR_MANAGEMENT',
    'ACCESS_FINANCIAL_REPORTS',
    'ACCESS_PATIENT_MANAGEMENT_PANEL',
    'ACCESS_PERMISSIONS_PANEL',
    'CAN_ADD_MEDICAL_HISTORY',
    'CAN_ADD_TREATMENT_PLAN',
    'CAN_APPROVE_HIGH_VALUE_BILL',
    'CAN_PRESCRIBE_MEDICATION',
    'CAN_REGISTER_NEW_USER',
    'CAN_VIEW_MEDICAL_HISTORY',
    'CAN_VIEW_PATIENT_DETAILS',
    'CAN_VIEW_TREATMENT_PLAN'
  ) NOT NULL,
  KEY `FK_staff_perms` (`staff_id`),
  CONSTRAINT `FK_staff_perms_ref` FOREIGN KEY (`staff_id`) REFERENCES `Staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Doctor
CREATE TABLE IF NOT EXISTS `Doctor` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) DEFAULT NULL,
  `specialization` VARCHAR(255) DEFAULT NULL,
  `qualification` VARCHAR(255) DEFAULT NULL,
  `registrationNumber` VARCHAR(255) DEFAULT NULL,
  `practicingHospital` VARCHAR(255) DEFAULT NULL,
  `experienceYears` INT NOT NULL,
  `fee` DOUBLE NOT NULL,
  `department_id` INT DEFAULT NULL,
  `user_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_doctor_user` (`user_id`),
  CONSTRAINT `FK_doctor_dept` FOREIGN KEY (`department_id`) REFERENCES `Department` (`id`),
  CONSTRAINT `FK_doctor_user_ref` FOREIGN KEY (`user_id`) REFERENCES `system_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Doctor Schedule
CREATE TABLE IF NOT EXISTS `DoctorSchedule` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `dayOfWeek` ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') DEFAULT NULL,
  `startTime` TIME(6) DEFAULT NULL,
  `endTime` TIME(6) DEFAULT NULL,
  `location` VARCHAR(255) NOT NULL,
  `doctor_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_schedule_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `Doctor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Patient
CREATE TABLE IF NOT EXISTS `Patient` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `patientUID` VARCHAR(255) NOT NULL,
  `nic` VARCHAR(255) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `address` VARCHAR(255) DEFAULT NULL,
  `mobileNumber` VARCHAR(255) DEFAULT NULL,
  `emergencyContactName` VARCHAR(255) DEFAULT NULL,
  `emergencyContactNumber` VARCHAR(255) DEFAULT NULL,
  `gender` ENUM('MALE', 'FEMALE') DEFAULT NULL,
  `bloodType` ENUM('A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE', 'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE', 'UNKNOWN') DEFAULT NULL,
  `dateOfBirth` DATE DEFAULT NULL,
  `registeredDate` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_patient_nic` (`nic`),
  UNIQUE KEY `UK_patient_uid` (`patientUID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Appointment
CREATE TABLE IF NOT EXISTS `Appointment` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `appointmentTime` DATETIME(6) DEFAULT NULL,
  `appointmentType` ENUM('CHECKUP', 'CONSULTATION', 'FOLLOW_UP', 'SURGERY') DEFAULT NULL,
  `location` VARCHAR(255) NOT NULL,
  `status` ENUM('BOOKED', 'CANCELED', 'COMPLETED') DEFAULT NULL,
  `doctor_id` INT NOT NULL,
  `patient_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_appointment_doctor` FOREIGN KEY (`doctor_id`) REFERENCES `Doctor` (`id`),
  CONSTRAINT `FK_appointment_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Billing Claim
CREATE TABLE IF NOT EXISTS `BillingClaim` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `amount` DOUBLE NOT NULL,
  `approvedAmount` DOUBLE DEFAULT NULL,
  `insuranceCovered` DOUBLE DEFAULT NULL,
  `patientResponsibility` DOUBLE DEFAULT NULL,
  `policyNumber` VARCHAR(255) DEFAULT NULL,
  `insuranceProvider` VARCHAR(255) DEFAULT NULL,
  `approvalCode` VARCHAR(255) DEFAULT NULL,
  `createdDate` DATETIME(6) DEFAULT NULL,
  `errorMessage` VARCHAR(255) DEFAULT NULL,
  `processingNotes` VARCHAR(500) DEFAULT NULL,
  `serviceType` VARCHAR(255) DEFAULT NULL,
  `billingType` ENUM('DIRECT', 'INSURANCE') DEFAULT NULL,
  `status` ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'REJECTED', 'FAILED') DEFAULT NULL,
  `appointment_id` INT DEFAULT NULL,
  `patient_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_billing_appointment` FOREIGN KEY (`appointment_id`) REFERENCES `Appointment` (`id`),
  CONSTRAINT `FK_billing_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Diagnostic Result
CREATE TABLE IF NOT EXISTS `DiagnosticResult` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `findings` LONGTEXT,
  `reportDate` DATE DEFAULT NULL,
  `reportType` VARCHAR(255) DEFAULT NULL,
  `orderedBy_id` INT DEFAULT NULL,
  `patient_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_result_doctor` FOREIGN KEY (`orderedBy_id`) REFERENCES `Doctor` (`id`),
  CONSTRAINT `FK_result_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Medical History
CREATE TABLE IF NOT EXISTS `MedicalHistory` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `conditionDetails` VARCHAR(255) NOT NULL,
  `recordedBy` VARCHAR(255) DEFAULT NULL,
  `recordedDate` DATE DEFAULT NULL,
  `patient_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_history_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. Treatment Plan
CREATE TABLE IF NOT EXISTS `TreatmentPlan` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `dosage` VARCHAR(255) DEFAULT NULL,
  `frequency` VARCHAR(255) DEFAULT NULL,
  `medicationName` VARCHAR(255) NOT NULL,
  `startDate` DATE DEFAULT NULL,
  `patient_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_treatment_patient` FOREIGN KEY (`patient_id`) REFERENCES `Patient` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. Audit Log
CREATE TABLE IF NOT EXISTS `AuditLog` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `action` VARCHAR(255) DEFAULT NULL,
  `details` VARCHAR(255) DEFAULT NULL,
  `isSuccess` BIT(1) NOT NULL,
  `timestamp` DATETIME(6) DEFAULT NULL,
  `username` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
