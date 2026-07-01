package lk.jiat.oodp.globemed.hms.gui.patient;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import lk.jiat.oodp.globemed.hms.exception.AccessDeniedException;
import lk.jiat.oodp.globemed.hms.patientManagement.PatientRecordAccessor;
import lk.jiat.oodp.globemed.hms.patientManagement.SimplePatientRecordAccessor;
import lk.jiat.oodp.globemed.hms.patientManagement.AccessControlDecorator;
import lk.jiat.oodp.globemed.hms.patientManagement.AuditLoggingDecorator;
import lk.jiat.oodp.globemed.hms.patientManagement.ValidationDecorator;
import lk.jiat.oodp.globemed.hms.exception.ValidationException;
import lk.jiat.oodp.globemed.hms.model.dao.PatientDAO;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;
import lk.jiat.oodp.globemed.hms.gui.dialogs.PatientDetailsDialog;
import lk.jiat.oodp.globemed.hms.model.enums.BloodType;
import lk.jiat.oodp.globemed.hms.model.enums.Gender;
import lk.jiat.oodp.globemed.hms.security.AccessControlProxy;
import lk.jiat.oodp.globemed.hms.model.entity.User;
import lk.jiat.oodp.globemed.hms.model.enums.AppPermission;
import lk.jiat.oodp.globemed.hms.permissons.PermissionService;
import lk.jiat.oodp.globemed.hms.util.SessionManager;

/**
 *
 * @author Manujaya
 */
public class PatientPanel extends javax.swing.JPanel {

    private PatientDAO patientDAO;
    final String placeholder = "Search by Name or NIC...";
    UserRole currentUserRole = SessionManager.getInstance().getCurrentUserProfile().getUser().getUserRole();

    public PatientPanel() {
        initComponents();
        this.patientDAO = new PatientDAO();
        loadPatients();
        setupPanel();
        
        JTableHeader header = tblPatients.getTableHeader();
        header.setBackground(new Color(202, 240, 248)); 
        header.setForeground(Color.BLACK);           
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setOpaque(true);
    }

    private void loadPatients() {
        PatientDAO patientDAO = new PatientDAO();
        List<Patient> allPatients = patientDAO.findAll();

        DefaultTableModel model = (DefaultTableModel) tblPatients.getModel();
        model.setRowCount(0);

        for (Patient patient : allPatients) {
            Vector<Object> row = new Vector<>();
            row.add(patient.getId());
            row.add(patient.getPatientUID());
            row.add(patient.getName());
            row.add(patient.getNic());
            row.add(patient.getMobileNumber());
            row.add(patient.getRegisteredDate());

            model.addRow(row);
        }
    }

