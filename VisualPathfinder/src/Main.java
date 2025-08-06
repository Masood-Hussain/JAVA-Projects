package VisualPathfinder.src;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Visual Pathfinder");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new java.awt.BorderLayout());
            GridPanel gridPanel = new GridPanel(20, 30);
            ControlPanel controlPanel = new ControlPanel(gridPanel);
            frame.add(controlPanel, java.awt.BorderLayout.WEST);
            frame.add(gridPanel, java.awt.BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
