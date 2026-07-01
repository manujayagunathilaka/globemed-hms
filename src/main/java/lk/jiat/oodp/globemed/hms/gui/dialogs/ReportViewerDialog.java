/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Manujaya
 */
public class ReportViewerDialog extends JDialog {

    private final String reportContent; 
        JEditorPane editorPane = new JEditorPane();

    public ReportViewerDialog(Frame owner, String title, String reportContent) {
        super(owner, title, true);
        this.reportContent = reportContent;

        editorPane.setEditable(false);
        editorPane.setContentType("text/html");

        String htmlContent = "<html><body style='font-family: sans-serif; padding: 15px;'>"
                + reportContent.replace("\n", "<br>")
                + "</body></html>";
        editorPane.setText(htmlContent);
        editorPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(editorPane);

        JButton btnSave = new JButton("Save as File...");
        btnSave.addActionListener(e -> saveReportToFile());

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> this.dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnSave); 
        bottomPanel.add(btnClose);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setSize(700, 600);
        setLocationRelativeTo(owner);
    }

    private void saveReportToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
        fileChooser.setFileFilter(new FileNameExtensionFilter("HTML Files (*.html)", "html"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".html")) {
                fileToSave = new File(filePath + ".html");
            }

            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(editorPane.getText()); 
                JOptionPane.showMessageDialog(this, "Report saved successfully to:\n" + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
