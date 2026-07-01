/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui.doctor;

import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;
import lk.jiat.oodp.globemed.hms.model.entity.DoctorSchedule;
import lk.jiat.oodp.globemed.hms.model.enums.DayOfWeek;

/**
 *
 * @author Manujaya
 */
public class AddScheduleDialog extends javax.swing.JDialog {

    private DoctorSchedule newSchedule = null;

    /**
     * Creates new form AddScheduleDialog
     */
    public AddScheduleDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setupDialog(); // අපගේ custom setup method එක
    }

    private void setupDialog() {
        setTitle("Add New Schedule");

        // --- ComboBoxes සහ TimePickers සකස් කිරීම ---
        // Location ComboBox
        cmbLocation.setModel(new DefaultComboBoxModel<>(new String[]{"GlobeMed Colombo", "GlobeMed Kandy", "GlobeMed Galle"}));

        // Day of Week ComboBox
        cmbDay.setModel(new DefaultComboBoxModel<>(DayOfWeek.values()));

        // timePickerStart සඳහා settings සකස් කිරීම
        // 1. TimePicker එකේ දැනටමත් ඇති settings object එක ලබාගන්නවා.
        TimePickerSettings startTimeSettings = timePickerStart.getSettings();
        // 2. එම object එකේ අගයන් (properties) වෙනස් කරනවා.
        startTimeSettings.initialTime = LocalTime.of(9, 0);
        startTimeSettings.setFormatForDisplayTime("HH:mm");
        startTimeSettings.setFormatForMenuTimes("HH:mm");

        // timePickerEnd සඳහා settings සකස් කිරීම
        TimePickerSettings endTimeSettings = timePickerEnd.getSettings();
        endTimeSettings.initialTime = LocalTime.of(10, 0);
        endTimeSettings.setFormatForDisplayTime("HH:mm");
        endTimeSettings.setFormatForMenuTimes("HH:mm");

        // --- Action Listeners ---
        btnSave.addActionListener(e -> {
            // UI එකෙන් දත්ත ලබාගැනීම
            LocalTime startTime = timePickerStart.getTime();
            LocalTime endTime = timePickerEnd.getTime();

            // Validation: Start time එක, end time එකට වඩා පසුව විය නොහැක
            if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
                JOptionPane.showMessageDialog(this, "Please select a valid start and end time. End time must be after start time.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // නව DoctorSchedule object එකක් නිර්මාණය කර, දත්ත පිරවීම
            this.newSchedule = new DoctorSchedule();
            newSchedule.setLocation(cmbLocation.getSelectedItem().toString());
            newSchedule.setDayOfWeek((DayOfWeek) cmbDay.getSelectedItem());
            newSchedule.setStartTime(startTime);
            newSchedule.setEndTime(endTime);

            // Save කළ පසු Dialog එක වසා දැමීම
            this.dispose();
        });

        btnCancel.addActionListener(e -> {
            this.newSchedule = null; // Cancel කළ නිසා, කිසිවක් return නොකිරීමට
            this.dispose(); // Dialog එක වසා දැමීම
        });

        this.pack(); // Components වලට ගැලපෙන සේ ප්‍රමාණය සකස් කිරීම
        this.setLocationRelativeTo((Frame) this.getParent()); // Parent එක මැද පෙන්වීමට
    }

    public Optional<DoctorSchedule> getNewSchedule() {
        return Optional.ofNullable(newSchedule);
    }
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        timePicker1 = new com.github.lgooddatepicker.components.TimePicker();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        cmbLocation = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        cmbDay = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        timePickerStart = new com.github.lgooddatepicker.components.TimePicker();
        jLabel5 = new javax.swing.JLabel();
        timePickerEnd = new com.github.lgooddatepicker.components.TimePicker();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Schedule");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel2.setLayout(new java.awt.GridLayout(5, 2, 10, 10));

        jLabel2.setText("Location :");
        jPanel2.add(jLabel2);

        jPanel2.add(cmbLocation);

        jLabel3.setText("Day of Week :");
        jPanel2.add(jLabel3);

        jPanel2.add(cmbDay);

        jLabel4.setText("Start Time :");
        jPanel2.add(jLabel4);
        jPanel2.add(timePickerStart);

        jLabel5.setText("End Time :");
        jPanel2.add(jLabel5);
        jPanel2.add(timePickerEnd);

        btnCancel.setText("Cancel");
        jPanel2.add(btnCancel);

        btnSave.setText("Save Schedule");
        jPanel2.add(btnSave);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<DayOfWeek> cmbDay;
    private javax.swing.JComboBox<String> cmbLocation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private com.github.lgooddatepicker.components.TimePicker timePicker1;
    private com.github.lgooddatepicker.components.TimePicker timePickerEnd;
    private com.github.lgooddatepicker.components.TimePicker timePickerStart;
    // End of variables declaration//GEN-END:variables
}
