/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.permissons;

import java.util.HashSet;
import java.util.Set;
import lk.jiat.oodp.globemed.hms.model.enums.AppPermission;

/**
 *
 * @author Manujaya
 */
public class Employee implements AccessControllable {

    private String name;
    private String role;
    private Set<AppPermission> permissions = new HashSet<>();

    public Employee(String name, String role) {
        this.name = name;
        this.role = role;
    }

    @Override
    public String getName() {
        return this.name + " (" + this.role + ")";
    }

    @Override
    public void grantPermission(AppPermission permission) {
        System.out.println("  Granting '" + permission + "' to " + getName());
        this.permissions.add(permission);
    }

    @Override
    public void revokePermission(AppPermission permission) {
        this.permissions.remove(permission);
    }

    @Override
    public boolean hasPermission(AppPermission permission) {
        return this.permissions.contains(permission);
    }
}
