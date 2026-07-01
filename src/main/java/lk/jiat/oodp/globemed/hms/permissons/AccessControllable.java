/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.permissons;

import lk.jiat.oodp.globemed.hms.model.enums.AppPermission;

/**
 *
 * @author Manujaya
 */
public interface AccessControllable {
    String getName();

    void grantPermission(AppPermission permission);

    void revokePermission(AppPermission permission);

    boolean hasPermission(AppPermission permission);
}
