/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui.appoinment;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import lk.jiat.oodp.globemed.hms.appointment.AppointmentScheduler;
import lk.jiat.oodp.globemed.hms.appointment.DoctorColleague;
import lk.jiat.oodp.globemed.hms.appointment.PatientColleague;
import lk.jiat.oodp.globemed.hms.model.dao.AppointmentDAO;
import lk.jiat.oodp.globemed.hms.model.dao.DoctorDAO;
import lk.jiat.oodp.globemed.hms.model.dao.DoctorScheduleDAO;
import lk.jiat.oodp.globemed.hms.model.dao.PatientDAO;
import lk.jiat.oodp.globemed.hms.model.entity.Appointment;
import lk.jiat.oodp.globemed.hms.model.entity.Doctor;
import lk.jiat.oodp.globemed.hms.model.entity.DoctorSchedule;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.enums.AppointmentType;
import lk.jiat.oodp.globemed.hms.model.enums.DayOfWeek;
import lk.jiat.oodp.globemed.hms.model.enums.SessionAvailability;

/**
 *
 * @author Manujaya
 */
public class AppointmentMainPanel extends javax.swing.JPanel {

    private final AppointmentScheduler mediator;
    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;
    private final AppointmentDAO appointmentDAO;
    private final DoctorScheduleDAO doctorScheduleDAO;

    private final LocalTime START_TIME = LocalTime.of(9, 0);
    private final LocalTime END_TIME = LocalTime.of(17, 0);
    private final int SLOT_DURATION_MINUTES = 20;
    
    public AppointmentMainPanel() {
        initComponents();

        this.mediator = new AppointmentScheduler();
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.doctorScheduleDAO = new DoctorScheduleDAO();

        setupPanel();
    }

