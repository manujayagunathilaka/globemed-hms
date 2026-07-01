/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.security;

import java.util.List;
import lk.jiat.oodp.globemed.hms.patientManagement.PatientRecordAccessor;
import lk.jiat.oodp.globemed.hms.patientManagement.RecordAccessorDecorator;
import lk.jiat.oodp.globemed.hms.exception.AccessDeniedException;
import lk.jiat.oodp.globemed.hms.exception.ValidationException;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.entity.User;
import lk.jiat.oodp.globemed.hms.model.enums.AppPermission;
import lk.jiat.oodp.globemed.hms.patientManagement.RecordAccessorDecorator;
import lk.jiat.oodp.globemed.hms.permissons.PermissionService;
import lk.jiat.oodp.globemed.hms.util.SessionManager;

/**
 *
 * @author Manujaya
 */
public class AccessControlProxy extends RecordAccessorDecorator {

    private final PermissionService permissionService;
    private final User currentUser;

    public AccessControlProxy(PatientRecordAccessor accessor) {
        super(accessor);
        this.permissionService = new PermissionService();
        this.currentUser = SessionManager.getInstance().getCurrentUserProfile() != null
                ? SessionManager.getInstance().getCurrentUserProfile().getUser()
                : null;
    }

    @Override
    public List<Object[]> getPersonalDetails(int patientId) {
        if (permissionService.checkPermission(currentUser, AppPermission.CAN_VIEW_PATIENT_DETAILS)) {
            return super.getPersonalDetails(patientId);
        } else {
            throw new AccessDeniedException("Access Denied by Proxy: You do not have permission to view patient records.");
        }
    }

    @Override
    public void savePatient(Patient patient) throws ValidationException {
        if (permissionService.checkPermission(currentUser, AppPermission.CAN_REGISTER_NEW_USER)) {
            super.savePatient(patient);
        } else {
            throw new AccessDeniedException("Access Denied by Proxy: You do not have permission to register new patients.");
        }
    }
}