    private void setupPanel() {
        if (currentUserRole != UserRole.ADMIN && currentUserRole != UserRole.RECEPTIONIST) {
            tbPatientManagement.setEnabledAt(1, false);
            tbPatientManagement.setToolTipTextAt(1, "You do not have permission to register patients.");
        }

        cbGender.setModel(new DefaultComboBoxModel<>(Gender.values()));

        cbBloodType.setModel(new DefaultComboBoxModel<>(BloodType.values()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        txtRegDate.setText(sdf.format(new Date()));

        txtSearch.setText(placeholder);
        txtSearch.setForeground(Color.GRAY);

        String[] sortOptions = {"Date (Oldest First)", "Date (Newest First)", "Name (A-Z)", "Name (Z-A)"};
        cmbSortBy.setModel(new DefaultComboBoxModel<>(sortOptions));

        updatePatientTable();
        loadNextPatientId();
    }

    private void updatePatientTable() {
        String searchText = txtSearch.getText();
        if (searchText.equals("Search by Name or NIC...")) {
            searchText = "";
        }

        String sortBy = cmbSortBy.getSelectedItem().toString();

        List<Patient> patients = patientDAO.findAndSortPatients(searchText, sortBy);

        DefaultTableModel model = (DefaultTableModel) tblPatients.getModel();
        model.setRowCount(0); 

        for (Patient p : patients) {
            model.addRow(new Object[]{
                p.getId(),
                p.getPatientUID(),
                p.getName(),
                p.getNic(),
                p.getMobileNumber(),
                p.getRegisteredDate()
            });
        }
    }

    private void loadNextPatientId() {
        int lastId = patientDAO.findLastPatientId();

        int nextId = lastId + 1;

        DecimalFormat formatter = new DecimalFormat("P_000");
        String nextPatientUID = formatter.format(nextId);

        txtPID.setText(nextPatientUID);
        txtPID.setEditable(false);
    }

    private void handlePatientRegistration() {
        try {
            String patientUID = txtPID.getText(); // Auto-generated ID
            String name = txtName.getText().trim();
            String nic = txtNIC.getText().trim();
            Date dob = dcDOB.getDate();
            Gender gender = (Gender) cbGender.getSelectedItem();
            BloodType bloodType = (BloodType) cbBloodType.getSelectedItem();
            String mobile = txtMobile.getText().trim();
            String address = txtAddress.getText().trim();
            String emgContactName = txtEmgContactName.getText().trim();
            String emgContactNumber = txtEmgContactNumber.getText().trim();

            if (name.isEmpty() || nic.isEmpty() || dob == null) {
                JOptionPane.showMessageDialog(this, "Name, NIC and Date of Birth are required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Patient newPatient = new Patient();
            newPatient.setPatientUID(patientUID);
            newPatient.setName(name);
            newPatient.setNic(nic);
            newPatient.setDateOfBirth(dob);
            newPatient.setGender(gender);
            newPatient.setBloodType(bloodType);
            newPatient.setMobileNumber(mobile);
            newPatient.setAddress(address);
            newPatient.setEmergencyContactName(emgContactName);
            newPatient.setEmergencyContactNumber(emgContactNumber);
            newPatient.setRegisteredDate(new Date());

            // Decorator pattern in action
            PatientRecordAccessor accessor = new SimplePatientRecordAccessor();
            accessor = new ValidationDecorator(accessor);
            accessor = new AuditLoggingDecorator(accessor);

            accessor.savePatient(newPatient);

            JOptionPane.showMessageDialog(this, "Patient registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            updatePatientTable(); 
            resetRegistrationForm(); 

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Failed", JOptionPane.ERROR_MESSAGE);
            updatePatientTable();
            resetRegistrationForm();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred during registration.", "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetRegistrationForm() {
        txtName.setText("");
        txtNIC.setText("");
        txtMobile.setText("");
        txtAddress.setText("");
        txtEmgContactName.setText("");
        txtEmgContactNumber.setText("");

        dcDOB.setDate(null);

        if (cbGender.getItemCount() > 0) {
            cbGender.setSelectedIndex(0);
        }
        if (cbBloodType.getItemCount() > 0) {
            cbBloodType.setSelectedIndex(0);
        }

        loadNextPatientId();

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tbPatientManagement = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cmbSortBy = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPatients = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtPID = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtNIC = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        dcDOB = new com.toedter.calendar.JDateChooser();
        jLabel10 = new javax.swing.JLabel();
        cbGender = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        cbBloodType = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel12 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel13 = new javax.swing.JLabel();
        txtMobile = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAddress = new javax.swing.JTextArea();
        jLabel15 = new javax.swing.JLabel();
        txtEmgContactName = new javax.swing.JTextField();
        txtEmgContactNumber = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel17 = new javax.swing.JLabel();
        txtRegDate = new javax.swing.JTextField();
        btnRegisterPatient = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(202, 240, 248));

        jLabel1.setBackground(new java.awt.Color(202, 240, 248));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Patient Management");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
        );

        tbPatientManagement.setBackground(new java.awt.Color(255, 255, 255));
        tbPatientManagement.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        tbPatientManagement.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tbPatientManagementStateChanged(evt);
            }
        });
        tbPatientManagement.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbPatientManagementMouseClicked(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        txtSearch.setText("Search");
        txtSearch.setMinimumSize(new java.awt.Dimension(85, 40));
        txtSearch.setPreferredSize(new java.awt.Dimension(85, 40));
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtSearchFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtSearchFocusLost(evt);
            }
        });
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel2.setText("All Patients");

        jLabel3.setText("Sort by :");

        cmbSortBy.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSortBy.setMinimumSize(new java.awt.Dimension(95, 40));
        cmbSortBy.setPreferredSize(new java.awt.Dimension(95, 40));
        cmbSortBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSortByActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(202, 240, 248), 15, true));

        tblPatients.setBackground(new java.awt.Color(202, 240, 248));
        tblPatients.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "No.", "P. ID", "Name", "NIC", "Mobile", "Registered Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPatients.setRowHeight(40);
        tblPatients.setShowGrid(false);
        tblPatients.setShowHorizontalLines(true);
        tblPatients.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPatientsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblPatients);
        if (tblPatients.getColumnModel().getColumnCount() > 0) {
            tblPatients.getColumnModel().getColumn(0).setMinWidth(0);
            tblPatients.getColumnModel().getColumn(0).setPreferredWidth(0);
            tblPatients.getColumnModel().getColumn(0).setMaxWidth(0);
            tblPatients.getColumnModel().getColumn(1).setResizable(false);
            tblPatients.getColumnModel().getColumn(2).setResizable(false);
            tblPatients.getColumnModel().getColumn(3).setResizable(false);
            tblPatients.getColumnModel().getColumn(4).setResizable(false);
            tblPatients.getColumnModel().getColumn(5).setResizable(false);
        }

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1140, Short.MAX_VALUE)
                    .addComponent(jSeparator1)
                    .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(cmbSortBy, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSortBy, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tbPatientManagement.addTab("Patient Details", jPanel1);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel4.setText("Register New Patient");

        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(202, 240, 248), 15, true));

        jPanel6.setBackground(new java.awt.Color(202, 240, 248));

        jLabel5.setText("P. ID :");

        txtPID.setEditable(false);
        txtPID.setPreferredSize(new java.awt.Dimension(85, 40));

        jLabel6.setText("NIC :");

        txtNIC.setPreferredSize(new java.awt.Dimension(85, 40));

        jLabel7.setText("Name :");

        txtName.setPreferredSize(new java.awt.Dimension(85, 40));

        jLabel9.setText("Date of Birth :");

        dcDOB.setMinimumSize(new java.awt.Dimension(104, 40));
        dcDOB.setPreferredSize(new java.awt.Dimension(114, 40));

        jLabel10.setText("Gender :");

        cbGender.setPreferredSize(new java.awt.Dimension(95, 40));

        jLabel11.setText("Blood Type :");

        cbBloodType.setMinimumSize(new java.awt.Dimension(95, 40));
        cbBloodType.setPreferredSize(new java.awt.Dimension(95, 40));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("Patient's Personal Details");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("Patient's Contact Details");

        jLabel13.setText("Mobile Number :");

        txtMobile.setPreferredSize(new java.awt.Dimension(85, 40));

        jLabel14.setText("Address :");

        txtAddress.setColumns(20);
        txtAddress.setRows(5);
        jScrollPane2.setViewportView(txtAddress);

        jLabel15.setText("Emg. Contact Name :");

        txtEmgContactName.setPreferredSize(new java.awt.Dimension(85, 40));

        txtEmgContactNumber.setPreferredSize(new java.awt.Dimension(85, 40));

        jLabel16.setText("Emg. Number :");

        jLabel17.setText("Registered Date :");

        txtRegDate.setEditable(false);
        txtRegDate.setPreferredSize(new java.awt.Dimension(85, 40));

        btnRegisterPatient.setText("Register Patient");
        btnRegisterPatient.setPreferredSize(new java.awt.Dimension(140, 40));
        btnRegisterPatient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterPatientActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator2))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator3))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator4)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addGap(18, 18, 18)
                                .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel6Layout.createSequentialGroup()
                                        .addComponent(jLabel13)
                                        .addGap(38, 38, 38)
                                        .addComponent(txtMobile, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addComponent(jLabel16)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(txtEmgContactNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addComponent(jLabel15)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtEmgContactName, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane2))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtRegDate, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnRegisterPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(29, 29, 29)
                                .addComponent(txtPID, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
                                .addComponent(jLabel6)
                                .addGap(18, 18, 18)
                                .addComponent(txtNIC, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                                .addComponent(jLabel9)
                                .addGap(35, 35, 35)
                                .addComponent(dcDOB, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(18, 18, 18)
                                .addComponent(cbGender, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(100, 100, 100)
                                .addComponent(jLabel11)
                                .addGap(18, 18, 18)
                                .addComponent(cbBloodType, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtNIC, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtPID, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dcDOB, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbBloodType, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbGender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)))
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtMobile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtEmgContactName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtEmgContactNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtRegDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnRegisterPatient, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tbPatientManagement.addTab("Register Patient", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tbPatientManagement))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbPatientManagement)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tblPatientsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPatientsMouseClicked

        if (evt.getClickCount() == 2) {

            try {
                PermissionService permissionService = new PermissionService();
                User currentUser = SessionManager.getInstance().getCurrentUserProfile().getUser();

            if (!permissionService.checkPermission(currentUser, AppPermission.CAN_VIEW_PATIENT_DETAILS)) {
                JOptionPane.showMessageDialog(this, "You do not have permission to view patient details.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                return; 
            }
            
                int selectedRow = tblPatients.getSelectedRow();

                if (selectedRow != -1) {

                    int patientId = (int) tblPatients.getValueAt(selectedRow, 0);

//                UserRole currentUserRole = UserRole.DOCTOR;
//                PatientRecordAccessor baseAccessor = new SimplePatientRecordAccessor();
//                PatientRecordAccessor securedAccessor = new AccessControlDecorator(baseAccessor);
//                PatientRecordAccessor finalAccessor = new AuditLoggingDecorator(securedAccessor);
//                baseAccessor = new AuditLoggingDecorator(baseAccessor);
                    PatientRecordAccessor dataAccessor = new SimplePatientRecordAccessor();
                    dataAccessor = new AccessControlProxy(dataAccessor);
                    dataAccessor = new AuditLoggingDecorator(dataAccessor);
                    Patient selectedPatient = patientDAO.findById(patientId).orElse(null);

                    PatientDetailsDialog detailsDialog = new PatientDetailsDialog(
                            (JFrame) SwingUtilities.getWindowAncestor(this),
                            true,
                            patientId,
                            dataAccessor,
                            selectedPatient
                    );
                    detailsDialog.setVisible(true);
                }
            } catch (AccessDeniedException e) {
                JOptionPane.showMessageDialog(this,e.getMessage(), "Security Alert", JOptionPane.ERROR_MESSAGE);
            } 
            

        }

    }//GEN-LAST:event_tblPatientsMouseClicked

    private void txtSearchFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSearchFocusGained

        if (txtSearch.getText().equals(placeholder)) {
            txtSearch.setText("");
            txtSearch.setForeground(Color.BLACK);
        }

    }//GEN-LAST:event_txtSearchFocusGained

    private void txtSearchFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtSearchFocusLost

        if (txtSearch.getText().isEmpty()) {
            txtSearch.setText(placeholder);
            txtSearch.setForeground(Color.GRAY);
        }

    }//GEN-LAST:event_txtSearchFocusLost

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased

        updatePatientTable();

    }//GEN-LAST:event_txtSearchKeyReleased

    private void cmbSortByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSortByActionPerformed

        updatePatientTable();

    }//GEN-LAST:event_cmbSortByActionPerformed

    private void btnRegisterPatientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterPatientActionPerformed

        handlePatientRegistration();

    }//GEN-LAST:event_btnRegisterPatientActionPerformed

    private void tbPatientManagementStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tbPatientManagementStateChanged

//        loadNextPatientId();

    }//GEN-LAST:event_tbPatientManagementStateChanged

    private void tbPatientManagementMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbPatientManagementMouseClicked

        loadNextPatientId();

    }//GEN-LAST:event_tbPatientManagementMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRegisterPatient;
    private javax.swing.JComboBox<BloodType> cbBloodType;
    private javax.swing.JComboBox<Gender> cbGender;
    private javax.swing.JComboBox<String> cmbSortBy;
    private com.toedter.calendar.JDateChooser dcDOB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTabbedPane tbPatientManagement;
    private javax.swing.JTable tblPatients;
    private javax.swing.JTextArea txtAddress;
    private javax.swing.JTextField txtEmgContactName;
    private javax.swing.JTextField txtEmgContactNumber;
    private javax.swing.JTextField txtMobile;
    private javax.swing.JTextField txtNIC;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPID;
    private javax.swing.JTextField txtRegDate;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
