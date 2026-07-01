/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.security;

import java.util.List;
import lk.jiat.oodp.globemed.hms.exception.AccessDeniedException;
import lk.jiat.oodp.globemed.hms.model.entity.AuditLog;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;
import lk.jiat.oodp.globemed.hms.util.SessionManager;

/**
 *
 * @author Manujaya
 */
public class LogViewerSecurityProxy implements AuditLogAccessor {

    private final AuditLogAccessor realAccessor;
    private final UserRole currentUserRole;

    public LogViewerSecurityProxy() {
        this.realAccessor = new RealAuditLogAccessor();
        if (SessionManager.getInstance().getCurrentUserProfile() != null) {
            this.currentUserRole = SessionManager.getInstance().getCurrentUserProfile().getUser().getUserRole();
        } else {
            this.currentUserRole = null; 
        }
    }

    @Override
    public List<AuditLog> fetchAllLogs() {
        System.out.println("PROXY: Checking if user is ADMIN...");
//        if (currentUserRole == UserRole.ADMIN) {
            System.out.println("PROXY: Access GRANTED.");
            return this.realAccessor.fetchAllLogs();
//        } else {
//            System.out.println("PROXY: Access DENIED! Only ADMINs can view audit logs.");
//            throw new AccessDeniedException("You do not have permission to view security logs.");
//        }
    }
    
//    @Override
//    public List<AuditLog> fetchAllLogs() {
//        System.out.println("PROXY: Checking if user is ADMIN...");
//        if (currentUserRole == UserRole.ADMIN) {
//            System.out.println("PROXY: Access GRANTED.");
//            return this.realAccessor.fetchAllLogs();
//        } else {
//            System.out.println("PROXY: Access DENIED! Only ADMINs can view audit logs.");
//            throw new AccessDeniedException("You do not have permission to view security logs.");
//        }
//    }
    
}
