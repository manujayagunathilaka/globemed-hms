/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.service;

import java.util.Optional;
import lk.jiat.oodp.globemed.hms.exception.ValidationException;
import lk.jiat.oodp.globemed.hms.model.dao.DoctorDAO;
import lk.jiat.oodp.globemed.hms.model.dao.StaffDAO;
import lk.jiat.oodp.globemed.hms.model.dao.UserDAO;
import lk.jiat.oodp.globemed.hms.model.entity.Doctor;
import lk.jiat.oodp.globemed.hms.model.entity.Staff;
import lk.jiat.oodp.globemed.hms.model.entity.User;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;
import lk.jiat.oodp.globemed.hms.util.PasswordUtil;

/**
 *
 * @author Manujaya
 */
public class UserRegistrationService {

    private final UserDAO userDAO;
    private final DoctorDAO doctorDAO;
    private final StaffDAO staffDAO;

    public UserRegistrationService() {
        this.userDAO = new UserDAO();
        this.doctorDAO = new DoctorDAO();
        this.staffDAO = new StaffDAO();
    }

    public void registerNewUser(User user, Object profileDetails) throws ValidationException {
        // 1. Username එක දැනටමත් ඇත්දැයි පරීක්ෂා කිරීම
        Optional<User> existingUser = userDAO.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new ValidationException("Username '" + user.getUsername() + "' already exists. Please choose another.");
        }

        // 2. Password එක hash කිරීම
        user.setPassword(PasswordUtil.encode(user.getPassword()));

        // --- 👇👇👇 මෙන්න නිවැරදි කරන ලද Logic එක 👇👇👇 ---
        // 3. පළමුව, `User` object එක පමණක් සෘජුවම save කිරීම.
        //    මෙමගින් User ට database ID එකක් ලැබේ.
        userDAO.save(user);

        // 4. `User` save වූ පසු, අදාළ profile(s) save කිරීම.
        //    දැන්, CascadeType.ALL මගින්, දැනටමත් පවතින User ව update කිරීමක් පමණක් සිදු වේ.
        if (user.getUserRole() == UserRole.DOCTOR) {
            Doctor doctorProfile = (Doctor) profileDetails;
            doctorProfile.setUser(user);
            doctorDAO.save(doctorProfile);

            Staff staffProfileForDoctor = new Staff(doctorProfile.getName(), user, doctorProfile.getDepartment());
            staffDAO.save(staffProfileForDoctor);

        } else {
            // ADMIN, NURSE, RECEPTIONIST සඳහා
            Staff staffProfile = (Staff) profileDetails;
            staffProfile.setUser(user);
            staffDAO.save(staffProfile);
        }
    }
}
