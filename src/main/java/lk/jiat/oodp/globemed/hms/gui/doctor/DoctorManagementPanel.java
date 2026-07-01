/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui.doctor;

import java.awt.Component;
import java.awt.Frame;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import lk.jiat.oodp.globemed.hms.model.dao.DoctorDAO;
import lk.jiat.oodp.globemed.hms.model.dao.DoctorScheduleDAO;
import lk.jiat.oodp.globemed.hms.model.entity.Doctor;
import lk.jiat.oodp.globemed.hms.model.entity.DoctorSchedule;

/**
 *
 * @author Manujaya
 */
public class DoctorManagementPanel extends javax.swing.JPanel {

    private final DoctorDAO doctorDAO;
    private final DoctorScheduleDAO scheduleDAO;
    private final DefaultListModel<Doctor> doctorListModel;
    private List<Doctor> allDoctors;
    
    public DoctorManagementPanel() {
        initComponents();
        this.doctorDAO = new DoctorDAO();
        this.scheduleDAO = new DoctorScheduleDAO();
        this.doctorListModel = new DefaultListModel<>();
        this.doctorList.setModel(doctorListModel);
        setupPanel();
    }

    private void setupPanel() {
        doctorList.setCellRenderer(new DoctorListRenderer());

        loadInitialData();

        setupListeners();

        toggleEditMode(false);
    }

    private void loadInitialData() {
        allDoctors = doctorDAO.findAll();

        doctorListModel.clear();
        allDoctors.forEach(doctorListModel::addElement);
        
        List<String> specializations = doctorDAO.findDistinctSpecializations();
        DefaultComboBoxModel<String> specModel = new DefaultComboBoxModel<>();
        specModel.addElement("All Specializations");
        specializations.forEach(specModel::addElement);
        cmbFilterSpecialization.setModel(specModel);
        
        filterDoctorList();
    }

