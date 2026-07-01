/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 *
 * @author Manujaya
 */
public class UIMediator {

    private JPanel mainPanel;

    public void registerMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public void showPanel(JPanel jPanel){
        if (mainPanel != null) {
            mainPanel.removeAll();
            mainPanel.add(jPanel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }
    
}
