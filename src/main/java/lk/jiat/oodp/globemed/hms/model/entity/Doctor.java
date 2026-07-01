/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;

/**
 *
 * @author Manujaya
 */
@NamedQueries({
    @NamedQuery(name = "Doctor.findAll", query = "SELECT d FROM Doctor d ORDER BY d.name"),
    @NamedQuery(name = "Doctor.findById", query = "SELECT d FROM Doctor d WHERE d.id = :doctorId")
})
@Entity
public class Doctor implements UserProfile{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

//    private String name;
    
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;
    
    private String name;
    private String qualification;    
    private String specialization;
    private int experienceYears;
    private String practicingHospital;
    private String registrationNumber;
    private double fee;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    public Doctor() {
    }

    public Doctor(String name, String qualification, String specialization, int experienceYears, String practicingHospital, String registrationNumber, double fee) {
        this.name = name;
        this.qualification = qualification;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
        this.practicingHospital = practicingHospital;
        this.registrationNumber = registrationNumber;
        this.fee = fee;
    }

    public Doctor(String name, User user, String specialization) {
        this.name = name;
        this.user = user;
        this.specialization = specialization;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getQualification() {
        return qualification;
    }
    
    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
    
    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getPracticingHospital() {
        return practicingHospital;
    }

    public void setPracticingHospital(String practicingHospital) {
        this.practicingHospital = practicingHospital;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    @Override
    public User getUser() {
        return user;
    }
    
    @Override
    public String toString() {
        return "Dr. " + name + " (" + specialization + ")";
    }

    
    
}