    private void setupListeners() {
        doctorList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Doctor selectedDoctor = doctorList.getSelectedValue();
                if (selectedDoctor != null) {
                    populateDetailFields(selectedDoctor);

                    populateScheduleTable(selectedDoctor);
                } else {
                    clearDetailFields();
                }
            }
        });

        DocumentListener filterListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterDoctorList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterDoctorList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterDoctorList();
            }
        };
        txtSearchDoctor.getDocument().addDocumentListener(filterListener);
        cmbFilterSpecialization.addActionListener(e -> filterDoctorList());

        // Action Buttons
        btnEditDetails.addActionListener(e -> toggleEditMode(true));
        btnSaveChanges.addActionListener(e -> saveDoctorDetails());
        btnAddSchedule.addActionListener(e -> addNewSchedule());
        btnRemoveSchedule.addActionListener(e -> removeSelectedSchedule()); 

    }

    private void filterDoctorList() {
        String searchText = txtSearchDoctor.getText().toLowerCase();
        Object selectedSpecItem = cmbFilterSpecialization.getSelectedItem();
        String specFilter = (selectedSpecItem != null) ? selectedSpecItem.toString() : "All Specializations";

        doctorListModel.clear();

        for (Doctor doc : allDoctors) {

            boolean nameMatch = doc.getName().toLowerCase().contains(searchText)
                    || doc.getSpecialization().toLowerCase().contains(searchText);

            boolean specMatch = "All Specializations".equals(specFilter)
                    || doc.getSpecialization().equals(specFilter);

            if (nameMatch && specMatch) {
                doctorListModel.addElement(doc);
            }
        }
    }

    private void populateDetailFields(Doctor doctor) {
        txtNameDetail.setText(doctor.getName());
        txtSpecializationDetail.setText(doctor.getSpecialization());
        txtQualificationDetail.setText(doctor.getQualification());
        txtExperienceDetail.setText(String.valueOf(doctor.getExperienceYears()) + " Years");
        txtHospitalDetail.setText(doctor.getPracticingHospital());
        txtRegNoDetail.setText(doctor.getRegistrationNumber());
        txtFeeDetail.setText(String.format("LKR %.2f", doctor.getFee()));
    }

    private void clearDetailFields() {
        txtNameDetail.setText("");
        txtSpecializationDetail.setText("");
        txtQualificationDetail.setText("");
        txtExperienceDetail.setText("");
        txtHospitalDetail.setText("");
        txtRegNoDetail.setText("");
        txtFeeDetail.setText("");
        
        ((DefaultTableModel) tblDoctorSchedule.getModel()).setRowCount(0);
    }

    private void populateScheduleTable(Doctor doctor) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Location", "Day", "Start Time", "End Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<DoctorSchedule> schedules = scheduleDAO.findByDoctor(doctor);
        for (DoctorSchedule schedule : schedules) {
            model.addRow(new Object[]{
                schedule.getId(), 
                schedule.getLocation(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime()
            });
        }
        tblDoctorSchedule.setModel(model);

        tblDoctorSchedule.getColumnModel().getColumn(0).setMinWidth(0);
        tblDoctorSchedule.getColumnModel().getColumn(0).setMaxWidth(0);
        tblDoctorSchedule.getColumnModel().getColumn(0).setWidth(0);
    }

    private void toggleEditMode(boolean isEditing) {
        // Text fields
        txtNameDetail.setEditable(isEditing);
        txtSpecializationDetail.setEditable(isEditing);
        txtQualificationDetail.setEditable(isEditing);
        txtExperienceDetail.setEditable(isEditing);
        txtHospitalDetail.setEditable(isEditing);
        txtRegNoDetail.setEditable(isEditing);
        txtFeeDetail.setEditable(isEditing);

        // Buttons
        btnSaveChanges.setEnabled(isEditing);
        btnAddSchedule.setEnabled(isEditing);
        btnRemoveSchedule.setEnabled(isEditing);
        btnEditDetails.setEnabled(!isEditing);
    }

    private void saveDoctorDetails() {
        Doctor selectedDoctor = doctorList.getSelectedValue();
        if (selectedDoctor == null) {
            return;
        }

        try {
            selectedDoctor.setName(txtNameDetail.getText());
            selectedDoctor.setSpecialization(txtSpecializationDetail.getText());
            selectedDoctor.setQualification(txtQualificationDetail.getText());
            selectedDoctor.setExperienceYears(Integer.parseInt(txtExperienceDetail.getText().replace(" Years", "").trim()));
            selectedDoctor.setPracticingHospital(txtHospitalDetail.getText());
            selectedDoctor.setRegistrationNumber(txtRegNoDetail.getText());
            selectedDoctor.setFee(Double.parseDouble(txtFeeDetail.getText().replace("LKR", "").trim()));

            doctorDAO.save(selectedDoctor); 
            JOptionPane.showMessageDialog(this, "Doctor details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            toggleEditMode(false); 
            loadInitialData();     
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Experience and Fee.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewSchedule() {
        Doctor selectedDoctor = doctorList.getSelectedValue();
        if (selectedDoctor == null) {
            return;
        }

        AddScheduleDialog dialog = new AddScheduleDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setVisible(true);

        dialog.getNewSchedule().ifPresent(newSchedule -> {

            boolean hasConflict = scheduleDAO.hasTimeOverlap(
                    selectedDoctor,
                    newSchedule.getDayOfWeek(),
                    newSchedule.getStartTime(),
                    newSchedule.getEndTime()
            );

            if (hasConflict) {
                JOptionPane.showMessageDialog(this, "Time overlap detected! The doctor already has a schedule during this time on this day.", "Schedule Conflict", JOptionPane.ERROR_MESSAGE);
                return; 
            }

            
            newSchedule.setDoctor(selectedDoctor);
            scheduleDAO.save(newSchedule);

            JOptionPane.showMessageDialog(this, "New schedule added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            populateScheduleTable(selectedDoctor);
        });
    }
    
    private void removeSelectedSchedule() {
        int selectedRow = tblDoctorSchedule.getSelectedRow();
        Doctor selectedDoctor = doctorList.getSelectedValue();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a schedule from the table to remove.", "No Schedule Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove this schedule?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int scheduleId = (int) tblDoctorSchedule.getModel().getValueAt(selectedRow, 0);

            scheduleDAO.deleteById(scheduleId);

            JOptionPane.showMessageDialog(this, "Schedule removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            populateScheduleTable(selectedDoctor);
        }
    }

    private static class DoctorListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Doctor) {
                Doctor doc = (Doctor) value;
                label.setText(String.format("<html><b>%s</b><br/><i style='color:gray;'>%s</i></html>", doc.getName(), doc.getSpecialization()));
            }
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            return label;
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlFilter = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtSearchDoctor = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cmbFilterSpecialization = new javax.swing.JComboBox<>();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        doctorList = new javax.swing.JList<>();
        pnlDetailView = new javax.swing.JPanel();
        pnlDoctorInfoForm = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtNameDetail = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtSpecializationDetail = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtQualificationDetail = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtExperienceDetail = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtHospitalDetail = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtRegNoDetail = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtFeeDetail = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        btnEditDetails = new javax.swing.JButton();
        btnSaveChanges = new javax.swing.JButton();
        btnAddSchedule = new javax.swing.JButton();
        btnRemoveSchedule = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDoctorSchedule = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Search Doctor :");
        jLabel1.setPreferredSize(new java.awt.Dimension(120, 20));
        pnlFilter.add(jLabel1);

        txtSearchDoctor.setPreferredSize(new java.awt.Dimension(300, 40));
        pnlFilter.add(txtSearchDoctor);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Filter by Specialization :");
        jLabel2.setPreferredSize(new java.awt.Dimension(170, 20));
        pnlFilter.add(jLabel2);

        cmbFilterSpecialization.setPreferredSize(new java.awt.Dimension(300, 40));
        pnlFilter.add(cmbFilterSpecialization);

        add(pnlFilter, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setDividerLocation(350);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "All Doctors", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(doctorList);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel1);

        pnlDetailView.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Doctor Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N
        pnlDetailView.setLayout(new java.awt.BorderLayout());

        pnlDoctorInfoForm.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Name :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(jLabel3, gridBagConstraints);

        txtNameDetail.setPreferredSize(new java.awt.Dimension(89, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(txtNameDetail, gridBagConstraints);

        jLabel4.setText("Specialization :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(jLabel4, gridBagConstraints);

        txtSpecializationDetail.setPreferredSize(new java.awt.Dimension(89, 40));
        txtSpecializationDetail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSpecializationDetailActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(txtSpecializationDetail, gridBagConstraints);

        jLabel5.setText("Qualification :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(jLabel5, gridBagConstraints);

        txtQualificationDetail.setPreferredSize(new java.awt.Dimension(89, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(txtQualificationDetail, gridBagConstraints);

        jLabel6.setText("Experience :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(jLabel6, gridBagConstraints);

        txtExperienceDetail.setPreferredSize(new java.awt.Dimension(89, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(txtExperienceDetail, gridBagConstraints);

        jLabel7.setText("Hospital :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(jLabel7, gridBagConstraints);

        txtHospitalDetail.setPreferredSize(new java.awt.Dimension(89, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(txtHospitalDetail, gridBagConstraints);

        jLabel8.setText("Reg No :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(jLabel8, gridBagConstraints);

        txtRegNoDetail.setPreferredSize(new java.awt.Dimension(89, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(txtRegNoDetail, gridBagConstraints);

        jLabel9.setText("Fee :");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(jLabel9, gridBagConstraints);

        txtFeeDetail.setPreferredSize(new java.awt.Dimension(89, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        pnlDoctorInfoForm.add(txtFeeDetail, gridBagConstraints);

        pnlDetailView.add(pnlDoctorInfoForm, java.awt.BorderLayout.PAGE_START);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnEditDetails.setText("Edit Details");
        btnEditDetails.setPreferredSize(new java.awt.Dimension(170, 40));
        jPanel4.add(btnEditDetails);

        btnSaveChanges.setText("Save Changes");
        btnSaveChanges.setPreferredSize(new java.awt.Dimension(170, 40));
        jPanel4.add(btnSaveChanges);

        btnAddSchedule.setText("Add Schedule");
        btnAddSchedule.setPreferredSize(new java.awt.Dimension(170, 40));
        jPanel4.add(btnAddSchedule);

        btnRemoveSchedule.setText("Remove Schedule");
        btnRemoveSchedule.setPreferredSize(new java.awt.Dimension(170, 40));
        jPanel4.add(btnRemoveSchedule);

        pnlDetailView.add(jPanel4, java.awt.BorderLayout.PAGE_END);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Weekly Schedule", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        tblDoctorSchedule.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Location", "Day of Week", "Start Time", "End Time"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblDoctorSchedule.setRowHeight(40);
        tblDoctorSchedule.setShowGrid(false);
        tblDoctorSchedule.setShowHorizontalLines(true);
        jScrollPane2.setViewportView(tblDoctorSchedule);
        if (tblDoctorSchedule.getColumnModel().getColumnCount() > 0) {
            tblDoctorSchedule.getColumnModel().getColumn(0).setResizable(false);
            tblDoctorSchedule.getColumnModel().getColumn(1).setResizable(false);
            tblDoctorSchedule.getColumnModel().getColumn(2).setResizable(false);
            tblDoctorSchedule.getColumnModel().getColumn(3).setResizable(false);
        }

        pnlDetailView.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(pnlDetailView);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtSpecializationDetailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSpecializationDetailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSpecializationDetailActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddSchedule;
    private javax.swing.JButton btnEditDetails;
    private javax.swing.JButton btnRemoveSchedule;
    private javax.swing.JButton btnSaveChanges;
    private javax.swing.JComboBox<String> cmbFilterSpecialization;
    private javax.swing.JList<Doctor> doctorList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel pnlDetailView;
    private javax.swing.JPanel pnlDoctorInfoForm;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JTable tblDoctorSchedule;
    private javax.swing.JTextField txtExperienceDetail;
    private javax.swing.JTextField txtFeeDetail;
    private javax.swing.JTextField txtHospitalDetail;
    private javax.swing.JTextField txtNameDetail;
    private javax.swing.JTextField txtQualificationDetail;
    private javax.swing.JTextField txtRegNoDetail;
    private javax.swing.JTextField txtSearchDoctor;
    private javax.swing.JTextField txtSpecializationDetail;
    // End of variables declaration//GEN-END:variables
}
