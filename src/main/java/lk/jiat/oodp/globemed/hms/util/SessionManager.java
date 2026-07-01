/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.util;

import lk.jiat.oodp.globemed.hms.model.dao.DoctorDAO;
import lk.jiat.oodp.globemed.hms.model.dao.StaffDAO;
import lk.jiat.oodp.globemed.hms.model.entity.User;
import lk.jiat.oodp.globemed.hms.model.entity.UserProfile;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;

/**
 *
 * @author Manujaya
 */
public class SessionManager {

    private static SessionManager instance;
    private UserProfile currentUserProfile; 

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public boolean login(User user) {
        if (user.getUserRole() == UserRole.DOCTOR) {
            this.currentUserProfile = new DoctorDAO().findByUser(user).orElse(null);
        } else {
            this.currentUserProfile = new StaffDAO().findByUser(user).orElse(null);
        }
        return this.currentUserProfile != null;
    }

    public void logout() {
        this.currentUserProfile = null;
    }

    public UserProfile getCurrentUserProfile() {
        return currentUserProfile;
    }
}
