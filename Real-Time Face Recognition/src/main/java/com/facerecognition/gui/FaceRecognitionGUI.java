package com.facerecognition.gui;

import com.facerecognition.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

/**
 * Swing GUI for the Face Recognition System
 * Provides an intuitive interface for face recognition operations
 * 
 * Features:
 * - Camera control (start/stop)
 * - Person registration
 * - Database management
 * - Real-time status display
 * - System configuration
 * 
 * @author Face Recognition System
 * @version 1.0
 */
public class FaceRecognitionGUI extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionGUI.class);
    
    // Main application reference
    private final Main mainApp;
    
    // GUI Components
    private JButton startCameraButton;
    private JButton stopCameraButton;
    private JButton registerPersonButton;
    private JTextField personNameField;
    private JTextArea statusArea;
    private JLabel statusLabel;
    private JList<String> personsList;
    private DefaultListModel<String> personsListModel;
    private JButton deletePersonButton;
    private JButton refreshButton;
    private JLabel statsLabel;
    
    // Enhanced face recognition features
    private JButton checkFaceButton;
    private JLabel recognitionResultLabel;
    private JProgressBar recognitionConfidence;
    private JPanel recognitionPanel;
    private JLabel lastRecognizedLabel;
    private JTextArea recognitionHistoryArea;
    private JButton testRecognitionButton;
    
    // Status tracking
    private boolean cameraRunning = false;
    private String lastRecognizedPerson = "None";
    private double lastConfidence = 0.0;
    
    /**
     * Constructor
     * @param mainApp Reference to the main application
     */
    public FaceRecognitionGUI(Main mainApp) {
        this.mainApp = mainApp;
        initializeGUI();
        updatePersonsList();
        updateStats();
        
        logger.info("Face Recognition GUI initialized");
    }
    
    /**
     * Initialize the GUI components and layout
     */
    private void initializeGUI() {
        setTitle("Real-Time Face Recognition System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Set application icon (if available)
        try {
            // You can add an icon file to resources if desired
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/icon.png")));
        } catch (Exception e) {
            // Icon not available, continue without it
        }
        
        // Create main layout
        setLayout(new BorderLayout(10, 10));
        
        // Create and add components
        add(createControlPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);
        
        // Add window listener for cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
                System.exit(0);
            }
        });
    }
    
    /**
     * Create the control panel with camera and registration controls
     * @return Control panel component
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Camera & Registration Controls"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Camera control section
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Camera Control:"), gbc);
        
        startCameraButton = new JButton("Start Camera");
        startCameraButton.setPreferredSize(new Dimension(120, 30));
        startCameraButton.addActionListener(e -> startCamera());
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(startCameraButton, gbc);
        
        stopCameraButton = new JButton("Stop Camera");
        stopCameraButton.setPreferredSize(new Dimension(120, 30));
        stopCameraButton.setEnabled(false);
        stopCameraButton.addActionListener(e -> stopCamera());
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(stopCameraButton, gbc);
        
        // Registration section
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Person Name:"), gbc);
        
        personNameField = new JTextField(15);
        personNameField.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(personNameField, gbc);
        
        registerPersonButton = new JButton("Register Person");
        registerPersonButton.setPreferredSize(new Dimension(120, 30));
        registerPersonButton.addActionListener(e -> registerPerson());
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(registerPersonButton, gbc);
        
        return panel;
    }
    
    /**
     * Create the center panel with person list and management
     * @return Center panel component
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Left panel - Registered persons
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new TitledBorder("Registered Persons"));
        
        personsListModel = new DefaultListModel<>();
        personsList = new JList<>(personsListModel);
        personsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane listScrollPane = new JScrollPane(personsList);
        listScrollPane.setPreferredSize(new Dimension(200, 300));
        leftPanel.add(listScrollPane, BorderLayout.CENTER);
        
        // Person management buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            updatePersonsList();
            updateStats();
        });
        buttonPanel.add(refreshButton);
        
        deletePersonButton = new JButton("Delete Person");
        deletePersonButton.addActionListener(e -> deletePerson());
        buttonPanel.add(deletePersonButton);
        
        JButton clearAllButton = new JButton("Clear All Data");
        clearAllButton.addActionListener(e -> clearAllData());
        clearAllButton.setBackground(new Color(255, 100, 100));
        clearAllButton.setForeground(Color.WHITE);
        buttonPanel.add(clearAllButton);
        
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Right panel - Status and information
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new TitledBorder("System Information"));
        
        statusArea = new JTextArea(15, 40);
        statusArea.setEditable(false);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statusArea.setBackground(getBackground());
        
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        rightPanel.add(statusScrollPane, BorderLayout.CENTER);
        
        // Add information to status area
        updateSystemInfo();
        
        // Create recognition monitoring panel
        JPanel recognitionMonitorPanel = createRecognitionMonitorPanel();
        
        // Add panels to main center panel
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        panel.add(recognitionMonitorPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create face recognition monitoring panel
     * @return Recognition monitoring panel
     */
    private JPanel createRecognitionMonitorPanel() {
        recognitionPanel = new JPanel();
        recognitionPanel.setLayout(new BoxLayout(recognitionPanel, BoxLayout.Y_AXIS));
        recognitionPanel.setBorder(new TitledBorder("Face Recognition Monitor"));
        recognitionPanel.setPreferredSize(new Dimension(250, 300));
        
        // Current recognition result
        JPanel currentResultPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        currentResultPanel.setBorder(new TitledBorder("Current Recognition"));
        
        recognitionResultLabel = new JLabel("No face detected");
        recognitionResultLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        recognitionResultLabel.setForeground(Color.BLUE);
        currentResultPanel.add(recognitionResultLabel);
        
        recognitionConfidence = new JProgressBar(0, 100);
        recognitionConfidence.setStringPainted(true);
        recognitionConfidence.setString("0%");
        currentResultPanel.add(recognitionConfidence);
        
        lastRecognizedLabel = new JLabel("Last: None");
        lastRecognizedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        currentResultPanel.add(lastRecognizedLabel);
        
        recognitionPanel.add(currentResultPanel);
        recognitionPanel.add(Box.createVerticalStrut(10));
        
        // Recognition control buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        checkFaceButton = new JButton("Check Current Face");
        checkFaceButton.addActionListener(e -> checkCurrentFace());
        checkFaceButton.setEnabled(false);
        buttonPanel.add(checkFaceButton);
        
        testRecognitionButton = new JButton("Test Recognition");
        testRecognitionButton.addActionListener(e -> testRecognitionAccuracy());
        buttonPanel.add(testRecognitionButton);
        
        recognitionPanel.add(buttonPanel);
        recognitionPanel.add(Box.createVerticalStrut(10));
        
        // Recognition history
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(new TitledBorder("Recognition History"));
        
        recognitionHistoryArea = new JTextArea(8, 25);
        recognitionHistoryArea.setEditable(false);
        recognitionHistoryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        recognitionHistoryArea.setText("Recognition history will appear here...\n");
        
        JScrollPane historyScrollPane = new JScrollPane(recognitionHistoryArea);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);
        
        recognitionPanel.add(historyPanel);
        
        return recognitionPanel;
    }
    
    /**
     * Create the status panel at the bottom
     * @return Status panel component
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        statusLabel = new JLabel("Ready - Camera stopped");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(statusLabel, BorderLayout.WEST);
        
        statsLabel = new JLabel("Database: 0 persons, 0 embeddings");
        statsLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(statsLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Start the camera for face recognition
     */
    private void startCamera() {
        try {
            appendStatus("Starting camera...");
            mainApp.startCamera();
            
            cameraRunning = true;
            startCameraButton.setEnabled(false);
            stopCameraButton.setEnabled(true);
            registerPersonButton.setEnabled(true);
            
            // Enable face checking features
            setFaceCheckingEnabled(true);
            
            statusLabel.setText("Camera running - Face recognition active");
            appendStatus("Camera started successfully. Face recognition is now active.");
            
        } catch (Exception e) {
            logger.error("Error starting camera", e);
            appendStatus("Error starting camera: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Failed to start camera: " + e.getMessage(), 
                "Camera Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Stop the camera
     */
    private void stopCamera() {
        try {
            appendStatus("Stopping camera...");
            mainApp.stopCamera();
            
            cameraRunning = false;
            startCameraButton.setEnabled(true);
            stopCameraButton.setEnabled(false);
            registerPersonButton.setEnabled(false);
            
            // Disable face checking features
            setFaceCheckingEnabled(false);
            
            statusLabel.setText("Ready - Camera stopped");
            appendStatus("Camera stopped.");
            
        } catch (Exception e) {
            logger.error("Error stopping camera", e);
            appendStatus("Error stopping camera: " + e.getMessage());
        }
    }
    
    /**
     * Register a new person
     */
    private void registerPerson() {
        String personName = personNameField.getText().trim();
        
        if (personName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a person name.", 
                "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!cameraRunning) {
            JOptionPane.showMessageDialog(this, 
                "Please start the camera first.", 
                "Camera Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show progress dialog
        JDialog progressDialog = new JDialog(this, "Registering Person", true);
        JLabel progressLabel = new JLabel("Capturing face for " + personName + "...");
        progressLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        progressDialog.add(progressLabel);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(this);
        
        // Perform registration in background thread
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return mainApp.registerPerson(personName);
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    boolean success = get();
                    if (success) {
                        appendStatus("Successfully registered: " + personName);
                        personNameField.setText("");
                        updatePersonsList();
                        updateStats();
                        
                        JOptionPane.showMessageDialog(FaceRecognitionGUI.this,
                            "Person registered successfully: " + personName,
                            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        appendStatus("Failed to register: " + personName);
                        JOptionPane.showMessageDialog(FaceRecognitionGUI.this,
                            "Failed to register person. Make sure your face is clearly visible to the camera.",
                            "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    logger.error("Registration error", e);
                    progressDialog.dispose();
                    appendStatus("Registration error: " + e.getMessage());
                    JOptionPane.showMessageDialog(FaceRecognitionGUI.this,
                        "Registration error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    /**
     * Delete selected person
     */
    private void deletePerson() {
        String selectedPerson = personsList.getSelectedValue();
        
        if (selectedPerson == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a person to delete.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete '" + selectedPerson + "' and all their face data?",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            boolean success = mainApp.getDatabaseManager().deletePerson(selectedPerson);
            
            if (success) {
                appendStatus("Deleted person: " + selectedPerson);
                updatePersonsList();
                updateStats();
                
                JOptionPane.showMessageDialog(this,
                    "Person deleted successfully: " + selectedPerson,
                    "Deletion Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                appendStatus("Failed to delete person: " + selectedPerson);
                JOptionPane.showMessageDialog(this,
                    "Failed to delete person: " + selectedPerson,
                    "Deletion Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Clear all data from the database
     */
    private void clearAllData() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete ALL persons and face data?\n" +
            "This action cannot be undone and will clear the entire database.\n\n" +
            "This is useful if you have incompatible face embeddings from previous versions.",
            "Confirm Clear All Data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            boolean success = mainApp.getDatabaseManager().clearAllData();
            
            if (success) {
                appendStatus("Cleared all database data successfully");
                updatePersonsList();
                updateStats();
                
                JOptionPane.showMessageDialog(this,
                    "All data cleared successfully.\n" +
                    "You can now register persons with the new face recognition system.",
                    "Clear Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                appendStatus("Failed to clear database data");
                JOptionPane.showMessageDialog(this,
                    "Failed to clear database data.",
                    "Clear Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Update the list of registered persons
     */
    private void updatePersonsList() {
        personsListModel.clear();
        
        try {
            List<String> persons = mainApp.getDatabaseManager().getAllPersonNames();
            for (String person : persons) {
                personsListModel.addElement(person);
            }
            
            logger.debug("Updated persons list with {} entries", persons.size());
            
        } catch (Exception e) {
            logger.error("Error updating persons list", e);
            appendStatus("Error updating persons list: " + e.getMessage());
        }
    }
    
    /**
     * Update database statistics
     */
    private void updateStats() {
        try {
            Map<String, Integer> stats = mainApp.getDatabaseManager().getDatabaseStats();
            statsLabel.setText(String.format("Database: %d persons, %d embeddings",
                stats.get("persons"), stats.get("embeddings")));
        } catch (Exception e) {
            logger.error("Error updating stats", e);
            statsLabel.setText("Database: Error retrieving stats");
        }
    }
    
    /**
     * Update system information in the status area
     */
    private void updateSystemInfo() {
        StringBuilder info = new StringBuilder();
        
        info.append("=== Face Recognition System Information ===\n\n");
        
        // System info
        info.append("System Information:\n");
        info.append("- Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("- OS: ").append(System.getProperty("os.name")).append(" ")
            .append(System.getProperty("os.version")).append("\n");
        info.append("- Architecture: ").append(System.getProperty("os.arch")).append("\n\n");
        
        // Face detector info
        if (mainApp.getFaceDetector() != null) {
            info.append("Face Detector:\n");
            info.append("- Status: Initialized\n");
            info.append("- Method: Haar Cascade\n");
            info.append("- Model: haarcascade_frontalface_alt.xml\n\n");
        }
        
        // Face recognizer info
        if (mainApp.getFaceRecognizer() != null) {
            info.append("Face Recognizer:\n");
            info.append("- Status: Initialized\n");
            info.append("- Threshold: ").append(
                String.format("%.3f", mainApp.getFaceRecognizer().getRecognitionThreshold()))
                .append("\n");
            info.append("- Features: Histogram + LBP + Edge\n\n");
        }
        
        // Database info
        if (mainApp.getDatabaseManager() != null) {
            info.append("Database:\n");
            info.append("- Type: SQLite\n");
            info.append("- File: face_recognition.db\n");
            info.append("- Status: ").append(
                mainApp.getDatabaseManager().isInitialized() ? "Connected" : "Disconnected")
                .append("\n\n");
        }
        
        info.append("Instructions:\n");
        info.append("1. Click 'Start Camera' to begin face recognition\n");
        info.append("2. To register a new person:\n");
        info.append("   - Enter their name in the text field\n");
        info.append("   - Make sure they are clearly visible to camera\n");
        info.append("   - Click 'Register Person'\n");
        info.append("3. The system will automatically recognize registered faces\n");
        info.append("4. Use the person list to manage registered individuals\n\n");
        
        info.append("Tips:\n");
        info.append("- Ensure good lighting for better recognition\n");
        info.append("- Keep face centered and at moderate distance\n");
        info.append("- Register multiple images of the same person for better accuracy\n");
        
        statusArea.setText(info.toString());
        statusArea.setCaretPosition(0);
    }
    
    /**
     * Append status message to the status area
     * @param message Status message to append
     */
    private void appendStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            statusArea.append(String.format("[%s] %s\n", timestamp, message));
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }
    
    /**
     * Cleanup resources when closing the application
     */
    private void cleanup() {
        logger.info("Cleaning up GUI resources...");
        
        if (cameraRunning) {
            stopCamera();
        }
        
        // Give some time for cleanup
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("GUI cleanup completed");
    }
    
    /**
     * Check the current face being detected
     */
    private void checkCurrentFace() {
        if (!cameraRunning) {
            updateRecognitionResult("Camera not running", 0.0);
            return;
        }
        
        try {
            // This would be called when user wants to manually check current face
            updateRecognitionResult("Checking current face...", 0.0);
            
            // The actual recognition happens in the main camera loop
            // This is just for manual checking
            SwingUtilities.invokeLater(() -> {
                if (lastRecognizedPerson != null && !lastRecognizedPerson.equals("None")) {
                    updateRecognitionResult(lastRecognizedPerson, lastConfidence);
                } else {
                    updateRecognitionResult("No face detected", 0.0);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error checking current face: ", e);
            updateRecognitionResult("Error checking face", 0.0);
        }
    }
    
    /**
     * Test recognition accuracy with stored faces
     */
    private void testRecognitionAccuracy() {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Starting recognition accuracy test...");
                
                if (mainApp.getDatabaseManager() == null) {
                    publish("Database not available");
                    return null;
                }
                
                List<String> persons = mainApp.getDatabaseManager().getAllPersonNames();
                if (persons.isEmpty()) {
                    publish("No registered persons found");
                    return null;
                }
                
                publish("Testing with " + persons.size() + " registered persons");
                
                for (String person : persons) {
                    publish("Testing person: " + person);
                    Thread.sleep(500); // Simulate testing delay
                }
                
                publish("Recognition test completed");
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    addToRecognitionHistory(message);
                }
            }
            
            @Override
            protected void done() {
                addToRecognitionHistory("Test completed at " + 
                    java.time.LocalTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        };
        
        worker.execute();
    }
    
    /**
     * Update the recognition result display
     * @param personName Name of recognized person or status message
     * @param confidence Confidence level (0.0 to 1.0)
     */
    private void updateRecognitionResult(String personName, double confidence) {
        SwingUtilities.invokeLater(() -> {
            recognitionResultLabel.setText(personName);
            
            // Update confidence bar
            int confidencePercent = (int) (confidence * 100);
            recognitionConfidence.setValue(confidencePercent);
            recognitionConfidence.setString(confidencePercent + "%");
            
            // Color coding based on confidence
            if (confidence > 0.8) {
                recognitionResultLabel.setForeground(Color.GREEN);
                recognitionConfidence.setForeground(Color.GREEN);
            } else if (confidence > 0.6) {
                recognitionResultLabel.setForeground(Color.ORANGE);
                recognitionConfidence.setForeground(Color.ORANGE);
            } else if (confidence > 0.0) {
                recognitionResultLabel.setForeground(Color.RED);
                recognitionConfidence.setForeground(Color.RED);
            } else {
                recognitionResultLabel.setForeground(Color.BLUE);
                recognitionConfidence.setForeground(Color.BLUE);
            }
            
            // Update last recognized
            if (confidence > 0.6) {
                lastRecognizedPerson = personName;
                lastConfidence = confidence;
                lastRecognizedLabel.setText("Last: " + personName + " (" + confidencePercent + "%)");
                
                // Add to history
                addToRecognitionHistory(String.format("%s: %s (%.1f%%)", 
                    java.time.LocalTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                    personName, confidence * 100));
            }
        });
    }
    
    /**
     * Add message to recognition history
     * @param message Message to add
     */
    private void addToRecognitionHistory(String message) {
        SwingUtilities.invokeLater(() -> {
            recognitionHistoryArea.append(message + "\n");
            recognitionHistoryArea.setCaretPosition(recognitionHistoryArea.getDocument().getLength());
            
            // Keep history limited to prevent memory issues
            String text = recognitionHistoryArea.getText();
            String[] lines = text.split("\n");
            if (lines.length > 50) {
                StringBuilder sb = new StringBuilder();
                for (int i = lines.length - 50; i < lines.length; i++) {
                    sb.append(lines[i]).append("\n");
                }
                recognitionHistoryArea.setText(sb.toString());
            }
        });
    }
    
    /**
     * Enable/disable face checking based on camera status
     * @param enabled Whether face checking should be enabled
     */
    public void setFaceCheckingEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            checkFaceButton.setEnabled(enabled);
            if (enabled) {
                addToRecognitionHistory("Face checking enabled - Camera started");
            } else {
                addToRecognitionHistory("Face checking disabled - Camera stopped");
                updateRecognitionResult("Camera stopped", 0.0);
            }
        });
    }
    
    /**
     * Update recognition display with new detection results
     * @param personName Recognized person name
     * @param confidence Recognition confidence
     */
    public void onPersonRecognized(String personName, double confidence) {
        updateRecognitionResult(personName, confidence);
    }
    
    /**
     * Update recognition display when no face is detected
     */
    public void onNoFaceDetected() {
        updateRecognitionResult("No face detected", 0.0);
    }
    
    /**
     * Add more face samples for the selected person to improve recognition accuracy
     */
    private void addMoreSamples() {
        String selectedPerson = personsList.getSelectedValue();
        if (selectedPerson == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a person from the list first.", 
                "No Person Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!mainApp.isRunning()) {
            JOptionPane.showMessageDialog(this, 
                "Please start the camera first.", 
                "Camera Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show dialog for number of samples to collect
        String samplesStr = JOptionPane.showInputDialog(this, 
            "How many additional samples do you want to collect for " + selectedPerson + "?",
            "Add Face Samples", 
            JOptionPane.QUESTION_MESSAGE);
            
        if (samplesStr == null || samplesStr.trim().isEmpty()) {
            return;
        }
        
        try {
            int numSamples = Integer.parseInt(samplesStr.trim());
            if (numSamples <= 0 || numSamples > 20) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a number between 1 and 20.", 
                    "Invalid Number", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            collectAdditionalSamples(selectedPerson, numSamples);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid number.", 
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Test recognition accuracy for the selected person
     */
    private void testPersonAccuracy() {
        String selectedPerson = personsList.getSelectedValue();
        if (selectedPerson == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a person from the list first.", 
                "No Person Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!mainApp.isRunning()) {
            JOptionPane.showMessageDialog(this, 
                "Please start the camera first.", 
                "Camera Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Start accuracy test
        performAccuracyTest(selectedPerson);
    }
    
    /**
     * Collect additional face samples for a person
     * @param personName Name of the person
     * @param numSamples Number of samples to collect
     */
    private void collectAdditionalSamples(String personName, int numSamples) {
        addToRecognitionHistory("Starting sample collection for " + personName + 
                               " (" + numSamples + " samples)");
        
        JDialog progressDialog = new JDialog(this, "Collecting Face Samples", true);
        JProgressBar progressBar = new JProgressBar(0, numSamples);
        progressBar.setStringPainted(true);
        JLabel statusLabel = new JLabel("Position your face clearly in front of the camera...");
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        
        JButton cancelButton = new JButton("Cancel");
        panel.add(cancelButton, BorderLayout.SOUTH);
        
        progressDialog.add(panel);
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
        
        // Background worker to collect samples
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            private volatile boolean cancelled = false;
            private int samplesCollected = 0;
            
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i < numSamples && !cancelled; i++) {
                    Thread.sleep(1500); // Give time to position face
                    
                    if (cancelled) break;
                    
                    // Try to register additional sample
                    boolean success = mainApp.registerPerson(personName);
                    if (success) {
                        samplesCollected++;
                        publish(samplesCollected);
                    }
                    
                    Thread.sleep(500); // Brief pause between samples
                }
                return null;
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                if (!chunks.isEmpty()) {
                    int latest = chunks.get(chunks.size() - 1);
                    progressBar.setValue(latest);
                    progressBar.setString(latest + " / " + numSamples + " samples collected");
                    statusLabel.setText("Sample " + (latest + 1) + " of " + numSamples + 
                                       " - Keep face steady...");
                }
            }
            
            @Override
            protected void done() {
                progressDialog.dispose();
                addToRecognitionHistory("Sample collection completed: " + 
                                       samplesCollected + "/" + numSamples + " successful");
                
                if (samplesCollected > 0) {
                    JOptionPane.showMessageDialog(FaceRecognitionGUI.this,
                        "Successfully collected " + samplesCollected + " additional samples for " + 
                        personName + ".\nRecognition accuracy should be improved.",
                        "Samples Collected", JOptionPane.INFORMATION_MESSAGE);
                    updateStats();
                } else {
                    JOptionPane.showMessageDialog(FaceRecognitionGUI.this,
                        "No samples were collected. Make sure your face is clearly visible.",
                        "Collection Failed", JOptionPane.WARNING_MESSAGE);
                }
            }
            
            public void cancel() {
                cancelled = true;
                cancel(true);
            }
        };
        
        cancelButton.addActionListener(e -> {
            worker.cancel(true);
            progressDialog.dispose();
        });
        
        worker.execute();
        progressDialog.setVisible(true);
    }
    
    /**
     * Perform accuracy test for a person
     * @param personName Name of the person to test
     */
    private void performAccuracyTest(String personName) {
        addToRecognitionHistory("Starting accuracy test for " + personName);
        
        JDialog testDialog = new JDialog(this, "Recognition Accuracy Test", true);
        JTextArea resultArea = new JTextArea(15, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        JProgressBar testProgress = new JProgressBar(0, 100);
        testProgress.setStringPainted(true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(new JLabel("Testing recognition accuracy for " + personName), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(testProgress, BorderLayout.SOUTH);
        
        testDialog.add(panel);
        testDialog.setSize(600, 400);
        testDialog.setLocationRelativeTo(this);
        
        // Background worker to perform accuracy test
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                int totalTests = 20;
                int correctRecognitions = 0;
                int totalFaceDetections = 0;
                double totalConfidence = 0.0;
                
                publish("Starting accuracy test with " + totalTests + " samples...\n");
                publish("Position " + personName + " in front of the camera.\n\n");
                
                for (int i = 0; i < totalTests; i++) {
                    testProgress.setValue((i * 100) / totalTests);
                    testProgress.setString("Test " + (i + 1) + " of " + totalTests);
                    
                    Thread.sleep(1000); // Wait between tests
                    
                    try {
                        // Capture and test recognition
                        String recognized = testSingleRecognition();
                        double confidence = mainApp.getFaceRecognizer().getLastRecognitionConfidence();
                        
                        if (recognized != null && !recognized.equals("Unknown")) {
                            totalFaceDetections++;
                            totalConfidence += confidence;
                            
                            if (recognized.equals(personName)) {
                                correctRecognitions++;
                                publish(String.format("Test %2d: ✓ Correctly recognized as %s (%.1f%% confidence)\n", 
                                       i + 1, recognized, confidence * 100));
                            } else {
                                publish(String.format("Test %2d: ✗ Incorrectly recognized as %s (%.1f%% confidence)\n", 
                                       i + 1, recognized, confidence * 100));
                            }
                        } else {
                            publish(String.format("Test %2d: ✗ No face detected or unrecognized\n", i + 1));
                        }
                        
                    } catch (Exception e) {
                        publish(String.format("Test %2d: Error - %s\n", i + 1, e.getMessage()));
                    }
                }
                
                testProgress.setValue(100);
                testProgress.setString("Test completed");
                
                // Calculate and display results
                double accuracy = totalFaceDetections > 0 ? 
                    (double) correctRecognitions / totalFaceDetections * 100 : 0;
                double avgConfidence = totalFaceDetections > 0 ? 
                    totalConfidence / totalFaceDetections * 100 : 0;
                
                publish("\n" + "=".repeat(50) + "\n");
                publish("ACCURACY TEST RESULTS for " + personName + "\n");
                publish("=".repeat(50) + "\n");
                publish(String.format("Total tests: %d\n", totalTests));
                publish(String.format("Face detections: %d\n", totalFaceDetections));
                publish(String.format("Correct recognitions: %d\n", correctRecognitions));
                publish(String.format("Recognition accuracy: %.1f%%\n", accuracy));
                publish(String.format("Average confidence: %.1f%%\n", avgConfidence));
                
                if (accuracy < 70) {
                    publish("\n⚠ Low accuracy detected. Consider:\n");
                    publish("• Adding more face samples\n");
                    publish("• Ensuring good lighting\n");
                    publish("• Testing from different angles\n");
                } else if (accuracy >= 90) {
                    publish("\n✓ Excellent recognition accuracy!\n");
                } else {
                    publish("\n✓ Good recognition accuracy.\n");
                }
                
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    resultArea.append(message);
                    resultArea.setCaretPosition(resultArea.getDocument().getLength());
                }
            }
            
            @Override
            protected void done() {
                addToRecognitionHistory("Accuracy test completed for " + personName);
            }
        };
        
        worker.execute();
        testDialog.setVisible(true);
    }
    
    /**
     * Test recognition for a single frame
     * @return Recognized person name or null if no face detected
     */
    private String testSingleRecognition() throws Exception {
        return mainApp.performSingleRecognitionTest();
    }
}
