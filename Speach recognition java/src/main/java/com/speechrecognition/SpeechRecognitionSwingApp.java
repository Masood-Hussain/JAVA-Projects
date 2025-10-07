package com.speechrecognition;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Alternative Swing-based GUI for Speech Recognition Application
 * This version uses Swing instead of JavaFX for better compatibility
 * Features the same functionality with a simpler setup
 */
public class SpeechRecognitionSwingApp extends JFrame {
    
    private JTextArea speechToTextArea;
    private JTextArea textToSpeechArea;
    
    // Modern GUI button references
    private JButton startListeningButton;
    private JButton speakButton;
    private JButton clearButton;
    private JButton saveButton;
    
    // Language selection components
    private JComboBox<String> languageComboBox;
    private JComboBox<String> voiceComboBox;
    
    private JLabel statusLabel;
    private JProgressBar progressBar;
    
    // Current language settings
    private String currentLanguage = "en";
    private String currentVoice = "en";
    
    private SpeechToTextService speechToTextService; // 2025 AI powered speech recognition
    private TextToSpeechService textToSpeechService;
    private boolean isListening = false;
    
    public SpeechRecognitionSwingApp() {
        initializeServices();
        setupUI();
    }
    
    private void initializeServices() {
        textToSpeechService = new TextToSpeechService();
        
        // Using advanced 2025 AI speech recognition system
        System.out.println("INFO: Speech recognition ready with enhanced audio processing.");
        
        // Create the advanced 2025 AI speech recognition service
        speechToTextService = new SpeechToTextService();
        speechToTextService.setOnResult(this::onSpeechRecognized);
        speechToTextService.setOnError(this::onSpeechError);
        speechToTextService.setOnStatusChange(this::onStatusChange);
        
        System.out.println("INFO: Speech recognition ready with enhanced audio processing.");
    }
    
