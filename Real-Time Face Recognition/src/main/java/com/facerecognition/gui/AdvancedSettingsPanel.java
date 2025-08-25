package com.facerecognition.gui;

import com.facerecognition.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Advanced Settings Panel for the Face Recognition System
 * Allows real-time adjustment of recognition parameters
 * 
 * @author Face Recognition System
 * @version 2.0
 */
public class AdvancedSettingsPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedSettingsPanel.class);
    
    private final ConfigurationManager config = ConfigurationManager.getInstance();
    private final FaceRecognitionGUI parentGui;
    
    // Setting controls
    private JSlider thresholdSlider;
    private JLabel thresholdValueLabel;
    private JCheckBox strictModeCheckbox;
    private JCheckBox ultraPrecisionCheckbox;
    private JCheckBox antiSpoofingCheckbox;
    private JCheckBox livenessCheckbox;
    private JCheckBox qualityCheckbox;
    private JCheckBox fastModeCheckbox;
    
    // Performance controls
    private JSlider fpsSlider;
    private JLabel fpsValueLabel;
    private JComboBox<String> cameraResolutionCombo;
    
    // Status indicators
    private JLabel performanceLabel;
    private JProgressBar memoryUsageBar;
    
    public AdvancedSettingsPanel(FaceRecognitionGUI parentGui) {
        this.parentGui = parentGui;
        initializeComponents();
        setupLayout();
        loadCurrentSettings();
    }
    
    private void initializeComponents() {
        // Recognition threshold
        thresholdSlider = new JSlider(0, 100, 75);
        thresholdSlider.setMajorTickSpacing(10);
        thresholdSlider.setMinorTickSpacing(5);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setPaintLabels(true);
        thresholdSlider.addChangeListener(e -> updateThresholdValue());
        
        thresholdValueLabel = new JLabel("0.75");
        
        // Feature checkboxes
        strictModeCheckbox = new JCheckBox("Strict Mode");
        strictModeCheckbox.addActionListener(this::onSettingChanged);
        
        ultraPrecisionCheckbox = new JCheckBox("Ultra Precision");
        ultraPrecisionCheckbox.addActionListener(this::onSettingChanged);
        
        antiSpoofingCheckbox = new JCheckBox("Anti-Spoofing");
        antiSpoofingCheckbox.addActionListener(this::onSettingChanged);
        
        livenessCheckbox = new JCheckBox("Liveness Detection");
        livenessCheckbox.addActionListener(this::onSettingChanged);
        
        qualityCheckbox = new JCheckBox("Quality Check");
        qualityCheckbox.addActionListener(this::onSettingChanged);
        
        fastModeCheckbox = new JCheckBox("Fast Mode");
        fastModeCheckbox.addActionListener(this::onSettingChanged);
        
        // Performance controls
        fpsSlider = new JSlider(10, 60, 30);
        fpsSlider.setMajorTickSpacing(10);
        fpsSlider.setMinorTickSpacing(5);
        fpsSlider.setPaintTicks(true);
        fpsSlider.setPaintLabels(true);
        fpsSlider.addChangeListener(e -> updateFpsValue());
        
        fpsValueLabel = new JLabel("30 FPS");
        
        String[] resolutions = {"640x480", "800x600", "1024x768", "1280x720"};
        cameraResolutionCombo = new JComboBox<>(resolutions);
        cameraResolutionCombo.addActionListener(this::onSettingChanged);
        
        // Status indicators
        performanceLabel = new JLabel("Performance: Good");
        memoryUsageBar = new JProgressBar(0, 100);
        memoryUsageBar.setStringPainted(true);
        memoryUsageBar.setString("Memory: 45%");
        
        // Start memory monitoring
        startMemoryMonitoring();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Advanced Settings"));
        
        // Main tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Recognition settings tab
        JPanel recognitionPanel = createRecognitionPanel();
        tabbedPane.addTab("Recognition", recognitionPanel);
        
        // Performance settings tab
        JPanel performancePanel = createPerformancePanel();
        tabbedPane.addTab("Performance", performancePanel);
        
        // Features settings tab
        JPanel featuresPanel = createFeaturesPanel();
        tabbedPane.addTab("Features", featuresPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status panel at bottom
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createRecognitionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Threshold setting
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Recognition Threshold:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(thresholdSlider, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(thresholdValueLabel, gbc);
        
        // Mode checkboxes
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(strictModeCheckbox, gbc);
        
        gbc.gridy = 2;
        panel.add(ultraPrecisionCheckbox, gbc);
        
        return panel;
    }
    
    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // FPS setting
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Frame Rate:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(fpsSlider, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(fpsValueLabel, gbc);
        
        // Resolution setting
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Camera Resolution:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(cameraResolutionCombo, gbc);
        
        // Fast mode
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(fastModeCheckbox, gbc);
        
        return panel;
    }
    
    private JPanel createFeaturesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(antiSpoofingCheckbox, gbc);
        
        gbc.gridy = 1;
        panel.add(livenessCheckbox, gbc);
        
        gbc.gridy = 2;
        panel.add(qualityCheckbox, gbc);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(new TitledBorder("System Status"));
        
        panel.add(performanceLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(new JLabel("Memory Usage:"));
        panel.add(memoryUsageBar);
        
        return panel;
    }
    
    private void loadCurrentSettings() {
        // Load current values from configuration
        double threshold = config.getDouble("recognition.threshold", 0.75);
        thresholdSlider.setValue((int)(threshold * 100));
        updateThresholdValue();
        
        strictModeCheckbox.setSelected(config.getBoolean("recognition.strict.mode", false));
        ultraPrecisionCheckbox.setSelected(config.getBoolean("recognition.ultra.precision", false));
        antiSpoofingCheckbox.setSelected(config.getBoolean("features.anti.spoofing.enabled", true));
        livenessCheckbox.setSelected(config.getBoolean("features.liveness.detection", true));
        qualityCheckbox.setSelected(config.getBoolean("features.face.quality.check", true));
        fastModeCheckbox.setSelected(config.getBoolean("recognition.fast.mode", true));
        
        int fps = config.getInt("camera.frame.rate", 30);
        fpsSlider.setValue(fps);
        updateFpsValue();
        
        // Set resolution
        int width = config.getInt("camera.width", 640);
        int height = config.getInt("camera.height", 480);
        cameraResolutionCombo.setSelectedItem(width + "x" + height);
    }
    
    private void updateThresholdValue() {
        double value = thresholdSlider.getValue() / 100.0;
        thresholdValueLabel.setText(String.format("%.2f", value));
        
        // Update configuration
        config.setProperty("recognition.threshold", String.valueOf(value));
        logger.info("Recognition threshold updated to: {}", value);
    }
    
    private void updateFpsValue() {
        int fps = fpsSlider.getValue();
        fpsValueLabel.setText(fps + " FPS");
        
        // Update configuration
        config.setProperty("camera.frame.rate", String.valueOf(fps));
        logger.info("Frame rate updated to: {} FPS", fps);
    }
    
    private void onSettingChanged(ActionEvent e) {
        Object source = e.getSource();
        
        if (source instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) source;
            String configKey = getConfigKeyForCheckbox(checkBox);
            
            if (configKey != null) {
                config.setProperty(configKey, String.valueOf(checkBox.isSelected()));
                logger.info("Setting {} updated to: {}", configKey, checkBox.isSelected());
            }
        }
        // Handle other component types if needed in the future
        
        // Handle resolution change
        if (e.getSource() == cameraResolutionCombo) {
            String resolution = (String) cameraResolutionCombo.getSelectedItem();
            String[] parts = resolution.split("x");
            config.setProperty("camera.width", parts[0]);
            config.setProperty("camera.height", parts[1]);
            logger.info("Camera resolution updated to: {}", resolution);
        }
    }
    
    private String getConfigKeyForCheckbox(JCheckBox checkbox) {
        if (checkbox == strictModeCheckbox) return "recognition.strict.mode";
        if (checkbox == ultraPrecisionCheckbox) return "recognition.ultra.precision";
        if (checkbox == antiSpoofingCheckbox) return "features.anti.spoofing.enabled";
        if (checkbox == livenessCheckbox) return "features.liveness.detection";
        if (checkbox == qualityCheckbox) return "features.face.quality.check";
        if (checkbox == fastModeCheckbox) return "recognition.fast.mode";
        return null;
    }
    
    private void startMemoryMonitoring() {
        Timer memoryTimer = new Timer(2000, e -> updateMemoryUsage());
        memoryTimer.start();
    }
    
    private void updateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        int usagePercent = (int) ((usedMemory * 100) / totalMemory);
        memoryUsageBar.setValue(usagePercent);
        memoryUsageBar.setString("Memory: " + usagePercent + "%");
        
        // Update performance status
        if (usagePercent < 60) {
            performanceLabel.setText("Performance: Excellent");
            performanceLabel.setForeground(Color.GREEN);
        } else if (usagePercent < 80) {
            performanceLabel.setText("Performance: Good");
            performanceLabel.setForeground(Color.ORANGE);
        } else {
            performanceLabel.setText("Performance: Poor");
            performanceLabel.setForeground(Color.RED);
        }
    }
}
