/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.security;

import java.util.Base64;
import java.util.List;
import lk.jiat.oodp.globemed.hms.model.entity.AuditLog;
import lk.jiat.oodp.globemed.hms.model.entity.UserProfile;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;
import lk.jiat.oodp.globemed.hms.util.SessionManager;

/**
 *
 * @author Manujaya
 */
public class LogEncryptionDecorator implements AuditLogAccessor {

    private final AuditLogAccessor wrappedAccessor;
    private final UserRole currentUserRole;

    public LogEncryptionDecorator(AuditLogAccessor accessor) {
        this.wrappedAccessor = accessor;
        UserProfile profile = SessionManager.getInstance().getCurrentUserProfile();

        if (profile != null && profile.getUser() != null) {
            this.currentUserRole = profile.getUser().getUserRole();
        } else {
            this.currentUserRole = null;
            
            System.out.println("DEBUG [Decorator CONSTRUCTOR]: Decorator created. Current user role set to: " + this.currentUserRole);

        }
    }

    @Override
    public List<AuditLog> fetchAllLogs() {
        List<AuditLog> logs = this.wrappedAccessor.fetchAllLogs();

        System.out.println("DEBUG [Decorator fetchAllLogs]: Inside fetchAllLogs. Checking role. Role is: " + this.currentUserRole);

        if (currentUserRole != UserRole.ADMIN) {
            System.out.println("DEBUG [Decorator fetchAllLogs]: Role is NOT ADMIN. Entering encryption block...");
            for (AuditLog log : logs) {
                String originalDetails = log.getDetails();
                if (originalDetails != null) {
                    String encryptedDetails = Base64.getEncoder().encodeToString(originalDetails.getBytes());
                    log.setDetails("[ENCRYPTED] " + encryptedDetails);
                }
            }
        } else {
            System.out.println("DEBUG [Decorator fetchAllLogs]: Role IS ADMIN. Skipping encryption.");

        }
        return logs;
    }
    
}
