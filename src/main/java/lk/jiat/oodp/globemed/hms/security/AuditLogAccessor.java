/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.security;

import java.util.List;
import lk.jiat.oodp.globemed.hms.model.entity.AuditLog;

/**
 *
 * @author Manujaya
 */
public interface AuditLogAccessor {
    List<AuditLog> fetchAllLogs();
}
