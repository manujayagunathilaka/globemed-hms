-- GlobeMed Healthcare Management System - Initial Master Data Seed
-- Passwords are encoded in Base64 (matching lk.jiat.oodp.globemed.hms.util.PasswordUtil)
-- The password for all default accounts is "1234" (Base64: MTIzNA==)

USE `globemed-hms`;

-- 1. Departments
INSERT INTO `Department` (`id`, `name`, `parent_department_id`) VALUES
(1, 'Clinical Services', NULL),
(2, 'Administration', NULL),
(3, 'Cardiology', 1),
(4, 'Neurology', 1);

-- 2. System Users
INSERT INTO `system_user` (`id`, `username`, `password`, `userRole`) VALUES
(1, 'admin', 'MTIzNA==', 'ADMIN'),
(2, 'dramal', 'MTIzNA==', 'DOCTOR'),
(3, 'drbimali', 'MTIzNA==', 'DOCTOR'),
(4, 'nursekamala', 'MTIzNA==', 'NURSE'),
(5, 'receptionist', 'MTIzNA==', 'RECEPTIONIST');

-- 3. Staff Members
INSERT INTO `Staff` (`id`, `name`, `department_id`, `user_id`) VALUES
(1, 'Nimal Perera', 2, 1),
(2, 'Amal Fernando', 3, 2),
(3, 'Bimali Silva', 4, 3),
(4, 'Kamala Gunasekara', 3, 4),
(5, 'Sunitha Gamage', 2, 5);

-- 4. Doctors
INSERT INTO `Doctor` (`id`, `name`, `specialization`, `qualification`, `registrationNumber`, `practicingHospital`, `experienceYears`, `fee`, `department_id`, `user_id`) VALUES
(1, 'Amal Fernando', 'Cardiologist', 'MBBS, MD', 'SLMC-1122', 'General Hospital Colombo', 10, 3000.0, 3, 2),
(2, 'Bimali Silva', 'Neurologist', 'MBBS, FRCS', 'SLMC-1133', 'National Hospital Sri Lanka', 8, 3500.0, 4, 3);

-- 5. Doctor Schedules
INSERT INTO `DoctorSchedule` (`id`, `dayOfWeek`, `startTime`, `endTime`, `location`, `doctor_id`) VALUES
(1, 'MONDAY', '09:00:00', '13:00:00', 'Room 201 - Cardiology Clinic', 1),
(2, 'WEDNESDAY', '14:00:00', '18:00:00', 'Room 201 - Cardiology Clinic', 1),
(3, 'TUESDAY', '10:00:00', '14:00:00', 'Room 305 - Neurology Clinic', 2);

-- 6. Patients
INSERT INTO `Patient` (`id`, `patientUID`, `nic`, `name`, `address`, `mobileNumber`, `emergencyContactName`, `emergencyContactNumber`, `gender`, `bloodType`, `dateOfBirth`, `registeredDate`) VALUES
(1, 'PT-1001', '199512345678', 'John Doe', 'No. 45, Flower Road, Colombo 07', '0771234567', 'Jane Doe', '0777654321', 'MALE', 'A_POSITIVE', '1995-05-15', NOW()),
(2, 'PT-1002', '198898765432', 'Mary Watson', 'No. 12, Lake Drive, Rajagiriya', '0719876543', 'Sherlock Watson', '0711122334', 'FEMALE', 'O_NEGATIVE', '1988-11-20', NOW());

-- 7. Appointments
INSERT INTO `Appointment` (`id`, `appointmentTime`, `appointmentType`, `location`, `status`, `doctor_id`, `patient_id`) VALUES
(1, DATE_ADD(NOW(), INTERVAL 1 DAY), 'CONSULTATION', 'Room 201 - Cardiology Clinic', 'BOOKED', 1, 1);

-- 8. Department Permissions
INSERT INTO `department_permissions` (`department_id`, `permissions`) VALUES
(2, 'ACCESS_PERMISSIONS_PANEL'),
(2, 'CAN_REGISTER_NEW_USER'),
(2, 'ACCESS_PATIENT_MANAGEMENT_PANEL'),
(3, 'CAN_VIEW_PATIENT_DETAILS'),
(3, 'CAN_VIEW_MEDICAL_HISTORY'),
(3, 'CAN_ADD_MEDICAL_HISTORY'),
(4, 'CAN_VIEW_PATIENT_DETAILS'),
(4, 'CAN_VIEW_MEDICAL_HISTORY'),
(4, 'CAN_ADD_MEDICAL_HISTORY');

-- 9. Staff Permissions
INSERT INTO `staff_permissions` (`staff_id`, `permissions`) VALUES
(1, 'ACCESS_PERMISSIONS_PANEL'),
(1, 'CAN_REGISTER_NEW_USER'),
(1, 'ACCESS_PATIENT_MANAGEMENT_PANEL'),
(1, 'ACCESS_DOCTOR_MANAGEMENT'),
(1, 'ACCESS_BILLING_PANEL'),
(1, 'ACCESS_APPOINTMENT_PANEL'),
(1, 'ACCESS_FINANCIAL_REPORTS'),
(2, 'CAN_VIEW_PATIENT_DETAILS'),
(2, 'CAN_VIEW_MEDICAL_HISTORY'),
(2, 'CAN_ADD_MEDICAL_HISTORY'),
(2, 'CAN_VIEW_TREATMENT_PLAN'),
(2, 'CAN_ADD_TREATMENT_PLAN'),
(2, 'CAN_PRESCRIBE_MEDICATION'),
(3, 'CAN_VIEW_PATIENT_DETAILS'),
(3, 'CAN_VIEW_MEDICAL_HISTORY'),
(3, 'CAN_ADD_MEDICAL_HISTORY'),
(3, 'CAN_VIEW_TREATMENT_PLAN'),
(3, 'CAN_ADD_TREATMENT_PLAN'),
(3, 'CAN_PRESCRIBE_MEDICATION'),
(4, 'CAN_VIEW_PATIENT_DETAILS'),
(4, 'CAN_VIEW_MEDICAL_HISTORY'),
(4, 'CAN_ADD_MEDICAL_HISTORY'),
(4, 'CAN_VIEW_TREATMENT_PLAN'),
(5, 'ACCESS_APPOINTMENT_PANEL'),
(5, 'ACCESS_PATIENT_MANAGEMENT_PANEL'),
(5, 'CAN_VIEW_PATIENT_DETAILS'),
(5, 'ACCESS_BILLING_PANEL');
