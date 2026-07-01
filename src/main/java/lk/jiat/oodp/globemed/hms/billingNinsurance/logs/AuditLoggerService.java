package lk.jiat.oodp.globemed.hms.billingNinsurance.logs;

import java.util.Date;
import lk.jiat.oodp.globemed.hms.model.dao.AuditLogDAO;
import lk.jiat.oodp.globemed.hms.model.entity.AuditLog;
import lk.jiat.oodp.globemed.hms.model.entity.UserProfile;
import lk.jiat.oodp.globemed.hms.util.SessionManager;

/**
 * Service to log billing and other business process events to the audit log database.
 */
public class AuditLoggerService {
    private final AuditLogDAO auditLogDAO;

    public AuditLoggerService() {
        this.auditLogDAO = new AuditLogDAO();
    }

    public void log(String action, String details, boolean success) {
        UserProfile profile = SessionManager.getInstance().getCurrentUserProfile();
        String username = (profile != null && profile.getUser() != null)
                ? profile.getUser().getUsername()
                : "UNKNOWN_USER";

        AuditLog log = new AuditLog(username, action, details, success, new Date());
        auditLogDAO.save(log);
    }
}
