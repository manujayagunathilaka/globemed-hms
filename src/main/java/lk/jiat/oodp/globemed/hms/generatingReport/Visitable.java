/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.generatingReport;

/**
 *
 * @author Manujaya
 */
public interface Visitable {

    void accept(ReportVisitor visitor);
    
}
