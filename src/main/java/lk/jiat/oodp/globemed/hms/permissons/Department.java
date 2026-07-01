/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.permissons;

import java.util.ArrayList;
import java.util.List;
import lk.jiat.oodp.globemed.hms.model.enums.AppPermission;

/**
 *
 * @author Manujaya
 */
public class Department implements AccessControllable {

    private String name;
    private List<AccessControllable> members = new ArrayList<>();

    public Department(String name) {
        this.name = name;
    }

    public void addMember(AccessControllable member) {
        members.add(member);
    }

    public void removeMember(AccessControllable member) {
        members.remove(member);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void grantPermission(AppPermission permission) {
        System.out.println("Granting '" + permission + "' to the entire [" + getName() + "] department...");

        for (AccessControllable member : members) {
            member.grantPermission(permission);

        }
    }

    @Override
    public void revokePermission(AppPermission permission) {
        for (AccessControllable member : members) {
            member.revokePermission(permission);
        }
    }

    @Override
    public boolean hasPermission(AppPermission permission) {

        return false;
    }
}
