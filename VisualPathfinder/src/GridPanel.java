package VisualPathfinder.src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import VisualPathfinder.src.utils.Node;

public class GridPanel extends JPanel {
    private int rows, cols;
    private Node[][] grid;
    private Node startNode, endNode;
    private int cellSize = 30;
    private Mode mode = Mode.WALL;

    public enum Mode { START, END, WALL, ERASE }

    public GridPanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new Node[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Node(r, c);
            }
        }
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int r = e.getY() / cellSize;
                int c = e.getX() / cellSize;
                if (r < rows && c < cols) {
                    switch (mode) {
                        case START:
                            if (startNode != null) startNode.setType(Node.Type.EMPTY);
                            startNode = grid[r][c];
                            startNode.setType(Node.Type.START);
                            break;
                        case END:
                            if (endNode != null) endNode.setType(Node.Type.EMPTY);
                            endNode = grid[r][c];
                            endNode.setType(Node.Type.END);
                            break;
                        case WALL:
                            grid[r][c].setType(Node.Type.WALL);
                            break;
                        case ERASE:
                            grid[r][c].setType(Node.Type.EMPTY);
                            break;
                    }
                    repaint();
                }
            }
        });
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Node[][] getGrid() { return grid; }
    public Node getStartNode() { return startNode; }
    public Node getEndNode() { return endNode; }

    public void resetGrid() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].setType(Node.Type.EMPTY);
            }
        }
        startNode = null;
        endNode = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Node node = grid[r][c];
                switch (node.getType()) {
                    case EMPTY:
                        g.setColor(Color.WHITE);
                        break;
                    case WALL:
                        g.setColor(Color.BLACK);
                        break;
                    case START:
                        g.setColor(Color.GREEN);
                        break;
                    case END:
                        g.setColor(Color.RED);
                        break;
                    case VISITED:
                        g.setColor(Color.CYAN);
                        break;
                    case PATH:
                        g.setColor(Color.YELLOW);
                        break;
                }
                g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
                g.setColor(Color.GRAY);
                g.drawRect(c * cellSize, r * cellSize, cellSize, cellSize);
            }
        }
    }
}
