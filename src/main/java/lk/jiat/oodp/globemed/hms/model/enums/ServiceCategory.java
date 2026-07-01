/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.model.enums;

/**
 *
 * @author Manujaya
 */
public enum ServiceCategory {
    CONSULTATION("Doctor Consultation"),
    LABORATORY_TEST("Laboratory Test"),
    MEDICATION("Medication / Pharmacy"),
    SURGERY("Surgical Procedure"),
    ROOM_CHARGE("Room Charge"),
    OTHER("Other Services");

    private final String displayName;

    ServiceCategory(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
