/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui;

/**
 *
 * @author Manujaya
 */
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import javax.swing.*;
import javax.swing.UIManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import lk.jiat.oodp.globemed.hms.util.DatabaseSeeder;

public class GlobeMedSplashScreen extends JWindow {

    private Timer timer;
    private int progress = 0;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel versionLabel;
    private CurvedPanel mainPanel;
    private Font titleFont;
    private Font subtitleFont;
    private Font statusFont;
    private Font versionFont;

    // Animation variables
    private float logoAlpha = 0.0f;
    private float textAlpha = 0.0f;
    private float panelAlpha = 0.0f;
    private int pulseDirection = 1;
    private float pulseAlpha = 0.5f;
    private float rotationAngle = 0.0f;
    private int waveOffset = 0;

    // Logo image
    private Image logoImage;

    public GlobeMedSplashScreen() {
        loadLogo();
        initializeComponents();
        setupUI();
        startAnimation();
    }

    private void loadLogo() {
        try {
            logoImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/globemed.png"));
            logoImage = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/globemed.png"));
        } catch (Exception e) {
            logoImage = null; 
            System.out.println("Logo file not found. Using default medical cross.");
        }
    }

    private void initializeComponents() {
        // Load fonts
        try {
            titleFont = new Font("Segoe UI Light", Font.BOLD, 42);
            subtitleFont = new Font("Segoe UI", Font.PLAIN, 18);
            statusFont = new Font("Segoe UI", Font.PLAIN, 13);
            versionFont = new Font("Segoe UI", Font.PLAIN, 11);
        } catch (Exception e) {
            titleFont = new Font("Arial", Font.BOLD, 42);
            subtitleFont = new Font("Arial", Font.PLAIN, 18);
            statusFont = new Font("Arial", Font.PLAIN, 13);
            versionFont = new Font("Arial", Font.PLAIN, 11);
        }

        // Create curved main panel
        mainPanel = new CurvedPanel();

        // Title label with premium styling
        titleLabel = new JLabel("GlobeMed", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Set composite for fade-in effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));

                String text = getText();
                FontMetrics fm = g2d.getFontMetrics(titleFont);
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                // Draw multiple text shadows for depth
                g2d.setFont(titleFont);
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.drawString(text, x + 3, y + 3);
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.drawString(text, x + 6, y + 6);

                // Draw main text with gradient
                GradientPaint textGradient = new GradientPaint(
                        x, y - fm.getAscent(), new Color(255, 255, 255),
                        x, y, new Color(240, 240, 240)
                );
                g2d.setPaint(textGradient);
                g2d.drawString(text, x, y);