    private void setupUI() {
        setTitle("🎤 Advanced Speech Recognition Studio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Enable anti-aliasing for smoother text
            System.setProperty("awt.useSystemAAFontSettings","on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            // Use default look and feel
        }
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(45, 45, 48)); // Dark modern background
        
        // Title panel
        JPanel titlePanel = createTitlePanel();
        
        // Content panel
        JPanel contentPanel = createContentPanel();
        
        // Control panel
        JPanel controlPanel = createControlPanel();
        
        // Status panel
        JPanel statusPanel = createStatusPanel();
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        // Set size and center
        setSize(900, 750);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Set minimum size
        setMinimumSize(new Dimension(700, 600));
        
        // Initialize UI state
        updateUIState();
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 32));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 150, 255)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // Main title
        JLabel titleLabel = new JLabel("🎤 Speech Recognition Studio");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 255, 255));
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Advanced AI-Powered Voice Processing");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(160, 160, 160));
        
        // Version info
        JLabel versionLabel = new JLabel("v2.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(0, 150, 255));
        
        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel, BorderLayout.NORTH);
        titleContainer.add(subtitleLabel, BorderLayout.CENTER);
        
        panel.add(titleContainer, BorderLayout.WEST);
        panel.add(versionLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBackground(new Color(45, 45, 48));
        
        // Speech to Text section (left side)
        JPanel speechToTextPanel = createSpeechToTextPanel();
        
        // Text to Speech section (right side)
        JPanel textToSpeechPanel = createTextToSpeechPanel();
        
        panel.add(speechToTextPanel);
        panel.add(textToSpeechPanel);
        
        return panel;
    }
    
    private JPanel createSpeechToTextPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 100), 2, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(new Color(35, 35, 38));
        
        // Header with icon and title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        
        JLabel headerLabel = new JLabel("🎙️ Speech Recognition");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(0, 200, 100));
        
        JLabel statusIcon = new JLabel("●");
        statusIcon.setFont(new Font("Arial", Font.BOLD, 16));
        statusIcon.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(headerLabel);
        headerPanel.add(Box.createHorizontalStrut(10));
        headerPanel.add(statusIcon);
        
        // Text area with modern styling
        speechToTextArea = new JTextArea();
        speechToTextArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
        speechToTextArea.setLineWrap(true);
        speechToTextArea.setWrapStyleWord(true);
        speechToTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        speechToTextArea.setBackground(new Color(25, 25, 28));
        speechToTextArea.setForeground(new Color(220, 220, 220));
        speechToTextArea.setCaretColor(new Color(0, 200, 100));
        speechToTextArea.setSelectionColor(new Color(0, 200, 100, 80));
        speechToTextArea.setEditable(true);
        speechToTextArea.setText("🎤 Click 'Start Listening' to begin voice recognition...\n\nSpeak clearly and the AI will transcribe your words here.");
        
        JScrollPane scrollPane = new JScrollPane(speechToTextArea);
        scrollPane.setPreferredSize(new Dimension(0, 180));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65)));
        scrollPane.getViewport().setBackground(new Color(25, 25, 28));
        
        // Modern button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        startListeningButton = createModernButton("🎤 START", new Color(0, 200, 100), new Color(0, 180, 80));
        JButton stopBtn = createModernButton("⏹ STOP", new Color(255, 80, 80), new Color(220, 60, 60));
        
        // Set up action listeners
        startListeningButton.addActionListener(e -> {
            if (isListening) {
                stopListening();
            } else {
                startListening();
            }
        });
        
        buttonPanel.add(startListeningButton);
        buttonPanel.add(stopBtn);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTextToSpeechPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 2, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(new Color(35, 35, 38));
        
        // Header with icon and title
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        
        JLabel headerLabel = new JLabel("🔊 Text-to-Speech");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(new Color(0, 150, 255));
        
        JLabel qualityLabel = new JLabel("HD Audio");
        qualityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        qualityLabel.setForeground(new Color(120, 120, 120));
        qualityLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        
        headerPanel.add(headerLabel);
        headerPanel.add(Box.createHorizontalStrut(10));
        headerPanel.add(qualityLabel);
        
        // Text area with modern styling
        textToSpeechArea = new JTextArea();
        textToSpeechArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textToSpeechArea.setLineWrap(true);
        textToSpeechArea.setWrapStyleWord(true);
        textToSpeechArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textToSpeechArea.setBackground(new Color(25, 25, 28));
        textToSpeechArea.setForeground(new Color(220, 220, 220));
        textToSpeechArea.setCaretColor(new Color(0, 150, 255));
        textToSpeechArea.setSelectionColor(new Color(0, 150, 255, 80));
        textToSpeechArea.setEditable(true);
        textToSpeechArea.setText("Type your message here and click 'SPEAK' to hear it!\n\nYou can type multiple messages and speak each one.\nThe system will convert your text into speech.");
        
        JScrollPane scrollPane = new JScrollPane(textToSpeechArea);
        scrollPane.setPreferredSize(new Dimension(0, 180));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65)));
        scrollPane.getViewport().setBackground(new Color(25, 25, 28));
        
        // Modern button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        speakButton = createModernButton("🔊 SPEAK", new Color(0, 150, 255), new Color(0, 130, 220));
        
        // Set up action listener
        speakButton.addActionListener(e -> speakText());
        
        buttonPanel.add(speakButton);
        
        // Language selection panel
        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        languagePanel.setOpaque(false);
        
        JLabel langLabel = new JLabel("Language:");
        langLabel.setForeground(new Color(180, 180, 180));
        langLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        String[] languages = {
            "English (US)", "English (UK)", "Spanish", "French", "German", 
            "Italian", "Portuguese", "Russian", "Chinese", "Japanese", 
            "Korean", "Arabic", "Hindi", "Dutch", "Swedish"
        };
        languageComboBox = new JComboBox<>(languages);
        languageComboBox.setPreferredSize(new Dimension(120, 25));
        languageComboBox.addActionListener(e -> updateLanguage());
        
        JLabel voiceLabel = new JLabel("Voice:");
        voiceLabel.setForeground(new Color(180, 180, 180));
        voiceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        String[] voices = {"Default", "Male", "Female", "Slow", "Fast"};
        voiceComboBox = new JComboBox<>(voices);
        voiceComboBox.setPreferredSize(new Dimension(80, 25));
        voiceComboBox.addActionListener(e -> updateVoice());
        
        languagePanel.add(langLabel);
        languagePanel.add(languageComboBox);
        languagePanel.add(Box.createHorizontalStrut(10));
        languagePanel.add(voiceLabel);
        languagePanel.add(voiceComboBox);
        
        // Combine language and button panels
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(languagePanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(new Color(40, 40, 43));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(70, 70, 75)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // Create modern control buttons and assign to class fields
        clearButton = createModernButton("🗑️ CLEAR", new Color(255, 150, 0), new Color(230, 130, 0));
        JButton copyBtn = createModernButton("📋 COPY", new Color(150, 100, 255), new Color(130, 80, 230));
        JButton pasteBtn = createModernButton("📄 PASTE", new Color(100, 200, 150), new Color(80, 180, 130));
        saveButton = createModernButton("💾 SAVE", new Color(120, 120, 120), new Color(100, 100, 100));
        
        // Event handlers
        clearButton.addActionListener(e -> clearAll());
        copyBtn.addActionListener(e -> copyToClipboard());
        pasteBtn.addActionListener(e -> pasteSpeechToTTS());
        saveButton.addActionListener(e -> saveToFile());
        
        panel.add(clearButton);
        panel.add(copyBtn);
        panel.add(pasteBtn);
        panel.add(saveButton);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(25, 25, 28));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(80, 80, 85)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        statusLabel = new JLabel("🟢 Ready - Click START to begin voice recognition");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(180, 180, 180));
        
        // Modern progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(120, 8));
        progressBar.setBackground(new Color(60, 60, 65));
        progressBar.setForeground(new Color(0, 150, 255));
        progressBar.setBorder(null);
        
        // Add some system info
        JLabel systemInfo = new JLabel("🖥️ " + System.getProperty("os.name"));
        systemInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        systemInfo.setForeground(new Color(120, 120, 120));
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(progressBar);
        rightPanel.add(systemInfo);
        
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    // Helper method to create modern styled buttons
    private JButton createModernButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void startListening() {
        if (!isListening) {
            // Clear previous text first
            SwingUtilities.invokeLater(() -> {
                speechToTextArea.setText("🎤 Listening... Please speak now.\n\n");
            });
            
            isListening = true;
            // Always use the reliable fallback service
            speechToTextService.startListening();
            updateUIState();
        } else {
            stopListening();
        }
    }
    
    private void stopListening() {
        if (isListening) {
            isListening = false;
            // Always use the reliable fallback service
            speechToTextService.stopListening();
            updateUIState();
        }
    }
    
    private void speakText() {
        String text = textToSpeechArea.getText().trim();
        
        // Remove placeholder text and check if there's actual content
        String cleanText = text;
        if (text.contains("Type your message here") || 
            text.contains("click 'SPEAK'") ||
            text.isEmpty()) {
            
            // Show a helpful message if no text to speak
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("⚠️ Please type some text to speak");
            });
            return;
        }
        
        // Check if already speaking
        if (textToSpeechService.isSpeaking()) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("🔊 Already speaking - please wait...");
            });
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("🔊 Speaking: \"" + cleanText.substring(0, Math.min(cleanText.length(), 30)) + "...\"");
            progressBar.setVisible(true);
            speakButton.setEnabled(false);
        });
        
        new Thread(() -> {
            try {
                textToSpeechService.speak(cleanText);
                
                // Wait for TTS to actually finish
                int timeout = 0;
                while (textToSpeechService.isSpeaking() && timeout < 100) { // 10 second max
                    Thread.sleep(100);
                    timeout++;
                }
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("✅ Finished speaking - Ready for next text");
                    progressBar.setVisible(false);
                    speakButton.setEnabled(true);
                    updateUIState();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("❌ Speech error: " + e.getMessage());
                    progressBar.setVisible(false);
                    speakButton.setEnabled(true);
                    updateUIState();
                });
            }
        }).start();
    }
    
    private void clearAll() {
        speechToTextArea.setText("🎤 Click 'START' to begin voice recognition...\n\nSpeak clearly and the AI will transcribe your words here.");
        textToSpeechArea.setText("Type your message here and click 'SPEAK' to hear it!\n\nYou can type multiple messages and speak each one.\nThe system will convert your text into speech.");
        statusLabel.setText("🗑️ Content cleared - Ready for new input");
    }
    
    private void copyToClipboard() {
        String text = speechToTextArea.getText();
        if (!text.isEmpty()) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(text), null);
            statusLabel.setText("📋 Text copied to clipboard successfully");
        } else {
            statusLabel.setText("⚠️ No text to copy");
        }
    }
    
    private void pasteSpeechToTTS() {
        String speechText = speechToTextArea.getText().trim();
        if (!speechText.isEmpty()) {
            textToSpeechArea.setText(speechText);
            statusLabel.setText("📄 Speech text pasted to TTS area");
        } else {
            statusLabel.setText("⚠️ No speech text to paste");
        }
    }
    
    private void saveToFile() {
        String content = speechToTextArea.getText().trim();
        if (content.isEmpty()) {
            statusLabel.setText("⚠️ No content to save");
            return;
        }
        
        try {
            String filename = "speech_recognition_" + System.currentTimeMillis() + ".txt";
            FileWriter writer = new FileWriter(filename);
            writer.write("Speech Recognition Output\n");
            writer.write("========================\n\n");
            writer.write(content);
            writer.close();
            statusLabel.setText("💾 Saved to " + filename);
        } catch (IOException e) {
            statusLabel.setText("❌ Error saving file: " + e.getMessage());
        }
    }
    
    private void onSpeechRecognized(String text) {
        SwingUtilities.invokeLater(() -> {
            String currentText = speechToTextArea.getText();
            // If it's the initial listening message, replace it
            if (currentText.contains("🎤 Listening... Please speak now.")) {
                speechToTextArea.setText(text + " ");
            } else {
                speechToTextArea.append(text + " ");
            }
            statusLabel.setText("🎯 Speech recognized: \"" + text.substring(0, Math.min(text.length(), 25)) + (text.length() > 25 ? "..." : "") + "\"");
            
            // Auto-scroll to bottom
            speechToTextArea.setCaretPosition(speechToTextArea.getDocument().getLength());
        });
    }
    
    private void onSpeechError(String error) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("❌ Error: " + error);
            progressBar.setVisible(false);
            isListening = false;
            updateUIState();
        });
    }
    
    private void onStatusChange(String status) {
        SwingUtilities.invokeLater(() -> {
            if (status.contains("Listening")) {
                statusLabel.setText("🎙️ " + status + " - Speak now!");
                progressBar.setVisible(true);
            } else if (status.contains("Stopped")) {
                statusLabel.setText("⏹️ " + status + " - Ready for next command");
                progressBar.setVisible(false);
            } else if (status.contains("Error")) {
                statusLabel.setText("❌ " + status);
                progressBar.setVisible(false);
            } else {
                statusLabel.setText("🔄 " + status);
            }
        });
    }
    
    private void updateUIState() {
        boolean canSpeak = !textToSpeechService.isSpeaking();
        
        startListeningButton.setText(isListening ? "🛑 STOP LISTENING" : "🎤 START LISTENING");
        startListeningButton.setEnabled(canSpeak);
        speakButton.setEnabled(canSpeak && !textToSpeechArea.getText().trim().isEmpty());
        clearButton.setEnabled(true);
        saveButton.setEnabled(!speechToTextArea.getText().trim().isEmpty());
        
        // Update button colors based on state
        if (isListening) {
            startListeningButton.setBackground(new Color(220, 53, 69)); // Red when listening
        } else {
            startListeningButton.setBackground(new Color(40, 167, 69)); // Green when ready
        }
        
        if (textToSpeechService.isSpeaking()) {
            speakButton.setBackground(new Color(255, 193, 7)); // Yellow when speaking
        } else {
            speakButton.setBackground(new Color(0, 123, 255)); // Blue when ready
        }
        
        // Update progress bar visibility
        if (isListening) {
            progressBar.setVisible(true);
        } else if (!textToSpeechService.isSpeaking()) {
            progressBar.setVisible(false);
        }
    }
    
    private void updateLanguage() {
        String selected = (String) languageComboBox.getSelectedItem();
        if (selected != null) {
            // Map UI language names to espeak language codes
            switch (selected) {
                case "English (US)": currentLanguage = "en"; currentVoice = "en"; break;
                case "English (UK)": currentLanguage = "en"; currentVoice = "en+f3"; break;
                case "Spanish": currentLanguage = "es"; currentVoice = "es"; break;
                case "French": currentLanguage = "fr"; currentVoice = "fr"; break;
                case "German": currentLanguage = "de"; currentVoice = "de"; break;
                case "Italian": currentLanguage = "it"; currentVoice = "it"; break;
                case "Portuguese": currentLanguage = "pt"; currentVoice = "pt"; break;
                case "Russian": currentLanguage = "ru"; currentVoice = "ru"; break;
                case "Chinese": currentLanguage = "zh"; currentVoice = "zh"; break;
                case "Japanese": currentLanguage = "ja"; currentVoice = "ja"; break;
                case "Korean": currentLanguage = "ko"; currentVoice = "ko"; break;
                case "Arabic": currentLanguage = "ar"; currentVoice = "ar"; break;
                case "Hindi": currentLanguage = "hi"; currentVoice = "hi"; break;
                case "Dutch": currentLanguage = "nl"; currentVoice = "nl"; break;
                case "Swedish": currentLanguage = "sv"; currentVoice = "sv"; break;
                default: currentLanguage = "en"; currentVoice = "en"; break;
            }
            statusLabel.setText("🌐 Language changed to " + selected);
            
            // Update the TTS service with new language
            textToSpeechService.setLanguage(currentVoice);
            
            // Update speech recognition with new language
            speechToTextService.setLanguage(currentLanguage);
        }
    }
    
    private void updateVoice() {
        String selected = (String) voiceComboBox.getSelectedItem();
        if (selected != null) {
            String baseVoice = currentVoice.split("\\+")[0]; // Get base language
            
            switch (selected) {
                case "Male": currentVoice = baseVoice + "+m3"; break;
                case "Female": currentVoice = baseVoice + "+f3"; break;
                case "Slow": currentVoice = baseVoice + "+s150"; break;
                case "Fast": currentVoice = baseVoice + "+s200"; break;
                default: currentVoice = baseVoice; break;
            }
            
            statusLabel.setText("🎵 Voice changed to " + selected);
            textToSpeechService.setVoice(currentVoice);
        }
    }
    
    public static void main(String[] args) {
        boolean forceCli = false;
        for (String a : args) {
            if ("--cli".equalsIgnoreCase(a)) forceCli = true;
        }
        boolean headless = java.awt.GraphicsEnvironment.isHeadless();
        if (headless || forceCli) {
            System.out.println("[INFO] Running in CLI mode (" + (headless?"headless":"requested") + ")");
            SpeechRecognitionCLI.run();
            return;
        }
        SwingUtilities.invokeLater(() -> new SpeechRecognitionSwingApp().setVisible(true));
    }
}
