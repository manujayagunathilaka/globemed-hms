/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui.report;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import lk.jiat.oodp.globemed.hms.model.dao.AppointmentDAO;
import lk.jiat.oodp.globemed.hms.model.dao.BillingClaimDAO;
import lk.jiat.oodp.globemed.hms.model.dao.DiagnosticResultDAO;
import lk.jiat.oodp.globemed.hms.model.dao.PatientDAO;
import lk.jiat.oodp.globemed.hms.model.entity.Appointment;
import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.entity.DiagnosticResult;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.generatingReport.DiagnosticResultsVisitor;
import lk.jiat.oodp.globemed.hms.generatingReport.FinancialReportVisitor;
import lk.jiat.oodp.globemed.hms.generatingReport.PatientSummaryVisitor;
import lk.jiat.oodp.globemed.hms.generatingReport.PatientWrapper;
import lk.jiat.oodp.globemed.hms.generatingReport.ReportVisitor;
import lk.jiat.oodp.globemed.hms.generatingReport.Visitable;
import lk.jiat.oodp.globemed.hms.gui.dialogs.ReportViewerDialog;
import lk.jiat.oodp.globemed.hms.model.entity.User;
import lk.jiat.oodp.globemed.hms.model.enums.AppPermission;
import lk.jiat.oodp.globemed.hms.model.enums.UserRole;
import lk.jiat.oodp.globemed.hms.permissons.PermissionService;
import lk.jiat.oodp.globemed.hms.util.SessionManager;

/**
 *
 * @author Manujaya
 */
public class GenerateReportPanel extends javax.swing.JPanel {

    private final PatientDAO patientDAO;
    private final AppointmentDAO appointmentDAO;
    private final BillingClaimDAO billingClaimDAO;
    private final DiagnosticResultDAO diagnosticResultDAO;
    private Patient selectedPatient;
    private List<BillingClaim> currentFinancialClaims; 

    public GenerateReportPanel() {
        initComponents();
        this.patientDAO = new PatientDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.billingClaimDAO = new BillingClaimDAO();
        this.diagnosticResultDAO = new DiagnosticResultDAO();
        this.currentFinancialClaims = new ArrayList<>();

        setupPanel();
    }

    private void setupPanel() {
        ButtonGroup reportTypeGroup = new ButtonGroup();
        reportTypeGroup.add(rbtnPatientTreatmentSummary);
        reportTypeGroup.add(rbtnDiagnosticResults);

        UserRole role = SessionManager.getInstance().getCurrentUserProfile().getUser().getUserRole();

        if (role != UserRole.ADMIN) {

            jTabbedPane1.setEnabledAt(1, false);

            jTabbedPane1.setToolTipTextAt(1, "Access Denied: You must be an ADMIN to view financial reports.");
        }
        
        setupListeners();
    }

    private void setupListeners() {
        txtSearch.addActionListener(e -> findAndDisplayPatient());

        ActionListener autoUpdateListener = e -> updatePatientReportTable();
        rbtnPatientTreatmentSummary.addActionListener(autoUpdateListener);
        rbtnDiagnosticResults.addActionListener(autoUpdateListener);
        dcPatientReportFrom.getDateEditor().addPropertyChangeListener("date", e -> updatePatientReportTable());
        dcPatientReportTo.getDateEditor().addPropertyChangeListener("date", e -> updatePatientReportTable());

        btn4PatientReportGenerate.addActionListener(e -> generatePatientReport());

//        btnSearchFinancial.addActionListener(e -> generateFinancialReport());
//        btn4FinancialReportGenerate.addActionListener(e -> generateFinancialReport());

        btnSearchFinancial.addActionListener(e -> searchFinancialData());
        btn4FinancialReportGenerate.addActionListener(e -> generateFinalFinancialReportSummary());
    }