    private void setupPanel() {
        // Patient Table
        List<Patient> allPatients = patientDAO.findAll();
        DefaultTableModel patientModel = (DefaultTableModel) tblPatients.getModel();
        patientModel.setRowCount(0);
        for (Patient p : allPatients) {
            patientModel.addRow(new Object[]{p.getId(), p.getPatientUID(), p.getName(), p.getNic(), p.getMobileNumber()});
        }

        // Doctor Table
        filterDoctors(); 

        // Filter ComboBoxes
        List<String> specializations = doctorDAO.findDistinctSpecializations();
        DefaultComboBoxModel<String> specModel = new DefaultComboBoxModel<>();
        specModel.addElement("All Specializations");
        specializations.forEach(specModel::addElement);
        cmbSearchSpecialization.setModel(specModel);
        cmbSearchLocation.setModel(new DefaultComboBoxModel<>(new String[]{"All Locations", "GlobeMed Colombo", "GlobeMed Kandy"}));
        cmbAppoinmentType.setModel(new DefaultComboBoxModel<>(AppointmentType.values()));
        cmbSessionAvailability.setModel(new DefaultComboBoxModel<>(SessionAvailability.values()));

        // Patient Search
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                List<Patient> patients = patientDAO.searchByNameOrNic(txtSearch.getText());
                patientModel.setRowCount(0);
                for (Patient p : patients) {
                    patientModel.addRow(new Object[]{p.getId(), p.getPatientUID(), p.getName(), p.getNic(), p.getMobileNumber()});
                }
            }
        });

        // Patient Selection
        tblPatients.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblPatients.getSelectedRow();
                if (row == -1) {
                    return;
                }
                int patientId = (int) tblPatients.getValueAt(row, 0);
                patientDAO.findById(patientId).ifPresent(p -> {
                    txtPatientName.setText(p.getName());
                    txtPatientMobile.setText(p.getMobileNumber());
                });
            }
        });

        // Doctor Filters
        txtSearchDocName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterDoctors();
            }
        });
        cmbSearchSpecialization.addActionListener(e -> filterDoctors());
        cmbSearchLocation.addActionListener(e -> filterDoctors());

        // Doctor Double-Click (Session Load)
        tblDoctors.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    updateSessionTable();
                }
            }
        });

        // Date Chooser
        dcSearchSessions.getDateEditor().addPropertyChangeListener("date", e -> updateSessionTable());

        // Session Selection
        tblSessionDetails.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int doctorRow = tblDoctors.getSelectedRow();
                int sessionRow = tblSessionDetails.getSelectedRow();
                if (doctorRow != -1 && sessionRow != -1) {
                    txtDoctorName.setText(tblDoctors.getValueAt(doctorRow, 1).toString());
                    txtLocation.setText(tblSessionDetails.getValueAt(sessionRow, 0).toString());
                    txtAppoinmentTimeslot.setText(tblSessionDetails.getValueAt(sessionRow, 1).toString());
                    txtAppoinmentDate.setText(tblSessionDetails.getValueAt(sessionRow, 2).toString());
                }
            }
        });

        // Doctor Selection -> Doctor details and Session table update
        tblDoctors.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                System.out.println("--> UI-EVENT: Doctor selection changed.");
                updateDoctorDetailsOnRightPanel();
                updateSessionTable(); 
            }
        });

        dcSearchSessions.getDateEditor().addPropertyChangeListener("date", e -> {
            System.out.println("--> UI-EVENT: Date changed."); 
            updateSessionTable();
        });
        cmbSessionAvailability.addActionListener(e -> updateSessionTable());

        tblSessionDetails.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateAppointmentDetailsFromSession();
            }
        });
        
        btnBookAppoinment.addActionListener(e -> {
            System.out.println("--> UI-EVENT: Book Appointment button clicked."); 
            bookAppointment();
        });
        btnResetAll.addActionListener(e -> resetForm());
    }

    private void filterDoctors() {
        String name = txtSearchDocName.getText();
        String spec = (String) cmbSearchSpecialization.getSelectedItem(); 
        String loc = (String) cmbSearchLocation.getSelectedItem();

        List<Doctor> doctors = doctorDAO.findDoctorsFiltered(name, spec, loc);
        DefaultTableModel docModel = (DefaultTableModel) tblDoctors.getModel();
        docModel.setRowCount(0);
        for (Doctor d : doctors) {
            docModel.addRow(new Object[]{d.getId(), d.getName(), d.getSpecialization(), d.getPracticingHospital()});
        }
    }

    private void updatePatientDetailsOnRightPanel() {
        int row = tblPatients.getSelectedRow();
        if (row == -1) {
            return;
        }
        patientDAO.findById((int) tblPatients.getValueAt(row, 0)).ifPresent(p -> {
            txtPatientName.setText(p.getName());
            txtPatientMobile.setText(p.getMobileNumber());
        });
    }

    private void updateDoctorDetailsOnRightPanel() {
        int row = tblDoctors.getSelectedRow();
        if (row == -1) {
            return;
        }
        doctorDAO.findById((int) tblDoctors.getValueAt(row, 0)).ifPresent(d -> {
            txtDoctorName.setText(d.getName());
        });
    }

    private void updateSessionTable() {
        System.out.println("PANEL (UI-MEDIATOR): Received request to update session table."); // 👈 Log 4

        DefaultTableModel sessionModel = (DefaultTableModel) tblSessionDetails.getModel();
        sessionModel.setRowCount(0);
        int selectedDoctorRow = tblDoctors.getSelectedRow();
        if (selectedDoctorRow == -1) {
            return;
        }

        int doctorId = (int) tblDoctors.getValueAt(selectedDoctorRow, 0);

        List<Object[]> allScheduleData = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            DayOfWeek day = DayOfWeek.valueOf(date.getDayOfWeek().name()); 

            List<DoctorSchedule> schedulesForDay = doctorScheduleDAO.findByDoctorAndDay(doctorId, day);
            if (schedulesForDay.isEmpty()) {
                continue; 
            }
            List<LocalDateTime> bookedSlots = appointmentDAO.findByDoctorAndDate(doctorId, date)
                    .stream()
                    .map(Appointment::getAppointmentTime)
                    .collect(Collectors.toList());

            for (DoctorSchedule schedule : schedulesForDay) {
                LocalTime time = schedule.getStartTime();
                while (time.isBefore(schedule.getEndTime())) {
                    LocalDateTime slot = LocalDateTime.of(date, time);
                    boolean isBooked = bookedSlots.contains(slot);
                    allScheduleData.add(new Object[]{
                        schedule.getLocation(),
                        slot.toLocalTime(),
                        slot.toLocalDate(),
                        isBooked ? "Booked" : "Available"
                    });
                    time = time.plusMinutes(SLOT_DURATION_MINUTES);
                }
            }
        }

        SessionAvailability availabilityFilter = (SessionAvailability) cmbSessionAvailability.getSelectedItem();
        java.util.Date dateFilter = dcSearchSessions.getDate();

        for (Object[] slotData : allScheduleData) {
            LocalDate slotDate = (LocalDate) slotData[2];
            String availability = (String) slotData[3];

            boolean dateMatch = (dateFilter == null) || slotDate.equals(dateFilter.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            boolean availabilityMatch = (availabilityFilter == SessionAvailability.All) || availability.equalsIgnoreCase(availabilityFilter.name());

            if (dateMatch && availabilityMatch) {
                sessionModel.addRow(slotData);
            }
        }
    }

    private void populateAppointmentDetailsFromSession() {
        int sessionRow = tblSessionDetails.getSelectedRow();
        if (sessionRow == -1) {
            return;
        }

        if (tblSessionDetails.getValueAt(sessionRow, 3).toString().equals("Available")) {
            String location = tblSessionDetails.getValueAt(sessionRow, 0).toString();
            LocalTime time = (LocalTime) tblSessionDetails.getValueAt(sessionRow, 1);
            LocalDate date = (LocalDate) tblSessionDetails.getValueAt(sessionRow, 2);

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

            txtLocation.setText(location);
            txtAppoinmentDate.setText(date.toString());
            txtAppoinmentTimeslot.setText(time.format(timeFormatter));

            generateAppointmentDescription(); 
        }
    }

    private void generateAppointmentDescription() {
        if (txtPatientName.getText().isEmpty() || txtDoctorName.getText().isEmpty()) {
            return;
        }

        String desc = String.format("%s for patient '%s' with %s on %s at %s in %s.",
                cmbAppoinmentType.getSelectedItem(),
                txtPatientName.getText(),
                txtDoctorName.getText(),
                txtAppoinmentDate.getText(),
                txtAppoinmentTimeslot.getText(),
                txtLocation.getText()
        );
        txtAppoinmentDescription.setText(desc);
    }

    private void bookAppointment() {
        System.out.println("PANEL (UI-MEDIATOR): Gathering data to book appointment..."); 

        if (txtPatientName.getText().isEmpty() || txtDoctorName.getText().isEmpty() || txtAppoinmentTimeslot.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a patient, doctor, and a time slot.", "Incomplete Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientId = (int) tblPatients.getValueAt(tblPatients.getSelectedRow(), 0);
        int doctorId = (int) tblDoctors.getValueAt(tblDoctors.getSelectedRow(), 0);
        Patient patientEntity = patientDAO.findById(patientId).get();
        Doctor doctorEntity = doctorDAO.findById(doctorId).get();

        LocalDate date = LocalDate.parse(txtAppoinmentDate.getText());
        LocalTime time = LocalTime.parse(txtAppoinmentTimeslot.getText());
        LocalDateTime slot = LocalDateTime.of(date, time);

        String location = txtLocation.getText();
        AppointmentType type = (AppointmentType) cmbAppoinmentType.getSelectedItem();

        PatientColleague patientColleague = new PatientColleague(patientEntity);
        DoctorColleague doctorColleague = new DoctorColleague(doctorEntity);

        System.out.println("PANEL (UI-MEDIATOR): Handing over request to backend Mediator (AppointmentScheduler)...");
        boolean success = mediator.requestAppointment(patientColleague, doctorColleague, slot, location, type);

        if (success) {
            JOptionPane.showMessageDialog(this, "Appointment Booked Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            updateSessionTable(); // Refresh the session view
            resetForm();
        } else {
            JOptionPane.showMessageDialog(this, "This slot is not available. Please check the schedule.", "Booking Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlAppoinmentDetails = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtPatientName = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtPatientMobile = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtDoctorName = new javax.swing.JTextField();
        txtLocation = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtAppoinmentDate = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtAppoinmentTimeslot = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtAppoinmentDescription = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        btnResetAll = new javax.swing.JButton();
        btnBookAppoinment = new javax.swing.JButton();
        cmbAppoinmentType = new javax.swing.JComboBox<>();
        jSeparator2 = new javax.swing.JSeparator();
        pnlAppoinmentMain = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cmbSortBy = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPatients = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtSearchDocName = new javax.swing.JTextField();
        cmbSearchSpecialization = new javax.swing.JComboBox<>();
        cmbSearchLocation = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDoctors = new javax.swing.JTable();
        sessionPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblSessionDetails = new javax.swing.JTable();
        dcSearchSessions = new com.toedter.calendar.JDateChooser();
        jLabel17 = new javax.swing.JLabel();
        cmbSessionAvailability = new javax.swing.JComboBox<>();

        setLayout(new java.awt.BorderLayout());

        pnlAppoinmentDetails.setBackground(new java.awt.Color(202, 240, 248));
        pnlAppoinmentDetails.setPreferredSize(new java.awt.Dimension(300, 647));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Appointment Details :");

        jLabel9.setText("Patient Name :");

        jLabel10.setText("Patient Mobile Number :");

        jLabel11.setText("Doctor Name :");

        jLabel12.setText("Location :");

        jLabel13.setText("Date :");

        jLabel14.setText("Time :");

        jLabel15.setText("Appoinment Type :");

        jLabel16.setText("Appointment Description :");

        btnResetAll.setText("Reset");
        btnResetAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetAllActionPerformed(evt);
            }
        });

        btnBookAppoinment.setText("Book Appointment");

        javax.swing.GroupLayout pnlAppoinmentDetailsLayout = new javax.swing.GroupLayout(pnlAppoinmentDetails);
        pnlAppoinmentDetails.setLayout(pnlAppoinmentDetailsLayout);
        pnlAppoinmentDetailsLayout.setHorizontalGroup(
            pnlAppoinmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppoinmentDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAppoinmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlAppoinmentDetailsLayout.createSequentialGroup()
                        .addComponent(btnResetAll, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(btnBookAppoinment))
                    .addComponent(txtPatientName)
                    .addComponent(txtPatientMobile)
                    .addComponent(txtDoctorName)
                    .addComponent(txtLocation)
                    .addComponent(txtAppoinmentDate)
                    .addComponent(txtAppoinmentTimeslot)
                    .addComponent(txtAppoinmentDescription)
                    .addComponent(cmbAppoinmentType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlAppoinmentDetailsLayout.createSequentialGroup()
                        .addGroup(pnlAppoinmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jSeparator2)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlAppoinmentDetailsLayout.setVerticalGroup(
            pnlAppoinmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppoinmentDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(7, 7, 7)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPatientName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPatientMobile, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtDoctorName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAppoinmentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAppoinmentTimeslot, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbAppoinmentType, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAppoinmentDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addGroup(pnlAppoinmentDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnResetAll, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBookAppoinment, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        add(pnlAppoinmentDetails, java.awt.BorderLayout.LINE_END);

        pnlAppoinmentMain.setBackground(new java.awt.Color(255, 255, 255));

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        txtSearch.setText("Search");
        txtSearch.setMinimumSize(new java.awt.Dimension(85, 40));
        txtSearch.setPreferredSize(new java.awt.Dimension(0, 40));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtSearchKeyPressed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel2.setText("All Patients");
        jLabel2.setPreferredSize(null);

        jLabel3.setText("Sort by :");
        jLabel3.setPreferredSize(null);

        cmbSortBy.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSortBy.setMinimumSize(new java.awt.Dimension(95, 40));
        cmbSortBy.setPreferredSize(new java.awt.Dimension(0, 40));

        tblPatients.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "P. ID", "Name", "NIC", "Mobile", "Registered Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPatients.setRowHeight(40);
        tblPatients.setShowHorizontalLines(true);
        tblPatients.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblPatientsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblPatients);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
                    .addComponent(jSeparator1)
                    .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                    .addComponent(cmbSortBy, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 756, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addGap(11, 11, 11)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 724, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jTabbedPane1.addTab("Patient Details", jPanel1);

        jLabel1.setText("Doctor Name");

        jLabel4.setText("Specialization");

        jLabel5.setText("Location");

        tblDoctors.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "D. ID", "Name", "Specialization", "Mobile"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblDoctors.setRowHeight(40);
        jScrollPane2.setViewportView(tblDoctors);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Sessions :");

        jLabel8.setText("Date :");

        tblSessionDetails.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Location", "Time Slot", "Date", "Availability"
            }
        ));
        tblSessionDetails.setRowHeight(40);
        jScrollPane3.setViewportView(tblSessionDetails);

        jLabel17.setText("Availability :");

        javax.swing.GroupLayout sessionPanelLayout = new javax.swing.GroupLayout(sessionPanel);
        sessionPanel.setLayout(sessionPanelLayout);
        sessionPanelLayout.setHorizontalGroup(
            sessionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sessionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sessionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sessionPanelLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSessionAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dcSearchSessions, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE))
                .addContainerGap())
        );
        sessionPanelLayout.setVerticalGroup(
            sessionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sessionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sessionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dcSearchSessions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(sessionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cmbSessionAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSearchDocName, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbSearchSpecialization, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(cmbSearchLocation, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
            .addComponent(sessionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearchDocName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSearchSpecialization, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSearchLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sessionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Doctor", jPanel2);

        javax.swing.GroupLayout pnlAppoinmentMainLayout = new javax.swing.GroupLayout(pnlAppoinmentMain);
        pnlAppoinmentMain.setLayout(pnlAppoinmentMainLayout);
        pnlAppoinmentMainLayout.setHorizontalGroup(
            pnlAppoinmentMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppoinmentMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        pnlAppoinmentMainLayout.setVerticalGroup(
            pnlAppoinmentMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAppoinmentMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        add(pnlAppoinmentMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void tblPatientsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPatientsMouseClicked

    }//GEN-LAST:event_tblPatientsMouseClicked

    private void txtSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchKeyPressed

    private void btnResetAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetAllActionPerformed
        
        txtPatientName.setText("");
        txtPatientMobile.setText("");
        txtDoctorName.setText("");
        txtLocation.setText("");
        txtAppoinmentDate.setText("");
        txtAppoinmentTimeslot.setText("");
        txtAppoinmentDescription.setText("");
        if (cmbAppoinmentType.getItemCount() > 0) {
            cmbAppoinmentType.setSelectedIndex(0);
        }

        tblPatients.clearSelection();
        tblDoctors.clearSelection();
        tblSessionDetails.clearSelection();

        txtSearch.setText("");
        txtSearchDocName.setText("");
        if (cmbSearchSpecialization.getItemCount() > 0) {
            cmbSearchSpecialization.setSelectedIndex(0);
        }
        if (cmbSearchLocation.getItemCount() > 0) {
            cmbSearchLocation.setSelectedIndex(0);
        }
        dcSearchSessions.setDate(null);

        DefaultTableModel sessionModel = (DefaultTableModel) tblSessionDetails.getModel();
        sessionModel.setRowCount(0);

        filterDoctors();

        System.out.println("Form has been reset to its initial state.");
        
    }//GEN-LAST:event_btnResetAllActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBookAppoinment;
    private javax.swing.JButton btnResetAll;
    private javax.swing.JComboBox<AppointmentType> cmbAppoinmentType;
    private javax.swing.JComboBox<String> cmbSearchLocation;
    private javax.swing.JComboBox<String> cmbSearchSpecialization;
    private javax.swing.JComboBox<SessionAvailability> cmbSessionAvailability;
    private javax.swing.JComboBox<String> cmbSortBy;
    private com.toedter.calendar.JDateChooser dcSearchSessions;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel pnlAppoinmentDetails;
    private javax.swing.JPanel pnlAppoinmentMain;
    private javax.swing.JPanel sessionPanel;
    private javax.swing.JTable tblDoctors;
    private javax.swing.JTable tblPatients;
    private javax.swing.JTable tblSessionDetails;
    private javax.swing.JTextField txtAppoinmentDate;
    private javax.swing.JTextField txtAppoinmentDescription;
    private javax.swing.JTextField txtAppoinmentTimeslot;
    private javax.swing.JTextField txtDoctorName;
    private javax.swing.JTextField txtLocation;
    private javax.swing.JTextField txtPatientMobile;
    private javax.swing.JTextField txtPatientName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSearchDocName;
    // End of variables declaration//GEN-END:variables
}
