/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.security;

import java.util.List;
import lk.jiat.oodp.globemed.hms.model.dao.AuditLogDAO;
import lk.jiat.oodp.globemed.hms.model.entity.AuditLog;

/**
 *
 * @author Manujaya
 */
public class RealAuditLogAccessor implements AuditLogAccessor {

    @Override
    public List<AuditLog> fetchAllLogs() {
        System.out.println("REAL OBJECT: Fetching all logs from DAO...");
        return new AuditLogDAO().findAll();
    }
    
}
