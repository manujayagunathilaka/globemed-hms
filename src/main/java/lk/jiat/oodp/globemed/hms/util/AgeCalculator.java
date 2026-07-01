/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

/**
 *
 * @author Manujaya
 */
public class AgeCalculator {

    public static int calculateAge(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }

        LocalDate dob;
        
        if (dateOfBirth instanceof java.sql.Date) {
            dob = ((java.sql.Date) dateOfBirth).toLocalDate();
        } else {
            dob = dateOfBirth.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        LocalDate currentDate = LocalDate.now();
        return Period.between(dob, currentDate).getYears();
    }
}
