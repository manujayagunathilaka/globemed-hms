/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui.permisson;

import java.awt.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import lk.jiat.oodp.globemed.hms.model.dao.DepartmentDAO;
import lk.jiat.oodp.globemed.hms.model.dao.StaffDAO;
import lk.jiat.oodp.globemed.hms.model.entity.Department;
import lk.jiat.oodp.globemed.hms.model.entity.Staff;
import lk.jiat.oodp.globemed.hms.model.entity.User;
import lk.jiat.oodp.globemed.hms.model.enums.AppPermission;
import lk.jiat.oodp.globemed.hms.permissons.PermissionService;

/**
 *
 * @author Manujaya
 */
public class PermissionsPanel extends javax.swing.JPanel {

    private final DepartmentDAO departmentDAO;
    private final StaffDAO staffDAO;
    private final PermissionService permissionService;
    
    private TitledBorder permissionsBorder;
    
    public PermissionsPanel() {
        initComponents(); 

        this.permissionsBorder = BorderFactory.createTitledBorder("Permissions for: (nothing selected)");

        checklistScrollPane.setBorder(this.permissionsBorder);
        
        this.departmentDAO = new DepartmentDAO();
        this.staffDAO = new StaffDAO();
        this.permissionService = new PermissionService();

//        buildOrganizationTree();

        setupPanel();
    }

    private void setupPanel() {
        buildOrganizationTree();

        organizationTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) organizationTree.getLastSelectedPathComponent();

            if (selectedNode == null) {
                txtName.setText("");
                txtType.setText("");
                clearPermissionsView();
                return;
            }

            Object selectedObject = selectedNode.getUserObject();
            String name = "";
            String type = "";

            if (selectedObject instanceof Department) {
                Department dept = (Department) selectedObject;
                name = dept.getName();
                type = "Department";
            } else if (selectedObject instanceof Staff) {
                Staff staff = (Staff) selectedObject;
                name = staff.getName();
                type = staff.getUser().getUserRole().toString(); 
            } else if (selectedObject instanceof String) {
                name = selectedObject.toString();
                type = "Organization";
            }

            txtName.setText(name);
            txtType.setText(type);

