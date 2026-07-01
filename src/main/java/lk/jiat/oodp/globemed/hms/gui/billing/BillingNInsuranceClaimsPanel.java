/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui.billing;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import lk.jiat.oodp.globemed.hms.billingNinsurance.AbstractBillingHandler;
import lk.jiat.oodp.globemed.hms.billingNinsurance.BillingChainBuilder;
import lk.jiat.oodp.globemed.hms.billingNinsurance.HandlerStatusListener;
import lk.jiat.oodp.globemed.hms.model.dao.BillingClaimDAO;
import lk.jiat.oodp.globemed.hms.model.dao.PatientDAO;
import lk.jiat.oodp.globemed.hms.model.entity.BillingClaim;
import lk.jiat.oodp.globemed.hms.model.entity.Patient;
import lk.jiat.oodp.globemed.hms.model.enums.BillingType;
import lk.jiat.oodp.globemed.hms.model.enums.ServiceCategory;
import lk.jiat.oodp.globemed.hms.gui.billing.HandlerPanel;

/**
 *
 * @author Manujaya
 */
public class BillingNInsuranceClaimsPanel extends javax.swing.JPanel implements HandlerStatusListener {

    private static class ServiceItem {

        ServiceCategory category; 
        String details;
        double charge;

        public ServiceItem(ServiceCategory category, String details, double charge) {
            this.category = category;
            this.details = details;
            this.charge = charge;
        }

        public ServiceCategory getCategory() {
            return category;
        }

        public String getDetails() {
            return details;
        }

        public double getCharge() {
            return charge;
        }
    }

    private final PatientDAO patientDAO;
    private final BillingClaimDAO billingClaimDAO;

    private Patient currentPatient;
    private final List<ServiceItem> serviceItems;
    private final List<HandlerPanel> handlerPanels;

    public BillingNInsuranceClaimsPanel() {
        initComponents();
        this.patientDAO = new PatientDAO();
        this.billingClaimDAO = new BillingClaimDAO();
        this.serviceItems = new ArrayList<>();
        this.handlerPanels = new ArrayList<>();
        setupPanel();
    }

    private void setupPanel() {
        layeredBillDetailsPane.removeAll();
        layeredBillDetailsPane.add(pnlServiceDetails);
        layeredBillDetailsPane.repaint();
        layeredBillDetailsPane.revalidate();

        pnlInsuranceDetails.setVisible(false);
        
        cmbBillingType.setModel(new DefaultComboBoxModel<>(BillingType.values()));
        cmbServiceCategory.setModel(new DefaultComboBoxModel<>(ServiceCategory.values()));

        setupListeners();
    }

    private void setupListeners() {
        txtSerchByNIC.addActionListener(e -> findPatientByNic());

        btnAddServiceCharge.addActionListener(e -> addServiceToBill());

        cmbBillingType.addActionListener(e -> toggleInsuranceFields());

        btnProcess.addActionListener(e -> startBillingProcess());

    }

    private void findPatientByNic() {
        String nic = txtSerchByNIC.getText().trim();
        if (nic.isEmpty()) {
            return;
        }

        Optional<Patient> patientOpt = patientDAO.findById(1); 

        if (patientOpt.isPresent()) {
            this.currentPatient = patientOpt.get();
            txtPatientID.setText(String.valueOf(currentPatient.getId()));
            txtPatientName.setText(currentPatient.getName());
        } else {
            JOptionPane.showMessageDialog(this, "Patient not found for NIC: " + nic, "Not Found", JOptionPane.WARNING_MESSAGE);
            txtPatientID.setText("");
            txtPatientName.setText("");
            this.currentPatient = null;
        }
    }

