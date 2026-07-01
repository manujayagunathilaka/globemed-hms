/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.permissons;

import java.util.Optional;
import java.util.Set;
import lk.jiat.oodp.globemed.hms.model.dao.DepartmentDAO;
import lk.jiat.oodp.globemed.hms.model.dao.StaffDAO;
import lk.jiat.oodp.globemed.hms.model.entity.Department; 
import lk.jiat.oodp.globemed.hms.model.entity.Staff;
import lk.jiat.oodp.globemed.hms.model.entity.User;
import lk.jiat.oodp.globemed.hms.model.enums.AppPermission;

/**
 *
 * @author Manujaya
 */
public class PermissionService {

    private final StaffDAO staffDAO;
    private final DepartmentDAO departmentDAO;

    public PermissionService() {
        this.staffDAO = new StaffDAO();
        this.departmentDAO = new DepartmentDAO();
    }

    public boolean checkPermission(User user, AppPermission permission) {
        if (user == null) {
            return false;
        }

        Optional<Staff> staffOpt = staffDAO.findByUser(user);
        if (staffOpt.isEmpty()) {
            return false;
        }

        Staff staff = staffOpt.get();

        if (staff.getPermissions() != null && staff.getPermissions().contains(permission)) {
            return true;
        }

        Department currentDepartment = staff.getDepartment();
        while (currentDepartment != null) {
            if (currentDepartment.getPermissions() != null && currentDepartment.getPermissions().contains(permission)) {
                return true; 
            }
            currentDepartment = currentDepartment.getParentDepartment();
        }

        return false; 
    }

    public void savePermissionsForObject(Object object, Set<AppPermission> permissions) {
        if (object instanceof Department) {
            Department dept = (Department) object;
            departmentDAO.findById(dept.getId()).ifPresent(managedDept -> {
                managedDept.setPermissions(permissions);
                departmentDAO.save(managedDept);
            });
        } else if (object instanceof Staff) {
            Staff staff = (Staff) object;
            staffDAO.findById(staff.getId()).ifPresent(managedStaff -> {
                managedStaff.setPermissions(permissions);
                staffDAO.save(managedStaff);
            });
        }
    }
}