            displayPermissionsFor(selectedObject);
        });

        btnSaveChanges.addActionListener(e -> savePermissions());
    }
    
    private void buildOrganizationTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("GlobeMed Hospital");

        List<Department> rootDepartments = departmentDAO.findRootDepartments();
        for (Department dept : rootDepartments) {
            DefaultMutableTreeNode deptNode = new DefaultMutableTreeNode(dept);
            root.add(deptNode);
            addOrganizationNodes(deptNode, dept);
        }

        organizationTree.setModel(new DefaultTreeModel(root));
    }

    private void addOrganizationNodes(DefaultMutableTreeNode parentNode, Department parentDept) {
        // Sub-departments 
        for (Department subDept : parentDept.getSubDepartments()) {
            DefaultMutableTreeNode deptNode = new DefaultMutableTreeNode(subDept);
            parentNode.add(deptNode);
            addOrganizationNodes(deptNode, subDept); // Recursion
        }

        // Staff 
        List<Staff> staffMembers = staffDAO.findByDepartment(parentDept);
        for (Staff staff : staffMembers) {
            parentNode.add(new DefaultMutableTreeNode(staff));
        }
    }

    private void displayPermissionsFor(Object selectedObject) {
        pnlPermissionsChecklist.removeAll();

        User userToCheck = null;
        if (selectedObject instanceof Staff) {
            userToCheck = ((Staff) selectedObject).getUser();
        }

        for (AppPermission permission : AppPermission.values()) {
            JCheckBox chk = new JCheckBox(permission.name());

            if (userToCheck != null) {
                if (permissionService.checkPermission(userToCheck, permission)) {
                    chk.setSelected(true);
                }
            } 
            else if (selectedObject instanceof Department) {
                if (((Department) selectedObject).getPermissions().contains(permission)) {
                    chk.setSelected(true);
                }
            }

            pnlPermissionsChecklist.add(chk);
        }

        pnlPermissionsChecklist.revalidate();
        pnlPermissionsChecklist.repaint();
    }

    private void savePermissions() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) organizationTree.getLastSelectedPathComponent();
        if (selectedNode == null || selectedNode.getUserObject() instanceof String) {
            JOptionPane.showMessageDialog(this, "Please select a Department or a Staff member to save permissions.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object selectedObject = selectedNode.getUserObject();

        Set<AppPermission> selectedPermissions = new HashSet<>();
        for (Component comp : pnlPermissionsChecklist.getComponents()) {
            if (comp instanceof JCheckBox) {
                JCheckBox chk = (JCheckBox) comp;
                if (chk.isSelected()) {
                    selectedPermissions.add(AppPermission.valueOf(chk.getText()));
                }
            }
        }

        permissionService.savePermissionsForObject(selectedObject, selectedPermissions);

        JOptionPane.showMessageDialog(this, "Permissions updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        buildOrganizationTree();
    }

    private void clearPermissionsView() {
        permissionsBorder.setTitle("Permissions for: (nothing selected)");
        pnlPermissionsChecklist.removeAll();
        pnlPermissionsChecklist.revalidate();
        pnlPermissionsChecklist.repaint();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        organizationTree = new javax.swing.JTree();
        btnRegisterNewUserFrame = new javax.swing.JButton();
        pnlDetailsMain = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtType = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        checklistScrollPane = new javax.swing.JScrollPane();
        pnlPermissionsChecklist = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btnSaveChanges = new javax.swing.JButton();
        btnResetPermissions = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(300);

        jPanel2.setLayout(new java.awt.BorderLayout(0, 10));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("  Hospital Organizational Structure");
        jPanel2.add(jLabel1, java.awt.BorderLayout.PAGE_START);

        organizationTree.setRowHeight(30);
        organizationTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                organizationTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(organizationTree);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        btnRegisterNewUserFrame.setText("Register New User");
        btnRegisterNewUserFrame.setMaximumSize(new java.awt.Dimension(156, 40));
        btnRegisterNewUserFrame.setMinimumSize(new java.awt.Dimension(156, 40));
        btnRegisterNewUserFrame.setPreferredSize(new java.awt.Dimension(156, 40));
        btnRegisterNewUserFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterNewUserFrameActionPerformed(evt);
            }
        });
        jPanel2.add(btnRegisterNewUserFrame, java.awt.BorderLayout.PAGE_END);

        jSplitPane1.setLeftComponent(jPanel2);

        pnlDetailsMain.setLayout(new java.awt.BorderLayout(10, 0));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Selection Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N
        jPanel4.setMinimumSize(new java.awt.Dimension(100, 150));
        jPanel4.setPreferredSize(new java.awt.Dimension(618, 150));

        jLabel2.setText("Name :");

        jLabel3.setText("Type :");

        txtName.setEditable(false);

        txtType.setEditable(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtType)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtType, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pnlDetailsMain.add(jPanel4, java.awt.BorderLayout.PAGE_START);

        jPanel5.setLayout(new java.awt.BorderLayout());

        checklistScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Grant / Revoke Permissions", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        pnlPermissionsChecklist.setLayout(new java.awt.GridLayout(0, 1));
        checklistScrollPane.setViewportView(pnlPermissionsChecklist);

        jPanel5.add(checklistScrollPane, java.awt.BorderLayout.CENTER);

        pnlDetailsMain.add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel1.setPreferredSize(new java.awt.Dimension(618, 50));

        btnSaveChanges.setText("Save Changes");

        btnResetPermissions.setText("Reset");
        btnResetPermissions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetPermissionsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(308, Short.MAX_VALUE)
                .addComponent(btnResetPermissions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSaveChanges)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 10, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnResetPermissions, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveChanges, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pnlDetailsMain.add(jPanel1, java.awt.BorderLayout.PAGE_END);

        jSplitPane1.setRightComponent(pnlDetailsMain);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnResetPermissionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetPermissionsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnResetPermissionsActionPerformed

    private void organizationTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_organizationTreeValueChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_organizationTreeValueChanged

    private void btnRegisterNewUserFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterNewUserFrameActionPerformed

        UserRegistrationFrame registrationFrame = new UserRegistrationFrame();
        registrationFrame.setVisible(true);

    }//GEN-LAST:event_btnRegisterNewUserFrameActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRegisterNewUserFrame;
    private javax.swing.JButton btnResetPermissions;
    private javax.swing.JButton btnSaveChanges;
    private javax.swing.JScrollPane checklistScrollPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTree organizationTree;
    private javax.swing.JPanel pnlDetailsMain;
    private javax.swing.JPanel pnlPermissionsChecklist;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtType;
    // End of variables declaration//GEN-END:variables
}