    private void addServiceToBill() {
        ServiceCategory category = (ServiceCategory) cmbServiceCategory.getSelectedItem();
        String details = txtServiceDetails.getText().trim();
        String chargeStr = txtServiceAmount.getText().trim();

        if (category == null) {
            JOptionPane.showMessageDialog(this, "Please select a service category.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (chargeStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the service charge.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double charge;
        try {
            charge = Double.parseDouble(chargeStr);
            if (charge <= 0) {
                JOptionPane.showMessageDialog(this, "Charge must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for the charge.", "Invalid Number", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ServiceItem newItem = new ServiceItem(category, details, charge);
        serviceItems.add(newItem);

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.addRow(new Object[]{newItem.getCategory().toString(), newItem.getDetails(), newItem.getCharge()});

        updateTotalAmount();

        txtServiceDetails.setText("");
        txtServiceAmount.setText("");
        cmbServiceCategory.requestFocus(); 
    }

    private void updateTotalAmount() {
        double total = serviceItems.stream()
                .mapToDouble(ServiceItem::getCharge)
                .sum();

        String formattedTotal = String.format("%.2f", total);

        txtAmount.setText(formattedTotal);

        lblTotal.setText("Total: Rs. " + formattedTotal);
    }

    private void toggleInsuranceFields() {
        BillingType type = (BillingType) cmbBillingType.getSelectedItem();
        // pnlInsuranceDetails.setVisible(type == BillingType.INSURANCE);
    }

    private void startBillingProcess() {
        if (currentPatient == null || serviceItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please find a patient and add at least one service charge.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        pnlProcessUI.removeAll();
        handlerPanels.clear();
        pnlProcessUI.setLayout(new BoxLayout(pnlProcessUI, BoxLayout.Y_AXIS));
        pnlProcessUI.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        BillingClaim claim = new BillingClaim();
        claim.setPatient(currentPatient);
        claim.setBillingType((BillingType) cmbBillingType.getSelectedItem());
        claim.setAmount(Double.parseDouble(txtAmount.getText()));
        if (claim.getBillingType() == BillingType.INSURANCE) {
            claim.setInsuranceProvider(txtInsuranceCompany.getText());
            claim.setPolicyNumber(txtPolicyNumber.getText());
        }

        AbstractBillingHandler chain;
        if (claim.getBillingType() == BillingType.INSURANCE) {
            chain = BillingChainBuilder.buildInsuranceChain();
        } else {
            chain = BillingChainBuilder.buildDirectChain();
        }
//        AbstractBillingHandler chain = BillingChainBuilder.buildChain(claim.getBillingType());

        AbstractBillingHandler current = chain;
        while (current != null) {
            HandlerPanel hp = new HandlerPanel(current.handlerName, "");
            handlerPanels.add(hp);
            pnlProcessUI.add(hp);
            pnlProcessUI.add(Box.createRigidArea(new Dimension(0, 5)));

            current = current.getNextHandler();
        }

        layeredBillDetailsPane.removeAll();
        layeredBillDetailsPane.add(pnlProcessUI);
        layeredBillDetailsPane.repaint();
        layeredBillDetailsPane.revalidate();

        btnProcess.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                chain.setStatusListener(BillingNInsuranceClaimsPanel.this);
                chain.handleRequest(claim);
                return null;
            }

            @Override
            protected void done() {
                btnProcess.setEnabled(true);
            }
        };
        worker.execute();
    }

    // --- HandlerStatusListener Methods ---
    @Override
    public void onHandlerStarted(String handlerName) {
        findHandlerPanelByName(handlerName).ifPresent(HandlerPanel::setProcessing);
    }

    @Override
    public void onHandlerCompleted(String handlerName, boolean success, String message) {
        findHandlerPanelByName(handlerName).ifPresent(panel -> panel.setCompleted(success, message));
    }

    @Override
    public void onProcessingComplete(BillingClaim claim) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("PROCESS: Saving final claim details to the database...");
            billingClaimDAO.saveOrUpdate(claim); 
            System.out.println("PROCESS: Save complete.");

            JOptionPane.showMessageDialog(this,
                    "Billing Process Completed!\nStatus: " + claim.getStatus()
                    + "\nPatient Pays: " + String.format("%.2f", claim.getResult().getPatientResponsibility()),
                    "Process Finished",
                    JOptionPane.INFORMATION_MESSAGE);            
//             layeredBillDetailsPane.removeAll();
//             layeredBillDetailsPane.add(pnlServiceDetails);

            resetForm();
        });
    }

    private Optional<HandlerPanel> findHandlerPanelByName(String name) {
        return handlerPanels.stream()
                .filter(panel -> panel.getHandlerName().equals(name))
                .findFirst();
    }

    private void resetForm() {
        layeredBillDetailsPane.removeAll();
        layeredBillDetailsPane.add(pnlServiceDetails);
        layeredBillDetailsPane.repaint();
        layeredBillDetailsPane.revalidate();

        txtPatientID.setText("");
        txtPatientName.setText("");
        txtAmount.setText("0.00");
        if (cmbBillingType.getItemCount() > 0) {
            cmbBillingType.setSelectedIndex(0);
        }
        txtInsuranceCompany.setText("");
        txtPolicyNumber.setText("");

        txtSerchByNIC.setText("");

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        serviceItems.clear();

        lblTotal.setText("Rs. 0.00");
        this.currentPatient = null;

        System.out.println("UI has been reset for the next transaction.");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtPatientID = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtPatientName = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtAmount = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        btnProcess = new javax.swing.JButton();
        cmbBillingType = new javax.swing.JComboBox<>();
        pnlInsuranceDetails = new javax.swing.JPanel();
        txtPolicyNumber = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtInsuranceCompany = new javax.swing.JTextField();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel16 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        mainLayerdPane = new javax.swing.JPanel();
        layeredBillDetailsPane = new javax.swing.JLayeredPane();
        pnlServiceDetails = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        txtSerchByNIC = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cmbServiceCategory = new javax.swing.JComboBox<>();
        txtServiceDetails = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtServiceAmount = new javax.swing.JTextField();
        btnAddServiceCharge = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        pnlProcessUI = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(202, 240, 248));
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 626));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Billing Information");

        jLabel10.setText("Patient ID :");

        jLabel11.setText("Name :");

        jLabel12.setText("Amount :");

        jLabel13.setText("Billing Type :");

        btnProcess.setText("Start Billing Process");

        cmbBillingType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbBillingTypeActionPerformed(evt);
            }
        });

        pnlInsuranceDetails.setBackground(new java.awt.Color(202, 240, 248));

        jLabel15.setText("Policy Number :");

        jLabel14.setText("Insurance Company :");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("Insurance Details");

        javax.swing.GroupLayout pnlInsuranceDetailsLayout = new javax.swing.GroupLayout(pnlInsuranceDetails);
        pnlInsuranceDetails.setLayout(pnlInsuranceDetailsLayout);
        pnlInsuranceDetailsLayout.setHorizontalGroup(
            pnlInsuranceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlInsuranceDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlInsuranceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlInsuranceDetailsLayout.createSequentialGroup()
                        .addGroup(pnlInsuranceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlInsuranceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtInsuranceCompany, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                            .addComponent(txtPolicyNumber))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlInsuranceDetailsLayout.createSequentialGroup()
                        .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlInsuranceDetailsLayout.setVerticalGroup(
            pnlInsuranceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlInsuranceDetailsLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlInsuranceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlInsuranceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtInsuranceCompany, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlInsuranceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPolicyNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtPatientName, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtAmount, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbBillingType, javax.swing.GroupLayout.Alignment.LEADING, 0, 290, Short.MAX_VALUE)
                            .addComponent(txtPatientID))))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnProcess, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlInsuranceDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPatientID, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPatientName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbBillingType, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlInsuranceDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 147, Short.MAX_VALUE)
                .addComponent(btnProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        add(jPanel1, java.awt.BorderLayout.LINE_END);

        mainLayerdPane.setBackground(new java.awt.Color(153, 153, 255));

        layeredBillDetailsPane.setLayout(new java.awt.CardLayout());

        pnlServiceDetails.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Billing & Services");

        jLabel3.setText("Patient NIC");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Service Category", "Service Details", "Charge"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setRowHeight(40);
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Add Services");

        jLabel5.setText("Service Category");

        jLabel6.setText("Service Details");

        jLabel7.setText("Charge");

        btnAddServiceCharge.setText("Add New Service Charge");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setText("Total:");

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTotal.setText("Rs. 0.00");

        javax.swing.GroupLayout pnlServiceDetailsLayout = new javax.swing.GroupLayout(pnlServiceDetails);
        pnlServiceDetails.setLayout(pnlServiceDetailsLayout);
        pnlServiceDetailsLayout.setHorizontalGroup(
            pnlServiceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlServiceDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlServiceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1)
                    .addComponent(jLabel1)
                    .addGroup(pnlServiceDetailsLayout.createSequentialGroup()
                        .addGroup(pnlServiceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlServiceDetailsLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(txtSerchByNIC, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlServiceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbServiceCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnlServiceDetailsLayout.createSequentialGroup()
                                .addGroup(pnlServiceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel7))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(txtServiceDetails)
                            .addComponent(txtServiceAmount)
                            .addComponent(btnAddServiceCharge, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlServiceDetailsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlServiceDetailsLayout.setVerticalGroup(
            pnlServiceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlServiceDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlServiceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlServiceDetailsLayout.createSequentialGroup()
                        .addGroup(pnlServiceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSerchByNIC, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1))
                    .addGroup(pnlServiceDetailsLayout.createSequentialGroup()
                        .addGroup(pnlServiceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlServiceDetailsLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbServiceCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtServiceDetails, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtServiceAmount, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnAddServiceCharge, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 501, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlServiceDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );

        layeredBillDetailsPane.add(pnlServiceDetails, "card2");

        pnlProcessUI.setBackground(new java.awt.Color(255, 255, 255));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("Processing Chain");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlProcessUILayout = new javax.swing.GroupLayout(pnlProcessUI);
        pnlProcessUI.setLayout(pnlProcessUILayout);
        pnlProcessUILayout.setHorizontalGroup(
            pnlProcessUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProcessUILayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProcessUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlProcessUILayout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlProcessUILayout.setVerticalGroup(
            pnlProcessUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProcessUILayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        layeredBillDetailsPane.add(pnlProcessUI, "card3");

        javax.swing.GroupLayout mainLayerdPaneLayout = new javax.swing.GroupLayout(mainLayerdPane);
        mainLayerdPane.setLayout(mainLayerdPaneLayout);
        mainLayerdPaneLayout.setHorizontalGroup(
            mainLayerdPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(layeredBillDetailsPane)
        );
        mainLayerdPaneLayout.setVerticalGroup(
            mainLayerdPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(layeredBillDetailsPane)
        );

        add(mainLayerdPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbBillingTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBillingTypeActionPerformed
        
        BillingType selectedType = (BillingType) cmbBillingType.getSelectedItem();

        boolean isInsurance = (selectedType == BillingType.INSURANCE);
        pnlInsuranceDetails.setVisible(isInsurance);

    }//GEN-LAST:event_cmbBillingTypeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddServiceCharge;
    private javax.swing.JButton btnProcess;
    private javax.swing.JComboBox<BillingType> cmbBillingType;
    private javax.swing.JComboBox<ServiceCategory> cmbServiceCategory;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTable jTable1;
    private javax.swing.JLayeredPane layeredBillDetailsPane;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel mainLayerdPane;
    private javax.swing.JPanel pnlInsuranceDetails;
    private javax.swing.JPanel pnlProcessUI;
    private javax.swing.JPanel pnlServiceDetails;
    private javax.swing.JTextField txtAmount;
    private javax.swing.JTextField txtInsuranceCompany;
    private javax.swing.JTextField txtPatientID;
    private javax.swing.JTextField txtPatientName;
    private javax.swing.JTextField txtPolicyNumber;
    private javax.swing.JTextField txtSerchByNIC;
    private javax.swing.JTextField txtServiceAmount;
    private javax.swing.JTextField txtServiceDetails;
    // End of variables declaration//GEN-END:variables
}
