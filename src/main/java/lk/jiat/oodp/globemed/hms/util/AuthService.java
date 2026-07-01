/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.util;

import java.util.Optional;
import lk.jiat.oodp.globemed.hms.model.dao.UserDAO;
import lk.jiat.oodp.globemed.hms.model.entity.User;

/**
 *
 * @author Manujaya
 */
public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userDAO.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.matches(password, user.getPassword())) {
                return userOpt;
            }
        }

        return Optional.empty();
    }
}
