/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf; // හෝ ඔබට කැමති base theme එකක්
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author Manujaya
 */
public class ThemeManager {

    // Backgrounds
    private static final Color PRIMARY_BACKGROUND = Color.WHITE;
    private static final Color SECONDARY_BACKGROUND = new Color(0xF8F9FA); // Very light gray for headers/panels

    // Primary Theme Colors (Blue)
    private static final Color PRIMARY_BLUE_ACCENT = new Color(0x007BFF); // Classic, professional blue
    private static final Color HOVER_BLUE = new Color(61, 189, 236);         // Darker blue for mouse hover/press
    private static final Color LIGHT_BLUE_HIGHLIGHT = new Color(0xCCE5FF); // Light blue for selections

    // Text Colors
    private static final Color PRIMARY_TEXT = new Color(0x212529);       // Dark charcoal
    private static final Color SECONDARY_TEXT = new Color(0x6C757D);     // Medium gray for labels
    private static final Color TEXT_ON_BLUE_BG = Color.WHITE;             // White text for blue backgrounds

    // Borders and Lines
    private static final Color BORDERS_LINES = new Color(0xDEE2E6);      // Light gray for subtle borders

    public static void applyTheme() {
        try {
            FlatIntelliJLaf.setup();

            // ---- Global UI Improvements ----
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 10);

            // ---- General Component Colors ----
            UIManager.put("Panel.background", PRIMARY_BACKGROUND);
            UIManager.put("Window.background", PRIMARY_BACKGROUND);
            UIManager.put("Component.background", PRIMARY_BACKGROUND);
            UIManager.put("Label.foreground", PRIMARY_TEXT);
            UIManager.put("textText", PRIMARY_TEXT);
            UIManager.put("textInactiveText", SECONDARY_TEXT);
            UIManager.put("Component.borderColor", BORDERS_LINES);
            UIManager.put("Component.separatorColor", BORDERS_LINES);

            // ---- Accent & Focus ----
            UIManager.put("Component.accentColor", PRIMARY_BLUE_ACCENT);
            UIManager.put("Component.focusColor", PRIMARY_BLUE_ACCENT);
            UIManager.put("Component.focusedBorderColor", PRIMARY_BLUE_ACCENT);
            UIManager.put("Component.focusWidth", 1);

            // ---- Buttons ----
            UIManager.put("Button.background", PRIMARY_BLUE_ACCENT);
            UIManager.put("Button.foreground", TEXT_ON_BLUE_BG);
            UIManager.put("Button.hoverBackground", HOVER_BLUE);
            UIManager.put("Button.pressedBackground", HOVER_BLUE.darker());
            UIManager.put("Button.default.background", PRIMARY_BLUE_ACCENT.darker()); // Make default button slightly darker
            UIManager.put("Button.default.foreground", TEXT_ON_BLUE_BG);

            // ---- Text Fields and Text Areas ----
            UIManager.put("TextField.background", PRIMARY_BACKGROUND);
            UIManager.put("PasswordField.background", PRIMARY_BACKGROUND);
            UIManager.put("TextArea.background", PRIMARY_BACKGROUND);
            UIManager.put("TextComponent.borderColor", BORDERS_LINES);

            // ---- JTable (වගු) සඳහා ----
            UIManager.put("Table.background", PRIMARY_BACKGROUND);
            UIManager.put("Table.foreground", PRIMARY_TEXT);
            UIManager.put("Table.selectionBackground", LIGHT_BLUE_HIGHLIGHT); // **Selected Row Color**
            UIManager.put("Table.selectionForeground", PRIMARY_TEXT); // Text on selected row
            UIManager.put("Table.gridColor", BORDERS_LINES);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("TableHeader.background", SECONDARY_BACKGROUND); // **Title Row Background**
            UIManager.put("TableHeader.foreground", PRIMARY_TEXT); // **Title Row Text Color**
            UIManager.put("TableHeader.bottomSeparatorColor", BORDERS_LINES);
            UIManager.put("TableHeader.height", 32);

//            UIManager.put("TitlePane.background", new Color(0, 123, 255)); // නිල් පාට
//
//            UIManager.put("TitlePane.foreground", Color.WHITE);
//
//            UIManager.put("TitlePane.inactiveBackground", new Color(248, 249, 250)); // ලා අළු පාට
//
//            UIManager.put("TitlePane.inactiveForeground", new Color(108, 117, 125));
            
            // ---- JComboBox (ඩ්‍රොප්-ඩවුන්) සඳහා ----
            UIManager.put("ComboBox.background", PRIMARY_BACKGROUND);
            UIManager.put("ComboBox.foreground", PRIMARY_TEXT);
            UIManager.put("ComboBox.buttonBackground", PRIMARY_BACKGROUND);
            UIManager.put("ComboBox.buttonArrow.color", SECONDARY_TEXT); // **Dropdown Arrow Color**
            UIManager.put("ComboBox.buttonHoverArrow.color", PRIMARY_TEXT);
            UIManager.put("ComboBox.popup.background", PRIMARY_BACKGROUND); // Dropdown list background
            UIManager.put("ComboBox.popup.selectionBackground", LIGHT_BLUE_HIGHLIGHT); // Selected item in list
            UIManager.put("ComboBox.popup.selectionForeground", PRIMARY_TEXT);

            // ---- Radio Buttons & Check Boxes ----
            UIManager.put("RadioButton.icon.selectedColor", PRIMARY_BLUE_ACCENT);
            UIManager.put("CheckBox.icon.selectedColor", PRIMARY_BLUE_ACCENT);
            UIManager.put("CheckBox.icon.checkmarkColor", TEXT_ON_BLUE_BG); // White checkmark on blue square

            // ---- Other Components ----
            UIManager.put("ProgressBar.foreground", PRIMARY_BLUE_ACCENT);
            UIManager.put("TabbedPane.underlineColor", PRIMARY_BLUE_ACCENT);
            UIManager.put("Slider.thumb.color", PRIMARY_BLUE_ACCENT);
            UIManager.put("Slider.track.color", BORDERS_LINES);
            UIManager.put("ScrollPane.background", PRIMARY_BACKGROUND);
            UIManager.put("TitledBorder.titleColor", SECONDARY_TEXT);

            FlatLaf.updateUI();

        } catch (Exception ex) {
            System.err.println("Failed to initialize Oceanic Blue Theme");
            ex.printStackTrace();
        }
    }
    
    public static void setIcon(JFrame frame) {
        URL iconURL = ThemeManager.class.getResource("/images/globemed-ICON.png");

        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            frame.setIconImage(icon.getImage());
        } else {
            System.err.println("Application icon not found at '/images/globemed-ICON.png'");
        }
    }
    
}