    private void findAndDisplayPatient() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty()) {
            return;
        }

        Optional<Patient> patientOpt = patientDAO.findByNic(searchText);
        if (patientOpt.isPresent()) {
            this.selectedPatient = patientOpt.get();
            txtPID.setText(String.valueOf(selectedPatient.getId()));
            txtName.setText(selectedPatient.getName());
            txtNIC.setText(selectedPatient.getNic());
            txtMobile.setText(selectedPatient.getMobileNumber());
            txtAddress.setText(selectedPatient.getAddress());
            txtGender.setText(selectedPatient.getGender().toString());
            int age = calculateAge(selectedPatient.getDateOfBirth());
            txtAge.setText(String.valueOf(age));

            ((DefaultTableModel) tblPatientReportDataLoad.getModel()).setRowCount(0);
        } else {
            JOptionPane.showMessageDialog(this, "Patient not found for NIC: " + searchText, "Not Found", JOptionPane.WARNING_MESSAGE);
            this.selectedPatient = null;
        }
    }

    private int calculateAge(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }
        LocalDate dob = ((java.sql.Date) dateOfBirth).toLocalDate();
        return Period.between(dob, LocalDate.now()).getYears();
    }

    private void updatePatientReportTable() {
        if (selectedPatient == null || !(rbtnPatientTreatmentSummary.isSelected() || rbtnDiagnosticResults.isSelected())) {
            ((DefaultTableModel) tblPatientReportDataLoad.getModel()).setRowCount(0);
            return;
        }

        Date fromDate = dcPatientReportFrom.getDate();
        Date toDate = dcPatientReportTo.getDate();
        boolean isSummaryReport = rbtnPatientTreatmentSummary.isSelected();

        SwingWorker<DefaultTableModel, Void> worker = new SwingWorker<>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                DefaultTableModel model = new DefaultTableModel();
                if (isSummaryReport) {
                    model.setColumnIdentifiers(new String[]{"Date", "Activity Type", "Details", "Location"});
                    List<Appointment> appointments = appointmentDAO.findByPatientAndDateRange(selectedPatient, fromDate, toDate);
                    List<BillingClaim> claims = billingClaimDAO.findByPatientAndDateRange(selectedPatient, fromDate, toDate);

                    for (Appointment app : appointments) {
                        model.addRow(new Object[]{app.getAppointmentTime(), "Consultation", "With " + app.getDoctor(), app.getLocation()});
                    }
                    for (BillingClaim claim : claims) {
                        model.addRow(new Object[]{claim.getCreatedDate(), "Billing", "Total: " + claim.getAmount(), "Billing Dept"});
                    }
                } else {
                    model.setColumnIdentifiers(new String[]{"Report Date", "Report Type", "Ordered By", "Findings"});
                    List<DiagnosticResult> results = diagnosticResultDAO.findByPatientAndDateRange(selectedPatient, fromDate, toDate);

                    for (DiagnosticResult res : results) {
                        model.addRow(new Object[]{res.getReportDate(), res.getReportType(), res.getOrderedBy().getName(), res.getFindings()});
                    }
                }
                return model;
            }

            @Override
            protected void done() {
                try {
                    tblPatientReportDataLoad.setModel(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void generatePatientReport() {
        if (selectedPatient == null) {
            JOptionPane.showMessageDialog(this, "Please find and select a patient first.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!rbtnPatientTreatmentSummary.isSelected() && !rbtnDiagnosticResults.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please select a report type first.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tblPatientReportDataLoad.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "There is no data in the table to generate a report.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reportContent = generateHtmlFromTable("Patient Report", (DefaultTableModel) tblPatientReportDataLoad.getModel());
        ReportViewerDialog viewer = new ReportViewerDialog((Frame) SwingUtilities.getWindowAncestor(this), "Generated Patient Report", reportContent);
        viewer.setVisible(true);
    }

    private void searchFinancialData() {
        User currentUser = SessionManager.getInstance().getCurrentUserProfile().getUser();
        PermissionService permissionService = new PermissionService();

        if (!permissionService.checkPermission(currentUser, AppPermission.ACCESS_FINANCIAL_REPORTS)) {
            JOptionPane.showMessageDialog(this, "You do not have permission to generate this report.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return; 
        }
        
        Date fromDate = dcFinancialReportFrom.getDate();
        Date toDate = dcFinancialReportTo.getDate();
        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid date range.", "Date Not Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<List<BillingClaim>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BillingClaim> doInBackground() throws Exception {
                return billingClaimDAO.findByDateRange(fromDate, toDate);
            }

            @Override
            protected void done() {
                try {
                    currentFinancialClaims = get();

                    DefaultTableModel model = (DefaultTableModel) tblFinancialReportDataLoad.getModel();
                    model.setRowCount(0);

                    model.setColumnIdentifiers(new String[]{"Date", "Patient Name", "Amount (LKR)", "Billing Type"});
                    for (BillingClaim claim : currentFinancialClaims) {
                        model.addRow(new Object[]{
                            claim.getCreatedDate(),
                            claim.getPatient().getName(),
                            claim.getAmount(),
                            claim.getBillingType()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(GenerateReportPanel.this, "Error fetching financial data.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void generateFinalFinancialReportSummary() {
        if (currentFinancialClaims == null || currentFinancialClaims.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please search for financial data first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        FinancialReportVisitor visitor = new FinancialReportVisitor();
        for (BillingClaim claim : currentFinancialClaims) {
            visitor.visit(claim);
        }

//        String summaryContent = visitor.getReport();
//        showReportInDialog("Financial Summary Report", summaryContent);

        String reportContent = generateHtmlFromTable("Finance Report", (DefaultTableModel) tblFinancialReportDataLoad.getModel());
        ReportViewerDialog viewer = new ReportViewerDialog((Frame) SwingUtilities.getWindowAncestor(this), "Generated Finance Report", reportContent);
        viewer.setVisible(true);
    }
    
    private void generateFinancialReport() {
        Date fromDate = dcFinancialReportFrom.getDate();
        Date toDate = dcFinancialReportTo.getDate();
        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid date range.", "Date Not Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingWorker<DefaultTableModel, Void> worker = new SwingWorker<>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                FinancialReportVisitor visitor = new FinancialReportVisitor();
                List<BillingClaim> claims = billingClaimDAO.findByDateRange(fromDate, toDate);
                for (BillingClaim claim : claims) {
                    visitor.visit(claim);
                }
                DefaultTableModel model = new DefaultTableModel();
                model.setColumnIdentifiers(new String[]{"Category", "Value"});
                model.addRow(new Object[]{"Total Revenue (LKR)", String.format("%.2f", visitor.getTotalRevenue())});
                model.addRow(new Object[]{"Revenue from Insurance (LKR)", String.format("%.2f", visitor.getInsuranceRevenue())});
                model.addRow(new Object[]{"Revenue from Direct (LKR)", String.format("%.2f", visitor.getDirectRevenue())});
                return model;
            }

            @Override
            protected void done() {
                try {
                    DefaultTableModel resultModel = get();
                    tblFinancialReportDataLoad.setModel(resultModel);
                    showReportInDialog("Financial Report", resultModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private String generateHtmlFromTable(String title, DefaultTableModel model) {
        StringBuilder reportContent = new StringBuilder();
        reportContent.append("<h1>").append(title).append("</h1>");
        if (selectedPatient != null) {
            reportContent.append("<p><b>Patient:</b> ").append(selectedPatient.getName()).append(" (ID: ").append(selectedPatient.getId()).append(")</p>");
        }
        reportContent.append("<hr>");
        reportContent.append("<table border='1' cellpadding='5' cellspacing='0' width='100%'>");
        reportContent.append("<tr>");
        for (int i = 0; i < model.getColumnCount(); i++) {
            reportContent.append("<th>").append(model.getColumnName(i)).append("</th>");
        }
        reportContent.append("</tr>");
        for (int i = 0; i < model.getRowCount(); i++) {
            reportContent.append("<tr>");
            for (int j = 0; j < model.getColumnCount(); j++) {
                reportContent.append("<td>").append(model.getValueAt(i, j)).append("</td>");
            }
            reportContent.append("</tr>");
        }
        reportContent.append("</table>");
        return reportContent.toString();
    }

    private void showReportInDialog(String title, DefaultTableModel model) {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data found for the selected criteria.", "No Data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String reportContent = generateHtmlFromTable(title, model);
        ReportViewerDialog viewer = new ReportViewerDialog((Frame) SwingUtilities.getWindowAncestor(this), title, reportContent);
        viewer.setVisible(true);
    }

    private void resetPatientTab() {
        selectedPatient = null;
        txtSearch.setText("");
        txtPID.setText("");
        txtName.setText("");
        txtNIC.setText("");
        txtMobile.setText("");
        txtAddress.setText("");
        txtGender.setText("");
        txtAge.setText("");

        ButtonGroup bg = (ButtonGroup) rbtnPatientTreatmentSummary.getModel().getGroup();
        bg.clearSelection();

        dcPatientReportFrom.setDate(null);
        dcPatientReportTo.setDate(null);

        ((DefaultTableModel) tblPatientReportDataLoad.getModel()).setRowCount(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtPID = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtAge = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtGender = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtNIC = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtMobile = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        rbtnPatientTreatmentSummary = new javax.swing.JRadioButton();
        rbtnDiagnosticResults = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel9 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        dcPatientReportFrom = new com.toedter.calendar.JDateChooser();
        jLabel10 = new javax.swing.JLabel();
        dcPatientReportTo = new com.toedter.calendar.JDateChooser();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearchPatient = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPatientReportDataLoad = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        btn4PatientReportGenerate = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        dcFinancialReportFrom = new com.toedter.calendar.JDateChooser();
        jLabel12 = new javax.swing.JLabel();
        dcFinancialReportTo = new com.toedter.calendar.JDateChooser();
        btnSearchFinancial = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblFinancialReportDataLoad = new javax.swing.JTable();
        jPanel14 = new javax.swing.JPanel();
        btn4FinancialReportGenerate = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jPanel1.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel3.setPreferredSize(new java.awt.Dimension(1079, 300));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel5.setMinimumSize(new java.awt.Dimension(100, 300));
        jPanel5.setPreferredSize(new java.awt.Dimension(1092, 300));
        jPanel5.setLayout(new java.awt.GridLayout(1, 2));

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Patient Details"));
        jPanel7.setPreferredSize(new java.awt.Dimension(545, 300));

        jLabel2.setText("Patient ID :");

        jLabel3.setText("Name :");

        jLabel4.setText("Age :");

        jLabel5.setText("Gender :");

        jLabel6.setText("NIC :");

        jLabel7.setText("Mobile :");

        jLabel8.setText("Address :");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(31, 31, 31)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(43, 43, 43)
                                .addComponent(txtAge, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtGender, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMobile, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtAddress))))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPID, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(287, 287, 287))
                            .addComponent(txtNIC, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(13, 13, 13))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPID, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNIC, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAge, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txtMobile, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtGender, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel7);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Report Type"));

        rbtnPatientTreatmentSummary.setText("Patient Treatment Summary");

        rbtnDiagnosticResults.setText("Diagnostic Results ");

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter By Date"));

        jLabel9.setText("From :");

        jLabel10.setText("To :");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(dcPatientReportFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dcPatientReportTo, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dcPatientReportFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcPatientReportTo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(rbtnPatientTreatmentSummary)
                        .addGap(35, 35, 35)
                        .addComponent(rbtnDiagnosticResults)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtnPatientTreatmentSummary, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rbtnDiagnosticResults, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.add(jPanel8);

        jPanel3.add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel6.setPreferredSize(new java.awt.Dimension(1079, 60));

        jLabel1.setText("Search :");

        txtSearch.setPreferredSize(new java.awt.Dimension(85, 40));

        btnSearchPatient.setText("Search");

        btnReset.setText("Reset");
        btnReset.setPreferredSize(new java.awt.Dimension(97, 40));
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(29, 29, 29)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 310, Short.MAX_VALUE)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSearchPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSearchPatient, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel3.add(jPanel6, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setTopComponent(jPanel3);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Patient's Report Details"));
        jPanel4.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);

        tblPatientReportDataLoad.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblPatientReportDataLoad.setRowHeight(40);
        jScrollPane1.setViewportView(tblPatientReportDataLoad);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel15.setMinimumSize(new java.awt.Dimension(100, 50));
        jPanel15.setPreferredSize(new java.awt.Dimension(1069, 50));

        btn4PatientReportGenerate.setText("Generate Report");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addGap(0, 930, Short.MAX_VALUE)
                .addComponent(btn4PatientReportGenerate))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn4PatientReportGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4.add(jPanel15, java.awt.BorderLayout.PAGE_END);

        jSplitPane1.setRightComponent(jPanel4);

        jPanel1.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Patient Reports", jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel10.setLayout(new java.awt.BorderLayout());

        jPanel11.setPreferredSize(new java.awt.Dimension(1079, 120));
        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter By Date"));
        jPanel13.setPreferredSize(new java.awt.Dimension(1079, 50));

        jLabel11.setText("From :");

        jLabel12.setText("To :");

        btnSearchFinancial.setText("Search");
        btnSearchFinancial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchFinancialActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap(242, Short.MAX_VALUE)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(dcFinancialReportFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(150, 150, 150)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addContainerGap(445, Short.MAX_VALUE))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(dcFinancialReportTo, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSearchFinancial, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16))))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSearchFinancial, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(dcFinancialReportFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dcFinancialReportTo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(20, 20, 20))
        );

        jPanel11.add(jPanel13, java.awt.BorderLayout.CENTER);

        jPanel10.add(jPanel11, java.awt.BorderLayout.PAGE_START);

        jPanel12.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Financial Details"));
        jPanel12.add(jScrollPane2, java.awt.BorderLayout.PAGE_START);

        tblFinancialReportDataLoad.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblFinancialReportDataLoad.setRowHeight(40);
        jScrollPane3.setViewportView(tblFinancialReportDataLoad);

        jPanel12.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        jPanel10.add(jPanel12, java.awt.BorderLayout.CENTER);

        jPanel14.setPreferredSize(new java.awt.Dimension(1079, 50));

        btn4FinancialReportGenerate.setText("Generate Report");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addContainerGap(934, Short.MAX_VALUE)
                .addComponent(btn4FinancialReportGenerate)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn4FinancialReportGenerate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel10.add(jPanel14, java.awt.BorderLayout.PAGE_END);

        jPanel2.add(jPanel10, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Financial Reports", jPanel2);

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed

        resetPatientTab();

    }//GEN-LAST:event_btnResetActionPerformed

    private void btnSearchFinancialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchFinancialActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchFinancialActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn4FinancialReportGenerate;
    private javax.swing.JButton btn4PatientReportGenerate;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearchFinancial;
    private javax.swing.JButton btnSearchPatient;
    private com.toedter.calendar.JDateChooser dcFinancialReportFrom;
    private com.toedter.calendar.JDateChooser dcFinancialReportTo;
    private com.toedter.calendar.JDateChooser dcPatientReportFrom;
    private com.toedter.calendar.JDateChooser dcPatientReportTo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton rbtnDiagnosticResults;
    private javax.swing.JRadioButton rbtnPatientTreatmentSummary;
    private javax.swing.JTable tblFinancialReportDataLoad;
    private javax.swing.JTable tblPatientReportDataLoad;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtAge;
    private javax.swing.JTextField txtGender;
    private javax.swing.JTextField txtMobile;
    private javax.swing.JTextField txtNIC;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPID;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
