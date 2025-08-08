package SmartAutocomplete.src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class Main {
    private static final AutocompleteSystem autocomplete = new AutocompleteSystem();
    private static boolean darkMode = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setTitle("Smart Autocomplete System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 420);
            frame.setLocationRelativeTo(null);
            frame.setUndecorated(false);
            frame.setLayout(new BorderLayout());

            // Title Bar
            JLabel title = new JLabel("Smart Autocomplete System", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 24));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

            // Input Field with rounded border
            JTextField inputField = new JTextField();
            inputField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 2, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

            // Add Word Button
            JButton addButton = new JButton("Add Word");
            addButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            addButton.setBackground(new Color(100, 149, 237));
            addButton.setForeground(Color.WHITE);
            addButton.setFocusPainted(false);
            addButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            // Dark Mode Button
            JButton darkModeButton = new JButton("🌙");
            darkModeButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            darkModeButton.setBackground(new Color(40, 44, 52));
            darkModeButton.setForeground(Color.WHITE);
            darkModeButton.setFocusPainted(false);
            darkModeButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            // Suggestion List in card style
            DefaultListModel<String> listModel = new DefaultListModel<>();
            JList<String> suggestionList = new JList<>(listModel);
            suggestionList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            suggestionList.setSelectionBackground(new Color(100, 149, 237));
            suggestionList.setSelectionForeground(Color.WHITE);
            suggestionList.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

            JScrollPane suggestionScroll = new JScrollPane(suggestionList);
            suggestionScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            suggestionScroll.setBackground(Color.WHITE);

            // Top panel layout
            JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));
            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(addButton, BorderLayout.EAST);

            // Main card panel
            JPanel cardPanel = new JPanel(new BorderLayout());
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(16, 32, 16, 32)));
            cardPanel.setBackground(Color.WHITE);
            cardPanel.add(inputPanel, BorderLayout.NORTH);
            cardPanel.add(suggestionScroll, BorderLayout.CENTER);

            // Bottom panel for dark mode
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 32));
            bottomPanel.setBackground(Color.WHITE);
            bottomPanel.add(darkModeButton);

            // Main layout
            frame.add(title, BorderLayout.NORTH);
            frame.add(cardPanel, BorderLayout.CENTER);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            // Listeners
            inputField.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    String prefix = inputField.getText();
                    List<String> suggestions = autocomplete.getSuggestions(prefix, 5);
                    listModel.clear();
                    for (String s : suggestions) listModel.addElement(s);
                }
            });

            addButton.addActionListener(e -> {
                String word = inputField.getText();
                if (!word.isEmpty()) {
                    autocomplete.addWord(word);
                    inputField.setText("");
                    listModel.clear();
                }
            });

            suggestionList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    String selected = suggestionList.getSelectedValue();
                    if (selected != null) {
                        autocomplete.useWord(selected);
                        inputField.setText(selected);
                    }
                }
            });

            darkModeButton.addActionListener(e -> {
                darkMode = !darkMode;
                if (darkMode) {
                    frame.getContentPane().setBackground(new Color(40, 44, 52));
                    title.setForeground(Color.WHITE);
                    cardPanel.setBackground(new Color(60, 63, 65));
                    inputPanel.setBackground(new Color(60, 63, 65));
                    inputField.setBackground(new Color(40, 44, 52));
                    inputField.setForeground(Color.WHITE);
                    addButton.setBackground(new Color(100, 149, 237));
                    addButton.setForeground(Color.WHITE);
                    suggestionList.setBackground(new Color(60, 63, 65));
                    suggestionList.setForeground(Color.WHITE);
                    suggestionList.setSelectionBackground(new Color(100, 149, 237));
                    suggestionList.setSelectionForeground(Color.WHITE);
                    suggestionScroll.setBackground(new Color(60, 63, 65));
                    bottomPanel.setBackground(new Color(40, 44, 52));
                    darkModeButton.setBackground(new Color(100, 149, 237));
                } else {
                    frame.getContentPane().setBackground(Color.WHITE);
                    title.setForeground(Color.BLACK);
                    cardPanel.setBackground(Color.WHITE);
                    inputPanel.setBackground(Color.WHITE);
                    inputField.setBackground(Color.WHITE);
                    inputField.setForeground(Color.BLACK);
                    addButton.setBackground(new Color(100, 149, 237));
                    addButton.setForeground(Color.WHITE);
                    suggestionList.setBackground(Color.WHITE);
                    suggestionList.setForeground(Color.BLACK);
                    suggestionList.setSelectionBackground(new Color(100, 149, 237));
                    suggestionList.setSelectionForeground(Color.WHITE);
                    suggestionScroll.setBackground(Color.WHITE);
                    bottomPanel.setBackground(Color.WHITE);
                    darkModeButton.setBackground(new Color(40, 44, 52));
                }
            });

            frame.getContentPane().setBackground(Color.WHITE);
            frame.setVisible(true);
        });
    }
}
