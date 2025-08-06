package VisualPathfinder.src;

import javax.swing.*;
import java.awt.*;
import VisualPathfinder.src.algorithms.*;

public class ControlPanel extends JPanel {
    private GridPanel gridPanel;
    private JComboBox<String> algoSelector;
    private JSlider speedSlider;
    private JButton startBtn, resetBtn;
    private ButtonGroup modeGroup;

    public ControlPanel(GridPanel gridPanel) {
        this.gridPanel = gridPanel;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel modeLabel = new JLabel("Edit Mode:");
        add(modeLabel, gbc);
        gbc.gridy++;

        JRadioButton startMode = new JRadioButton("Set Start");
        JRadioButton endMode = new JRadioButton("Set End");
        JRadioButton wallMode = new JRadioButton("Draw Wall", true);
        JRadioButton eraseMode = new JRadioButton("Erase");
        modeGroup = new ButtonGroup();
        modeGroup.add(startMode);
        modeGroup.add(endMode);
        modeGroup.add(wallMode);
        modeGroup.add(eraseMode);

        add(startMode, gbc);
        gbc.gridy++;
        add(endMode, gbc);
        gbc.gridy++;
        add(wallMode, gbc);
        gbc.gridy++;
        add(eraseMode, gbc);
        gbc.gridy++;

        startMode.addActionListener(e -> gridPanel.setMode(GridPanel.Mode.START));
        endMode.addActionListener(e -> gridPanel.setMode(GridPanel.Mode.END));
        wallMode.addActionListener(e -> gridPanel.setMode(GridPanel.Mode.WALL));
        eraseMode.addActionListener(e -> gridPanel.setMode(GridPanel.Mode.ERASE));

        JLabel algoLabel = new JLabel("Algorithm:");
        add(algoLabel, gbc);
        gbc.gridy++;
        String[] algorithms = {"BFS", "DFS", "Dijkstra", "A*"};
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(algorithms);
        algoSelector = new JComboBox<>(model);
        algoSelector.setMaximumRowCount(4);
        algoSelector.setPreferredSize(new Dimension(180, 30));
        add(algoSelector, gbc);
        gbc.gridy++;

        JLabel speedLabel = new JLabel("Speed:");
        add(speedLabel, gbc);
        gbc.gridy++;
        speedSlider = new JSlider(1, 50, 5);
        speedSlider.setMajorTickSpacing(50);
        speedSlider.setPaintTicks(true);
        add(speedSlider, gbc);
        gbc.gridy++;

        startBtn = new JButton("Start");
        resetBtn = new JButton("Reset");
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.add(startBtn);
        btnPanel.add(resetBtn);
        add(btnPanel, gbc);
        gbc.gridy++;

        startBtn.addActionListener(e -> runAlgorithm());
        resetBtn.addActionListener(e -> gridPanel.resetGrid());
    }

    private void runAlgorithm() {
        String algo = (String) algoSelector.getSelectedItem();
        AlgorithmStrategy strategy = null;
        if (algo != null) {
            switch (algo) {
                case "BFS":
                    strategy = new BFS();
                    break;
                case "DFS":
                    strategy = new DFS();
                    break;
                case "Dijkstra":
                    strategy = new Dijkstra();
                    break;
                case "A*":
                    strategy = new AStar();
                    break;
            }
        }
        if (strategy != null) {
            strategy.solve(gridPanel, speedSlider.getValue());
        }
    }
}
