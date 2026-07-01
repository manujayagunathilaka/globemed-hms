/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.jiat.oodp.globemed.hms.gui.billing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author Manujaya
 */
public class HandlerPanel extends JPanel {

    private final JLabel statusIconLabel;
    private final JLabel handlerNameLabel;
    private final JLabel descriptionLabel;
    private final JLabel detailsLabel;
    private final JProgressBar progressBar;
    private final Color defaultColor;

    // Modern color scheme
    private static final Color BACKGROUND_DEFAULT = new Color(248, 249, 250);
    private static final Color BACKGROUND_PROCESSING = new Color(59, 130, 246, 30);
    private static final Color BACKGROUND_SUCCESS = new Color(34, 197, 94, 30);
    private static final Color BACKGROUND_ERROR = new Color(239, 68, 68, 30);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);

    private Color currentBackgroundColor;
    private Timer animationTimer;
    private float animationProgress = 0f;
    private Color targetColor;

    public HandlerPanel(String name, String description) {
        currentBackgroundColor = BACKGROUND_DEFAULT;
        defaultColor = BACKGROUND_DEFAULT;
        targetColor = BACKGROUND_DEFAULT;

        statusIconLabel = new JLabel("⏳", SwingConstants.CENTER);
        statusIconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        statusIconLabel.setPreferredSize(new Dimension(45, 45));
        statusIconLabel.setForeground(TEXT_SECONDARY);

        // Modern icon container with rounded background
        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw circular background
                g2.setColor(new Color(241, 245, 249));
                g2.fillOval(5, 5, 35, 35);

                super.paintComponent(g);
                g2.dispose();
            }
        };
        iconContainer.setLayout(new BorderLayout());
        iconContainer.add(statusIconLabel, BorderLayout.CENTER);
        iconContainer.setOpaque(false);
        iconContainer.setPreferredSize(new Dimension(45, 45));

        handlerNameLabel = new JLabel(name);
        handlerNameLabel.setFont(new Font("Inter", Font.BOLD, 15));
        handlerNameLabel.setForeground(TEXT_PRIMARY);

        descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        descriptionLabel.setForeground(TEXT_SECONDARY);

        detailsLabel = new JLabel("Waiting to process...");
        detailsLabel.setFont(new Font("Inter", Font.PLAIN, 11));
        detailsLabel.setForeground(TEXT_SECONDARY);

        // Modern progress bar with smooth animation
        progressBar = new JProgressBar() {
            private Timer animationTimer;
            private float animationPosition = 0f;

            {
                // Smooth animation timer
                animationTimer = new Timer(16, e -> {
                    animationPosition += 0.02f;
                    if (animationPosition > 1f) {
                        animationPosition = -0.3f; // Reset with overlap for continuous effect
                    }
                    repaint();
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Background
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Animated progress bar
                if (isIndeterminate() && isVisible()) {
                    int barWidth = (int) (getWidth() * 0.35);
                    int x = (int) ((getWidth() + barWidth) * animationPosition - barWidth);

                    // Gradient effect for smoother appearance
                    g2.setColor(new Color(ACCENT_BLUE.getRed(), ACCENT_BLUE.getGreen(),
                            ACCENT_BLUE.getBlue(), 180));
                    g2.fillRoundRect(x, 0, barWidth, getHeight(), 8, 8);

                    // Highlight effect
                    g2.setColor(ACCENT_BLUE);
                    g2.fillRoundRect(x + 2, 1, barWidth - 4, getHeight() - 2, 6, 6);
                }

                g2.dispose();
            }

            @Override
            public void setVisible(boolean visible) {
                super.setVisible(visible);
                if (visible && isIndeterminate()) {
                    animationTimer.start();
                } else {
                    animationTimer.stop();
                }
            }
        };
        progressBar.setPreferredSize(new Dimension(0, 6));
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setBorderPainted(false);
        progressBar.setOpaque(false);

        setLayout(new BorderLayout(15, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setOpaque(false); 

        JPanel headerPanel = new JPanel(new BorderLayout(0, 2));
        headerPanel.setOpaque(false);
        headerPanel.add(handlerNameLabel, BorderLayout.NORTH);
        headerPanel.add(descriptionLabel, BorderLayout.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 8));
        contentPanel.setOpaque(false);
        contentPanel.add(detailsLabel, BorderLayout.NORTH);
        contentPanel.add(progressBar, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.SOUTH);

        add(iconContainer, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        animationTimer = new Timer(16, e -> {
            animationProgress += 0.1f;
            if (animationProgress >= 1f) {
                animationProgress = 1f;
                animationTimer.stop();
            }

            // Interpolate colors
            int r = (int) (currentBackgroundColor.getRed()
                    + (targetColor.getRed() - currentBackgroundColor.getRed()) * animationProgress);
            int g = (int) (currentBackgroundColor.getGreen()
                    + (targetColor.getGreen() - currentBackgroundColor.getGreen()) * animationProgress);
            int b = (int) (currentBackgroundColor.getBlue()
                    + (targetColor.getBlue() - currentBackgroundColor.getBlue()) * animationProgress);
            int a = (int) (currentBackgroundColor.getAlpha()
                    + (targetColor.getAlpha() - currentBackgroundColor.getAlpha()) * animationProgress);

            currentBackgroundColor = new Color(r, g, b, a);
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rounded background with shadow effect
        g2.setColor(new Color(0, 0, 0, 5));
        g2.fill(new RoundRectangle2D.Float(2, 2, getWidth() - 2, getHeight() - 2, 12, 12));

        // Main background
        g2.setColor(currentBackgroundColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

        // Border
        g2.setColor(BORDER_COLOR);
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));

        g2.dispose();
        super.paintComponent(g);
    }

    private void animateToColor(Color newColor) {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }

        targetColor = newColor;
        animationProgress = 0f;
        animationTimer.start();
    }

    public String getHandlerName() {
        return this.handlerNameLabel.getText();
    }

    public void resetState() {
        SwingUtilities.invokeLater(() -> {
            statusIconLabel.setText("⏳");
            statusIconLabel.setForeground(TEXT_SECONDARY);
            statusIconLabel.setVisible(true);
            progressBar.setVisible(false);
            detailsLabel.setText("Waiting to process...");
            detailsLabel.setForeground(TEXT_SECONDARY);
            animateToColor(defaultColor);
        });
    }

    public void setProcessing() {
        SwingUtilities.invokeLater(() -> {
            statusIconLabel.setVisible(false);
            progressBar.setVisible(true);
            detailsLabel.setText("Processing...");
            detailsLabel.setForeground(ACCENT_BLUE);
            animateToColor(BACKGROUND_PROCESSING);
        });
    }

    public void setCompleted(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(false);
            statusIconLabel.setVisible(true);
            if (success) {
                statusIconLabel.setText("✅");
                statusIconLabel.setForeground(new Color(34, 197, 94));
                detailsLabel.setForeground(new Color(21, 128, 61));
                animateToColor(BACKGROUND_SUCCESS);
            } else {
                statusIconLabel.setText("❌");
                statusIconLabel.setForeground(new Color(239, 68, 68));
                detailsLabel.setForeground(new Color(185, 28, 28));
                animateToColor(BACKGROUND_ERROR);
            }
            detailsLabel.setText(message);
        });
    }
}