                g2d.dispose();
            }
        };
        titleLabel.setOpaque(false);

        // Subtitle with elegant styling
        subtitleLabel = new JLabel("Hospital Management System", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha * 0.85f));

                g2d.setFont(subtitleFont);
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                // Text shadow
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.drawString(text, x + 1, y + 1);

                // Main text
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.drawString(text, x, y);

                g2d.dispose();
            }
        };
        subtitleLabel.setOpaque(false);

        // Version label
        versionLabel = new JLabel("v2.0.1 Professional Edition", SwingConstants.CENTER);
        versionLabel.setFont(versionFont);
        versionLabel.setForeground(new Color(255, 255, 255, 150));
        versionLabel.setOpaque(false);

        // Custom curved progress bar
        progressBar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Draw background with rounded corners
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.fillRoundRect(0, 0, width, height, height, height);

                // Draw inner shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(1, 1, width - 2, height - 2, height - 2, height - 2);

                // Calculate progress width
                int progressWidth = (int) ((double) getValue() / getMaximum() * (width - 4));

                if (progressWidth > 0) {
                    // Create gradient for progress
                    GradientPaint progressGradient = new GradientPaint(
                            0, 0, new Color(52, 152, 219),
                            progressWidth, 0, new Color(41, 128, 185)
                    );
                    g2d.setPaint(progressGradient);
                    g2d.fillRoundRect(2, 2, progressWidth, height - 4, height - 4, height - 4);

                    // Add highlight
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                    GradientPaint highlight = new GradientPaint(
                            0, 2, new Color(255, 255, 255, 100),
                            0, height / 2, new Color(255, 255, 255, 0)
                    );
                    g2d.setPaint(highlight);
                    g2d.fillRoundRect(2, 2, progressWidth, height / 2 - 1, height - 4, height - 4);
                }

                g2d.dispose();
            }
        };
        progressBar.setStringPainted(false);
        progressBar.setOpaque(false);
        progressBar.setBorderPainted(false);

        // Status label with modern styling
        statusLabel = new JLabel("Initializing system...", SwingConstants.CENTER);
        statusLabel.setFont(statusFont);
        statusLabel.setForeground(new Color(255, 255, 255, 180));
        statusLabel.setOpaque(false);
    }

    // Custom curved panel class
    private class CurvedPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            // Enable high-quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

            int width = getWidth();
            int height = getHeight();

            // Draw animated background
            drawAnimatedBackground(g2d, width, height);

            // Draw main curved panel with glass effect
            drawCurvedPanel(g2d, width, height);

            // Draw animated medical logo
            drawPremiumLogo(g2d, width, height);

            g2d.dispose();
        }

        private void drawAnimatedBackground(Graphics2D g2d, int width, int height) {
            // Blue theme animated gradient background - keeping the wave design
            float colorShift = (float) Math.sin(System.currentTimeMillis() * 0.001) * 0.1f + 0.9f;

            // Professional blue color palette
            Color color1 = new Color((int) (25 * colorShift), (int) (118 * colorShift), (int) (210 * colorShift)); // Deep Blue
            Color color2 = new Color((int) (52 * colorShift), (int) (152 * colorShift), (int) (219 * colorShift)); // Ocean Blue  
            Color color3 = new Color((int) (41 * colorShift), (int) (128 * colorShift), (int) (185 * colorShift)); // Professional Blue

            // Keep the original gradient style but with blue colors
            GradientPaint gradient1 = new GradientPaint(0, 0, color1, width / 2, height / 2, color2);
            GradientPaint gradient2 = new GradientPaint(width / 2, height / 2, color2, width, height, color3);

            g2d.setPaint(gradient1);
            g2d.fillRect(0, 0, width / 2, height);
            g2d.setPaint(gradient2);
            g2d.fillRect(width / 2, 0, width / 2, height);

            // Keep the original wave style
            drawAnimatedWaves(g2d, width, height);

            // Keep the original floating particles style
            drawFloatingParticles(g2d, width, height);
        }

        private void drawAnimatedWaves(Graphics2D g2d, int width, int height) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));

            // Keep original wave style but with subtle blue tinting
            for (int i = 0; i < 3; i++) {
                Path2D wave = new Path2D.Double();
                wave.moveTo(0, height / 2 + i * 30);

                for (int x = 0; x <= width; x += 10) {
                    double y = height / 2 + i * 30 + Math.sin((x + waveOffset + i * 50) * 0.02) * 20;
                    wave.lineTo(x, y);
                }

                wave.lineTo(width, height);
                wave.lineTo(0, height);
                wave.closePath();

                // Mix of white and subtle blue waves for elegant look
                if (i % 2 == 0) {
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(new Color(200, 230, 255, 100)); // Very light blue
                }
                g2d.fill(wave);
            }
        }

        private void drawFloatingParticles(Graphics2D g2d, int width, int height) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));

            long time = System.currentTimeMillis();
            for (int i = 0; i < 25; i++) {
                double x = (Math.sin(time * 0.001 + i) * 100 + width / 2 + i * 15) % width;
                double y = (Math.cos(time * 0.0015 + i) * 80 + height / 2 + i * 10) % height;
                int size = (int) (Math.sin(time * 0.003 + i) * 3 + 5);

                // Mix of white and light blue particles
                if (i % 3 == 0) {
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(new Color(220, 240, 255, 120)); // Very light blue
                }

                Ellipse2D particle = new Ellipse2D.Double(x, y, size, size);
                g2d.fill(particle);
            }
        }

        private void drawCurvedPanel(Graphics2D g2d, int width, int height) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, panelAlpha * 0.15f));

            // Create curved panel shape
            int panelWidth = width - 80;
            int panelHeight = height - 60;
            int panelX = 40;
            int panelY = 30;
            int cornerRadius = 25;

            RoundRectangle2D panelShape = new RoundRectangle2D.Double(
                    panelX, panelY, panelWidth, panelHeight, cornerRadius, cornerRadius
            );

            // Keep original glass effect but with subtle blue tint
            g2d.setColor(new Color(255, 255, 255, 40));
            g2d.fill(panelShape);

            // Draw panel border with subtle blue accent
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, panelAlpha * 0.3f));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.setColor(new Color(200, 230, 255, 100)); // Very light blue border
            g2d.draw(panelShape);

            // Draw inner glow - keep original but with light blue tint
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, panelAlpha * 0.2f));
            GradientPaint innerGlow = new GradientPaint(
                    panelX, panelY, new Color(240, 248, 255, 60), // Very light blue
                    panelX, panelY + 50, new Color(240, 248, 255, 0)
            );
            g2d.setPaint(innerGlow);
            g2d.fill(panelShape);
        }

        private void drawPremiumLogo(Graphics2D g2d, int width, int height) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoAlpha));

            int centerX = width / 2;
            int centerY = 100;
            int logoSize = 50;

            // Draw rotating outer ring with blue theme
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoAlpha * 0.6f));
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.setColor(new Color(135, 206, 235, 150)); // Sky Blue

            AffineTransform oldTransform = g2d.getTransform();
            g2d.rotate(rotationAngle, centerX, centerY);
            g2d.drawOval(centerX - logoSize - 18, centerY - logoSize - 18,
                    (logoSize + 18) * 2, (logoSize + 18) * 2);
            g2d.setTransform(oldTransform);

            // Draw pulsing background circle with blue glow
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulseAlpha * logoAlpha * 0.4f));
            RadialGradientPaint radialGradient = new RadialGradientPaint(
                    centerX, centerY, logoSize + 25,
                    new float[]{0.0f, 0.6f, 1.0f},
                    new Color[]{
                        new Color(173, 216, 230, 120), // Light Blue
                        new Color(135, 206, 235, 80), // Sky Blue
                        new Color(135, 206, 235, 0) // Transparent
                    }
            );
            g2d.setPaint(radialGradient);
            g2d.fillOval(centerX - logoSize - 25, centerY - logoSize - 25,
                    (logoSize + 25) * 2, (logoSize + 25) * 2);

            // Draw custom logo if available, otherwise draw medical cross
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoAlpha));

            if (logoImage != null && logoImage.getWidth(null) > 0) {
                // Draw custom logo with blue tint overlay
                int logoWidth = logoSize * 2;
                int logoHeight = logoSize * 2;

                // Draw logo shadow
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoAlpha * 0.3f));
                g2d.drawImage(logoImage, centerX - logoWidth / 2 + 3, centerY - logoHeight / 2 + 3,
                        logoWidth, logoHeight, null);

                // Draw main logo
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoAlpha));
                g2d.drawImage(logoImage, centerX - logoWidth / 2, centerY - logoHeight / 2,
                        logoWidth, logoHeight, null);

                // Add blue overlay for theme consistency
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoAlpha * 0.15f));
                g2d.setColor(new Color(52, 152, 219, 100));
                g2d.fillOval(centerX - logoWidth / 2, centerY - logoHeight / 2, logoWidth, logoHeight);

            } else {
                // Draw default medical cross with blue theme
                drawBlueMedicalCross(g2d, centerX, centerY, logoSize);
            }
        }

        private void drawBlueMedicalCross(Graphics2D g2d, int centerX, int centerY, int size) {
            int crossWidth = size / 3;

            // Create cross shapes
            RoundRectangle2D verticalBar = new RoundRectangle2D.Double(
                    centerX - crossWidth / 2, centerY - size, crossWidth, size * 2, 8, 8
            );
            RoundRectangle2D horizontalBar = new RoundRectangle2D.Double(
                    centerX - size, centerY - crossWidth / 2, size * 2, crossWidth, 8, 8
            );

            // Draw cross shadow
            g2d.setColor(new Color(0, 0, 0, 60));
            AffineTransform shadowTransform = AffineTransform.getTranslateInstance(2, 2);
            g2d.fill(shadowTransform.createTransformedShape(verticalBar));
            g2d.fill(shadowTransform.createTransformedShape(horizontalBar));

            // Draw cross with blue gradient
            GradientPaint crossGradient = new GradientPaint(
                    centerX, centerY - size, new Color(52, 152, 219), // Professional Blue
                    centerX, centerY + size, new Color(41, 128, 185) // Darker Blue
            );
            g2d.setPaint(crossGradient);
            g2d.fill(verticalBar);
            g2d.fill(horizontalBar);

            // Draw cross highlight
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoAlpha * 0.7f));
            GradientPaint highlight = new GradientPaint(
                    centerX, centerY - size, new Color(173, 216, 230, 150), // Light Blue highlight
                    centerX, centerY - size / 2, new Color(173, 216, 230, 0)
            );
            g2d.setPaint(highlight);
            g2d.fill(verticalBar);
            g2d.fill(horizontalBar);
        }

        private void drawPremiumMedicalCross(Graphics2D g2d, int centerX, int centerY, int size) {
            // This method is kept for backward compatibility
            drawBlueMedicalCross(g2d, centerX, centerY, size);
        }
    }

    private void setupUI() {
        setSize(600, 400);
        setLocationRelativeTo(null);

        mainPanel.setLayout(null);
        mainPanel.setOpaque(false);

        // Position components with premium spacing
        titleLabel.setBounds(100, 160, 400, 60);
        subtitleLabel.setBounds(100, 220, 400, 30);
        versionLabel.setBounds(100, 250, 400, 20);
        progressBar.setBounds(120, 310, 360, 12);
        statusLabel.setBounds(100, 335, 400, 25);

        mainPanel.add(titleLabel);
        mainPanel.add(subtitleLabel);
        mainPanel.add(versionLabel);
        mainPanel.add(progressBar);
        mainPanel.add(statusLabel);

        add(mainPanel);
        setVisible(true);
    }

    private void startAnimation() {
        timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAnimation();
                repaint();

                if (progress >= 100) {
                    timer.stop();
                    SwingUtilities.invokeLater(() -> {
                        showLoginWindow();
                        dispose();
                    });
                }
            }
        });
        timer.start();
    }

    private void updateAnimation() {
        progress++;

        // Panel fade-in animation
        if (progress <= 25) {
            panelAlpha = progress / 25.0f;
        } else {
            panelAlpha = 1.0f;
        }

        // Logo fade-in animation
        if (progress >= 10 && progress <= 40) {
            logoAlpha = (progress - 10) / 30.0f;
        } else if (progress > 40) {
            logoAlpha = 1.0f;
        }

        // Text fade-in animation
        if (progress >= 30 && progress <= 60) {
            textAlpha = (progress - 30) / 30.0f;
        } else if (progress > 60) {
            textAlpha = 1.0f;
        }

        // Pulsing effect
        pulseAlpha += pulseDirection * 0.015f;
        if (pulseAlpha >= 0.9f) {
            pulseDirection = -1;
        } else if (pulseAlpha <= 0.3f) {
            pulseDirection = 1;
        }

        // Rotation animation
        rotationAngle += 0.02f;

        // Wave animation
        waveOffset += 2;

        // Update progress bar with smooth animation
        progressBar.setValue(progress);

        // Update status messages
        if (progress < 15) {
            statusLabel.setText("Initializing core modules...");
        } else if (progress < 30) {
            statusLabel.setText("Loading medical database...");
        } else if (progress < 45) {
            statusLabel.setText("Configuring user interface...");
        } else if (progress < 60) {
            statusLabel.setText("Establishing secure connections...");
        } else if (progress < 75) {
            statusLabel.setText("Loading patient management system...");
        } else if (progress < 90) {
            statusLabel.setText("Preparing workspace...");
        } else if (progress < 98) {
            statusLabel.setText("Finalizing setup...");
        } else {
            statusLabel.setText("Launch ready!");
        }
    }

    private void showLoginWindow() {
        // Launch the Login window after splash screen completes
        try {
            new LogIn().setVisible(true);
        } catch (Exception e) {
            // If LogIn class is not available, show error message
            JOptionPane.showMessageDialog(null,
                    "Login module not found. Please ensure LogIn.java is available in the classpath.",
                    "Module Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static void main(String args[]) {
        FlatMacLightLaf.setup();

        try {
            DatabaseSeeder.seedMasterData();

        } catch (Exception e) {
            System.err.println("Database seeding failed!");
            e.printStackTrace();
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame.setDefaultLookAndFeelDecorated(true);
                FlatMacLightLaf.setup();
                UIManager.put("PasswordField.showRevealButton", true);

//                forceAccentColor();
                System.setProperty("flatlaf.popupDropShadowPainted", "true");
                ThemeManager.applyTheme();
                
                new GlobeMedSplashScreen();
            }
        });
    }
    
    private static void forceAccentColor() {
        Color accentColor = new Color(66, 193, 238); // Red accent color

        // Set all possible accent color properties
        UIManager.put("Component.accentColor", accentColor);
        UIManager.put("@accentColor", accentColor);
        UIManager.put("Component.focusColor", accentColor);
        UIManager.put("Component.focusedBorderColor", accentColor);

        // TextField and PasswordField focus colors
        UIManager.put("TextField.focusedBorderColor", accentColor);
        UIManager.put("PasswordField.focusedBorderColor", accentColor);
        UIManager.put("Component.focusWidth", 2);

        // Other components
        UIManager.put("CheckBox.icon.checkmarkColor", accentColor);
        UIManager.put("CheckBox.icon.focusedBorderColor", accentColor);
        UIManager.put("RadioButton.icon.centerColor", accentColor);
        UIManager.put("ProgressBar.foreground", accentColor);

        UIManager.put("PasswordField.showRevealButton", true);
        
        // Debug output
//        System.out.println("Accent color set to: " + accentColor);
//        System.out.println("Component.accentColor: " + UIManager.get("Component.accentColor"));
//        System.out.println("@accentColor: " + UIManager.get("@accentColor"));
    }
    
}
